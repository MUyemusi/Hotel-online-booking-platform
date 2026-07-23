

package com.zhisu.hotel.booking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhisu.hotel.booking.dto.*;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.entity.OrderDailyPrice;
import com.zhisu.hotel.booking.entity.OrderGuest;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.booking.mapper.OrderDailyPriceMapper;
import com.zhisu.hotel.booking.mapper.OrderGuestMapper;
import com.zhisu.hotel.booking.service.BookingFrontdeskService;
import com.zhisu.hotel.booking.service.BookingOrderService;
import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.booking.service.StayDateRange;
import com.zhisu.hotel.booking.vo.*;
import com.zhisu.hotel.checkin.dto.ConfirmCheckoutRequest;
import com.zhisu.hotel.checkin.service.CheckoutService;
import com.zhisu.hotel.checkin.vo.ConfirmCheckoutVO;
import com.zhisu.hotel.common.enums.CancelType;
import com.zhisu.hotel.common.enums.OrderStatus;
import com.zhisu.hotel.common.enums.PaymentStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.payment.entity.CancellationRecord;
import com.zhisu.hotel.payment.entity.PaymentRecord;
import com.zhisu.hotel.payment.mapper.CancellationRecordMapper;
import com.zhisu.hotel.payment.mapper.PaymentRecordMapper;
import com.zhisu.hotel.payment.vo.CancellationVO;
import com.zhisu.hotel.payment.vo.PaymentVO;
import com.zhisu.hotel.pricing.entity.DailyPrice;
import com.zhisu.hotel.pricing.service.PriceService;
import com.zhisu.hotel.pricing.service.PriceQuoteService;
import com.zhisu.hotel.pricing.vo.PriceQuoteVO;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import com.zhisu.hotel.user.service.CreditService;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingOrderServiceImpl implements BookingOrderService, BookingFrontdeskService {

    private final BookingOrderMapper bookingOrderMapper;
    private final OrderGuestMapper orderGuestMapper;
    private final OrderDailyPriceMapper orderDailyPriceMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final CancellationRecordMapper cancellationRecordMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final InventoryService inventoryService;
    private final CreditService creditService;
    private final CheckoutService checkoutService;
    private final PriceService priceService;
    private final PriceQuoteService priceQuoteService;
    private final BookingDatePolicyService bookingDatePolicyService;
    private final SystemConfigService systemConfigService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO createOrder(CreateOrderRequest request, Long userId) {
        BookingOrder existing = bookingOrderMapper.selectOne(
                new LambdaQueryWrapper<BookingOrder>()
                        .eq(BookingOrder::getIdempotencyKey, request.getIdempotencyKey())
                        .eq(BookingOrder::getUserId, userId));
        if (existing != null) {
            log.info("幂等请求，返回已有订单: {}", existing.getOrderNo());
            return buildDetailVO(existing);
        }
        StayDateRange stay = bookingDatePolicyService.resolveStay(
                request.getCheckInDate(), request.getCheckOutDate(), request.getStayType());
        if (!creditService.checkBookingPermission(userId)) {
            throw new BusinessException("USER_BANNED", "您当前处于禁订期，无法创建订单");
        }
        if (!inventoryService.checkInventory(request.getRoomTypeId(),
                stay.inventoryStartDate(), stay.inventoryEndExclusive(), request.getRoomCount())) {
            throw new BusinessException("INSUFFICIENT_INVENTORY", "所选日期库存不足");
        }
        RoomType roomType = roomTypeMapper.selectById(request.getRoomTypeId());
        if (roomType == null || !"ONLINE".equals(roomType.getStatus())) {
            throw new BusinessException("ROOM_TYPE_NOT_FOUND", "房型不存在或已下架");
        }
        PriceQuoteVO quote = priceQuoteService.quotePrice(request.getRoomTypeId(),
                request.getCheckInDate(), request.getCheckOutDate(), request.getRoomCount(), stay.stayType().name());
        BigDecimal totalAmount = quote.getTotalAmount();
        int totalNights = stay.stayNights();
        LocalDateTime now = LocalDateTime.now();
        String orderNo = generateOrderNo();
        String snapshotJson = serializeToJson(roomType);

        BookingOrder order = BookingOrder.builder()
                .orderNo(orderNo).userId(userId).roomTypeId(request.getRoomTypeId())
                .roomTypeName(roomType.getName()).roomTypeSnapshot(snapshotJson)
                .facilitiesSnapshot("[]").roomCount(request.getRoomCount())
                .checkInDate(request.getCheckInDate()).checkOutDate(request.getCheckOutDate())
                .stayType(stay.stayType().name()).totalNights(totalNights).guestCount(request.getGuestCount())
                .totalAmount(totalAmount).totalAmountFen(totalAmount.multiply(BigDecimal.valueOf(100)).intValue())
                .idempotencyKey(request.getIdempotencyKey()).phone(request.getPhone())
                .orderStatus(OrderStatus.PENDING_PAYMENT.name()).paymentStatus(PaymentStatus.UNPAID.name())
                .paymentTimeout(now.plusMinutes(systemConfigService.getInteger("PAYMENT_TIMEOUT_MINUTES"))).remark(request.getRemark())
                .build();
        bookingOrderMapper.insert(order);

        List<OrderGuest> guests = request.getGuests().stream().map(g ->
                OrderGuest.builder().orderId(order.getId()).guestName(g.getGuestName())
                        .guestPhone(g.getGuestPhone()).idType(g.getIdType())
                        .idNumber(g.getIdNumber()).isMain(g.getIsMain() != null && g.getIsMain() ? 1 : 0)
                        .build()).collect(Collectors.toList());
        for (OrderGuest guest : guests) {
            orderGuestMapper.insert(guest);
        }
        saveDailyPriceSnapshot(order.getId(), roomType.getId(), stay.inventoryStartDate(), stay.inventoryEndExclusive(), roomType.getBasePrice());
        try {
            inventoryService.lockInventory(order.getId(), request.getRoomTypeId(),
                    stay.inventoryStartDate(), stay.inventoryEndExclusive(), request.getRoomCount());
        } catch (Exception e) {
            log.error("锁库存失败，订单 {} 回滚", orderNo, e);
            throw new RuntimeException("锁库存失败，请稍后重试", e);
        }
        log.info("订单创建成功: {} userId={} amount={}", orderNo, userId, totalAmount);
        return buildDetailVO(order);
    }

    @Override
    public PageResponse<OrderVO> getMyOrders(Long userId, String orderStatus, int pageNum, int size) {
        LambdaQueryWrapper<BookingOrder> wrapper = new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getUserId, userId);
        if (orderStatus != null && !orderStatus.isBlank()) {
            wrapper.eq(BookingOrder::getOrderStatus, orderStatus);
        }
        wrapper.orderByDesc(BookingOrder::getCreatedAt);
        Page<BookingOrder> page = new Page<>(pageNum, size);
        Page<BookingOrder> result = bookingOrderMapper.selectPage(page, wrapper);
        List<OrderVO> records = result.getRecords().stream().map(this::buildListVO).collect(Collectors.toList());
        return new PageResponse<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId, Long userId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException("FORBIDDEN", "无权查看该订单");
        }
        return buildDetailVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CancellationVO cancelOrder(Long orderId, CancelOrderRequest request, Long userId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("FORBIDDEN", "无权操作该订单");
        return doCancel(order, CancelType.USER_CANCEL.name(), request.getCancelReason(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfirmCheckoutVO checkoutOrder(Long orderId, ConfirmCheckoutRequest request, Long userId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("FORBIDDEN", "无权操作该订单");
        return checkoutService.confirmCheckout(orderId, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CancellationVO adminCancelOrder(Long orderId, String reason) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        return doCancel(order, CancelType.ADMIN_CANCEL.name(), reason, null);
    }

    @Override
    public PageResponse<OrderVO> adminOrderList(AdminOrderSearchRequest request) {
        LambdaQueryWrapper<BookingOrder> wrapper = new LambdaQueryWrapper<>();
        if (request.getOrderNo() != null && !request.getOrderNo().isBlank()) {
            wrapper.eq(BookingOrder::getOrderNo, request.getOrderNo());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            wrapper.eq(BookingOrder::getPhone, request.getPhone());
        }
        if (request.getOrderStatus() != null && !request.getOrderStatus().isBlank()) {
            wrapper.eq(BookingOrder::getOrderStatus, request.getOrderStatus());
        }
        if (request.getStartDate() != null) wrapper.ge(BookingOrder::getCheckInDate, request.getStartDate());
        if (request.getEndDate() != null) wrapper.le(BookingOrder::getCheckOutDate, request.getEndDate());
        wrapper.orderByDesc(BookingOrder::getCreatedAt);
        Page<BookingOrder> page = new Page<>(request.getPage(), request.getSize());
        Page<BookingOrder> result = bookingOrderMapper.selectPage(page, wrapper);
        List<OrderVO> records = result.getRecords().stream().map(this::buildListVO).collect(Collectors.toList());
        return new PageResponse<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateStatus(Long orderId, String targetStatus) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        OrderStatus current = OrderStatus.valueOf(order.getOrderStatus());
        OrderStatus target = OrderStatus.valueOf(targetStatus);
        if (!isValidTransition(current, target)) {
            throw new BusinessException("INVALID_STATUS_TRANSITION", "不允许从 " + current + " 转换到 " + target);
        }
        order.setOrderStatus(target.name());
        bookingOrderMapper.updateById(order);
        if (target == OrderStatus.COMPLETED) {
            creditService.resetContinuousCancellation(order.getUserId(), order.getId());
        }
        log.info("管理员变更订单 {} 状态: {} → {}", order.getOrderNo(), current, target);
    }

    // BookingFrontdeskService impl
    @Override
    public FrontdeskOrderInfo findByOrderNo(String orderNo) {
        BookingOrder order = bookingOrderMapper.selectOne(
                new LambdaQueryWrapper<BookingOrder>().eq(BookingOrder::getOrderNo, orderNo));
        if (order == null) return null;
        return buildFrontdeskInfo(order);
    }

    @Override
    public FrontdeskOrderInfo findByPhone(String phone) {
        BookingOrder order = bookingOrderMapper.selectOne(
                new LambdaQueryWrapper<BookingOrder>()
                        .eq(BookingOrder::getPhone, phone)
                        .in(BookingOrder::getOrderStatus, OrderStatus.PENDING_CHECK_IN.name(), OrderStatus.CHECKED_IN.name())
                        .orderByDesc(BookingOrder::getCreatedAt).last("LIMIT 1"));
        if (order == null) return null;
        return buildFrontdeskInfo(order);
    }

    @Override
    public FrontdeskOrderInfo getByOrderId(Long orderId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) return null;
        return buildFrontdeskInfo(order);
    }

    @Override
    @Transactional
    public void confirmCheckedIn(Long orderId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        if (!OrderStatus.PENDING_CHECK_IN.name().equals(order.getOrderStatus())) {
            throw new BusinessException("INVALID_STATUS", "只有待入住的订单才能办理入住");
        }
        order.setOrderStatus(OrderStatus.CHECKED_IN.name());
        bookingOrderMapper.updateById(order);
        log.info("订单 {} 确认入住", order.getOrderNo());
    }

    @Override
    @Transactional
    public void confirmCompleted(Long orderId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        if (!OrderStatus.CHECKED_IN.name().equals(order.getOrderStatus())) {
            throw new BusinessException("INVALID_STATUS", "只有已入住的订单才能退房");
        }
        order.setOrderStatus(OrderStatus.COMPLETED.name());
        bookingOrderMapper.updateById(order);
        creditService.resetContinuousCancellation(order.getUserId(), order.getId());
        log.info("订单 {} 退房完成，用户 {} 连续取消计数已重置", order.getOrderNo(), order.getUserId());
    }

    // Private helpers
    private CancellationVO doCancel(BookingOrder order, String cancelType, String reason, Long operatorId) {
        String currentStatus = order.getOrderStatus();
        if (!OrderStatus.PENDING_PAYMENT.name().equals(currentStatus)
                && !OrderStatus.PENDING_CHECK_IN.name().equals(currentStatus)) {
            throw new BusinessException("INVALID_STATUS", "当前订单状态不允许取消: " + currentStatus);
        }
        if (OrderStatus.CANCELLED.name().equals(currentStatus) || OrderStatus.CLOSED.name().equals(currentStatus)) {
            throw new BusinessException("ALREADY_CANCELLED", "订单已被取消或关闭");
        }
        boolean wasPaid = PaymentStatus.PAID.name().equals(order.getPaymentStatus());
        LocalDateTime now = LocalDateTime.now();
        order.setOrderStatus(OrderStatus.CANCELLED.name());
        order.setCancelType(cancelType);
        order.setCancelReason(reason);
        order.setCancelledAt(now);
        bookingOrderMapper.updateById(order);
        inventoryService.releaseInventory(order.getId());
        BigDecimal refundAmount = null;
        String refundId = null;
        if (wasPaid) {
            refundAmount = order.getTotalAmount();
            refundId = "REF" + System.currentTimeMillis() + randomDigits(3);
            PaymentRecord refundRecord = PaymentRecord.builder()
                    .paymentNo("RF" + System.currentTimeMillis() + randomDigits(3))
                    .orderId(order.getId()).userId(order.getUserId())
                    .amount(order.getTotalAmount()).amountFen(order.getTotalAmountFen())
                    .paymentMethod("REFUND").transactionId(refundId).paymentStatus("REFUNDED")
                    .refundAmount(order.getTotalAmount()).refundAmountFen(order.getTotalAmountFen())
                    .refundTransactionId(refundId).refundedAt(now).build();
            paymentRecordMapper.insert(refundRecord);
        }
        CancellationRecord record = CancellationRecord.builder()
                .orderId(order.getId()).userId(order.getUserId())
                .cancelType(cancelType).cancelReason(reason).cancelTime(now)
                .orderStatusBefore(currentStatus).paymentStatusBefore(order.getPaymentStatus())
                .refundAmount(refundAmount)
                .refundAmountFen(refundAmount != null ? refundAmount.multiply(BigDecimal.valueOf(100)).intValue() : null)
                .refundId(refundId).operatorId(operatorId).build();
        cancellationRecordMapper.insert(record);
        if (CancelType.USER_CANCEL.name().equals(cancelType)) {
            creditService.recordUserCancellation(order.getUserId(), order.getId());
        }
        log.info("订单 {} 已取消 type={} wasPaid={} refund={}", order.getOrderNo(), cancelType, wasPaid, refundAmount);
        return CancellationVO.builder().id(record.getId()).orderId(order.getId())
                .cancelType(cancelType).cancelReason(reason).cancelTime(now)
                .orderStatusBefore(currentStatus).refundAmount(refundAmount).refundId(refundId).build();
    }

    private OrderDetailVO buildDetailVO(BookingOrder order) {
        List<OrderGuest> guests = orderGuestMapper.selectList(
                new LambdaQueryWrapper<OrderGuest>().eq(OrderGuest::getOrderId, order.getId()));
        List<GuestVO> guestVOs = guests.stream().map(g -> GuestVO.builder().id(g.getId())
                .guestName(g.getGuestName()).guestPhone(g.getGuestPhone())
                .idType(g.getIdType()).idNumber(g.getIdNumber())
                .isMain(g.getIsMain() != null && g.getIsMain() == 1).build()).collect(Collectors.toList());
        List<OrderDailyPrice> prices = orderDailyPriceMapper.selectList(
                new LambdaQueryWrapper<OrderDailyPrice>().eq(OrderDailyPrice::getOrderId, order.getId())
                        .orderByAsc(OrderDailyPrice::getPriceDate));
        List<DailyPriceVO> priceVOs = prices.stream().map(p -> DailyPriceVO.builder()
                .id(p.getId()).priceDate(p.getPriceDate()).dailyPrice(p.getDailyPrice()).build())
                .collect(Collectors.toList());
        PaymentRecord payment = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getOrderId, order.getId())
                        .orderByDesc(PaymentRecord::getCreatedAt).last("LIMIT 1"));
        PaymentVO paymentVO = null;
        if (payment != null) {
            paymentVO = PaymentVO.builder().id(payment.getId()).paymentNo(payment.getPaymentNo())
                    .orderId(payment.getOrderId()).amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod()).transactionId(payment.getTransactionId())
                    .paymentStatus(payment.getPaymentStatus()).paidAt(payment.getPaidAt())
                    .refundAmount(payment.getRefundAmount()).refundTransactionId(payment.getRefundTransactionId())
                    .refundedAt(payment.getRefundedAt()).createdAt(payment.getCreatedAt()).build();
        }
        CancellationRecord cancellation = cancellationRecordMapper.selectOne(
                new LambdaQueryWrapper<CancellationRecord>().eq(CancellationRecord::getOrderId, order.getId())
                        .orderByDesc(CancellationRecord::getCreatedAt).last("LIMIT 1"));
        CancellationVO cancellationVO = null;
        if (cancellation != null) {
            cancellationVO = CancellationVO.builder().id(cancellation.getId()).orderId(cancellation.getOrderId())
                    .cancelType(cancellation.getCancelType()).cancelReason(cancellation.getCancelReason())
                    .cancelTime(cancellation.getCancelTime()).orderStatusBefore(cancellation.getOrderStatusBefore())
                    .refundAmount(cancellation.getRefundAmount()).refundId(cancellation.getRefundId()).build();
        }
        long remainingSeconds = -1L;
        if (OrderStatus.PENDING_PAYMENT.name().equals(order.getOrderStatus()) && order.getPaymentTimeout() != null) {
            remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), order.getPaymentTimeout());
            if (remainingSeconds < 0) remainingSeconds = 0;
        }
        return OrderDetailVO.builder().id(order.getId()).orderNo(order.getOrderNo()).userId(order.getUserId())
                .roomTypeId(order.getRoomTypeId()).roomTypeName(order.getRoomTypeName())
                .roomTypeSnapshot(order.getRoomTypeSnapshot()).facilitiesSnapshot(order.getFacilitiesSnapshot())
                .roomCount(order.getRoomCount()).checkInDate(order.getCheckInDate())
                .checkOutDate(order.getCheckOutDate()).stayType(normalizeStayType(order.getStayType())).totalNights(order.getTotalNights())
                .guestCount(order.getGuestCount()).totalAmount(order.getTotalAmount())
                .phone(order.getPhone()).orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus()).paymentTimeout(order.getPaymentTimeout())
                .paidAt(order.getPaidAt()).cancelledAt(order.getCancelledAt())
                .cancelType(order.getCancelType()).cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt()).guests(guestVOs).dailyPrices(priceVOs)
                .latestPayment(paymentVO).cancellation(cancellationVO)
                .paymentRemainingSeconds(remainingSeconds).build();
    }

    private OrderVO buildListVO(BookingOrder order) {
        return OrderVO.builder().id(order.getId()).orderNo(order.getOrderNo())
                .roomTypeId(order.getRoomTypeId()).roomTypeName(order.getRoomTypeName())
                .roomCount(order.getRoomCount()).checkInDate(order.getCheckInDate())
                .checkOutDate(order.getCheckOutDate()).stayType(normalizeStayType(order.getStayType())).totalNights(order.getTotalNights())
                .guestCount(order.getGuestCount()).totalAmount(order.getTotalAmount())
                .phone(order.getPhone()).orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus()).paymentTimeout(order.getPaymentTimeout())
                .createdAt(order.getCreatedAt()).build();
    }

    private FrontdeskOrderInfo buildFrontdeskInfo(BookingOrder order) {
        List<OrderGuest> guests = orderGuestMapper.selectList(
                new LambdaQueryWrapper<OrderGuest>().eq(OrderGuest::getOrderId, order.getId()));
        List<GuestVO> guestVOs = guests.stream().map(g -> GuestVO.builder().id(g.getId())
                .guestName(g.getGuestName()).guestPhone(g.getGuestPhone())
                .idType(g.getIdType()).idNumber(g.getIdNumber())
                .isMain(g.getIsMain() != null && g.getIsMain() == 1).build()).collect(Collectors.toList());
        return FrontdeskOrderInfo.builder().orderId(order.getId()).orderNo(order.getOrderNo())
                .contactPhone(order.getPhone()).roomTypeId(order.getRoomTypeId())
                .roomCount(order.getRoomCount()).checkInDate(order.getCheckInDate())
                .checkOutDate(order.getCheckOutDate()).orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus()).guests(guestVOs).build();
    }

    private String normalizeStayType(String stayType) {
        return stayType == null || stayType.isBlank() ? "STANDARD" : stayType;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isBefore(LocalDate.now())) throw new BusinessException("INVALID_DATE", "入住日期不能早于今天");
        if (!checkOut.isAfter(checkIn)) throw new BusinessException("INVALID_DATE", "离店日期必须晚于入住日期");
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > 30) throw new BusinessException("INVALID_DATE", "最多连续入住30天");
    }

    private void saveDailyPriceSnapshot(Long orderId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut, BigDecimal fallbackPrice) {
        List<DailyPrice> prices = priceService.listDailyPrices(roomTypeId, checkIn, checkOut);
        if (prices != null && !prices.isEmpty()) {
            for (DailyPrice price : prices) {
                orderDailyPriceMapper.insert(OrderDailyPrice.builder()
                        .orderId(orderId).priceDate(price.getBusinessDate()).dailyPrice(price.getFinalPrice()).build());
            }
            return;
        }
        LocalDate date = checkIn;
        while (date.isBefore(checkOut)) {
            orderDailyPriceMapper.insert(OrderDailyPrice.builder().orderId(orderId).priceDate(date).dailyPrice(fallbackPrice).build());
            date = date.plusDays(1);
        }
    }

    private String generateOrderNo() {
        String prefix = "BO" + java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss").format(LocalDateTime.now());
        String no;
        do { no = prefix + randomDigits(4); }
        while (bookingOrderMapper.selectOne(new LambdaQueryWrapper<BookingOrder>().eq(BookingOrder::getOrderNo, no)) != null);
        return no;
    }

    private String randomDigits(int len) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < len; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private String serializeToJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (JsonProcessingException e) { log.warn("序列化JSON失败", e); return "{}"; }
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus target) {
        return switch (current) {
            case PENDING_PAYMENT -> target == OrderStatus.PENDING_CHECK_IN || target == OrderStatus.CANCELLED || target == OrderStatus.CLOSED;
            case PENDING_CHECK_IN -> target == OrderStatus.CHECKED_IN || target == OrderStatus.CANCELLED;
            case CHECKED_IN -> target == OrderStatus.COMPLETED;
            default -> false;
        };
    }
}
