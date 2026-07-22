package com.zhisu.hotel.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.common.enums.OrderStatus;
import com.zhisu.hotel.common.enums.PaymentStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.payment.dto.PaymentRequest;
import com.zhisu.hotel.payment.entity.PaymentRecord;
import com.zhisu.hotel.payment.mapper.PaymentRecordMapper;
import com.zhisu.hotel.payment.service.PaymentService;
import com.zhisu.hotel.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRecordMapper paymentRecordMapper;
    private final BookingOrderMapper bookingOrderMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized PaymentVO processPayment(PaymentRequest request, Long userId) {
        PaymentRecord idempotentRecord = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getUserId, userId)
                        .eq(PaymentRecord::getIdempotencyKey, request.getIdempotencyKey())
                        .last("LIMIT 1"));
        if (idempotentRecord != null) return toVO(idempotentRecord);

        BookingOrder order = bookingOrderMapper.selectById(request.getOrderId());
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("FORBIDDEN", "无权操作该订单");

        PaymentRecord existingPaid = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, order.getId())
                        .eq(PaymentRecord::getPaymentStatus, PaymentStatus.PAID.name())
                        .last("LIMIT 1"));
        if (existingPaid != null) return toVO(existingPaid);

        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getOrderStatus())) {
            throw new BusinessException("INVALID_ORDER_STATUS", "订单当前状态不支持支付");
        }
        if (order.getPaymentTimeout() != null && !LocalDateTime.now().isBefore(order.getPaymentTimeout())) {
            throw new BusinessException("PAYMENT_TIMEOUT", "订单支付已超时，请重新下单");
        }

        LocalDateTime now = LocalDateTime.now();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        String paymentNo = "PY" + DateTimeFormatter.ofPattern("yyMMddHHmmss").format(now) + suffix;
        String transactionId = transactionPrefix(request.getPaymentMethod()) + "-" + suffix;
        PaymentRecord record = PaymentRecord.builder()
                .paymentNo(paymentNo)
                .orderId(order.getId())
                .userId(userId)
                .amount(order.getTotalAmount())
                .amountFen(order.getTotalAmountFen())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .paymentStatus(PaymentStatus.PAID.name())
                .idempotencyKey(request.getIdempotencyKey())
                .paidAt(now)
                .build();
        paymentRecordMapper.insert(record);

        order.setOrderStatus(OrderStatus.PENDING_CHECK_IN.name());
        order.setPaymentStatus(PaymentStatus.PAID.name());
        order.setPaidAt(now);
        bookingOrderMapper.updateById(order);
        inventoryService.confirmInventory(order.getId());
        log.info("模拟支付成功: {} orderNo={} method={} amount={}", paymentNo, order.getOrderNo(), request.getPaymentMethod(), order.getTotalAmount());
        return toVO(record);
    }

    @Override
    public List<PaymentVO> getPaymentRecords(Long orderId, Long userId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("FORBIDDEN", "无权查看该订单的支付记录");
        }
        return getPaymentRecordsInternal(orderId);
    }

    @Override
    public List<PaymentVO> getPaymentRecordsByOrderId(Long orderId) {
        return getPaymentRecordsInternal(orderId);
    }

    private List<PaymentVO> getPaymentRecordsInternal(Long orderId) {
        return paymentRecordMapper.selectList(
                        new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getOrderId, orderId)
                                .orderByDesc(PaymentRecord::getCreatedAt))
                .stream().map(this::toVO).toList();
    }

    private PaymentVO toVO(PaymentRecord record) {
        return PaymentVO.builder().id(record.getId()).paymentNo(record.getPaymentNo())
                .orderId(record.getOrderId()).amount(record.getAmount()).paymentMethod(record.getPaymentMethod())
                .transactionId(record.getTransactionId()).paymentStatus(record.getPaymentStatus())
                .paidAt(record.getPaidAt()).refundAmount(record.getRefundAmount())
                .refundTransactionId(record.getRefundTransactionId()).refundedAt(record.getRefundedAt())
                .createdAt(record.getCreatedAt()).build();
    }

    private String transactionPrefix(String method) {
        return switch (method) {
            case "WECHAT" -> "WX";
            case "ALIPAY" -> "ALI";
            case "BANK_CARD" -> "BC";
            default -> "MOCK";
        };
    }
}