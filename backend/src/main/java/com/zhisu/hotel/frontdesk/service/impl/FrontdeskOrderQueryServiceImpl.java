package com.zhisu.hotel.frontdesk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.entity.OrderGuest;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.booking.mapper.OrderGuestMapper;
import com.zhisu.hotel.common.enums.AssignmentStatus;
import com.zhisu.hotel.common.enums.OrderStatus;
import com.zhisu.hotel.common.enums.PaymentStatus;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.frontdesk.dto.FrontdeskOrderQueryRequest;
import com.zhisu.hotel.frontdesk.service.FrontdeskOrderQueryService;
import com.zhisu.hotel.frontdesk.vo.AllocatableRoomVO;
import com.zhisu.hotel.frontdesk.vo.CheckinCheckItemVO;
import com.zhisu.hotel.frontdesk.vo.CheckinVerificationVO;
import com.zhisu.hotel.frontdesk.vo.FrontdeskGuestVO;
import com.zhisu.hotel.frontdesk.vo.FrontdeskOrderDetailVO;
import com.zhisu.hotel.frontdesk.vo.FrontdeskOrderListVO;
import com.zhisu.hotel.room.entity.Room;
import com.zhisu.hotel.room.entity.RoomAssignment;
import com.zhisu.hotel.room.mapper.RoomAssignmentMapper;
import com.zhisu.hotel.room.mapper.RoomMapper;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FrontdeskOrderQueryServiceImpl implements FrontdeskOrderQueryService {

    private static final Set<AssignmentStatus> ACTIVE_ASSIGNMENT_STATUSES = Set.of(
            AssignmentStatus.PENDING,
            AssignmentStatus.CHECKED_IN
    );

    private final BookingOrderMapper bookingOrderMapper;
    private final OrderGuestMapper orderGuestMapper;
    private final RoomMapper roomMapper;
    private final RoomAssignmentMapper roomAssignmentMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FrontdeskOrderListVO> searchOrders(FrontdeskOrderQueryRequest request) {
        normalize(request);
        if (!hasSearchCondition(request)) {
            throw new BusinessException(ErrorCode.FRONTDESK_SEARCH_CONDITION_REQUIRED,
                    "At least one search condition is required");
        }

        Set<Long> guestMatchedOrderIds = findGuestMatchedOrderIds(request.getContactName());
        if (request.getContactName() != null && guestMatchedOrderIds.isEmpty()) {
            int pageNum = normalizePage(request.getPage());
            int size = normalizeSize(request.getSize());
            return PageResponse.<FrontdeskOrderListVO>builder()
                    .records(Collections.emptyList())
                    .total(0)
                    .current(pageNum)
                    .size(size)
                    .pages(0)
                    .build();
        }

        LambdaQueryWrapper<BookingOrder> wrapper = new LambdaQueryWrapper<>();
        if (request.getOrderNo() != null) {
            wrapper.eq(BookingOrder::getOrderNo, request.getOrderNo());
        }
        if (request.getPhone() != null) {
            wrapper.eq(BookingOrder::getPhone, request.getPhone());
        }
        if (request.getCheckInDate() != null) {
            wrapper.eq(BookingOrder::getCheckInDate, request.getCheckInDate());
        }
        if (request.getOrderStatus() != null) {
            wrapper.eq(BookingOrder::getOrderStatus, request.getOrderStatus());
        }
        if (request.getContactName() != null) {
            wrapper.in(BookingOrder::getId, guestMatchedOrderIds);
        }
        wrapper.orderByDesc(BookingOrder::getCreatedAt);

        Page<BookingOrder> page = bookingOrderMapper.selectPage(
                new Page<>(normalizePage(request.getPage()), normalizeSize(request.getSize())),
                wrapper);
        List<OrderGuest> guests = findGuestsByOrderIds(extractOrderIds(page.getRecords()));
        Map<Long, OrderGuest> mainGuestMap = buildMainGuestMap(guests);
        List<FrontdeskOrderListVO> records = page.getRecords().stream()
                .map(order -> buildListVO(order, mainGuestMap.get(order.getId())))
                .collect(Collectors.toList());

        return new PageResponse<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    @Transactional(readOnly = true)
    public FrontdeskOrderDetailVO getOrderDetail(Long orderId) {
        BookingOrder order = findRequiredOrder(orderId);
        List<OrderGuest> guests = findGuestsByOrderIds(List.of(orderId));
        OrderGuest mainGuest = buildMainGuestMap(guests).get(orderId);
        return buildDetailVO(order, mainGuest, guests);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckinVerificationVO verifyCheckin(Long orderId) {
        return verify(findRequiredOrder(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public CheckinVerificationVO verifyCheckinByOrderNo(String orderNo) {
        String normalizedOrderNo = trimToNull(orderNo);
        if (normalizedOrderNo == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Order number is required");
        }
        BookingOrder order = bookingOrderMapper.selectOne(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getOrderNo, normalizedOrderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found");
        }
        return verify(order);
    }

    private CheckinVerificationVO verify(BookingOrder order) {
        List<CheckinCheckItemVO> items = new ArrayList<>();
        String failureCode = null;
        String failureMessage = null;

        boolean paymentPassed = PaymentStatus.PAID.name().equals(order.getPaymentStatus());
        items.add(checkItem("PAYMENT_STATUS", paymentPassed,
                paymentPassed ? "Order is paid" : "Order is not paid"));
        if (!paymentPassed) {
            failureCode = ErrorCode.ORDER_NOT_PAID;
            failureMessage = "Order is not paid";
        }

        boolean statusPassed = OrderStatus.PENDING_CHECK_IN.name().equals(order.getOrderStatus());
        items.add(checkItem("ORDER_STATUS", statusPassed,
                statusPassed ? "Order is pending check-in" : orderStatusFailureMessage(order.getOrderStatus())));
        if (failureCode == null && !statusPassed) {
            failureCode = ErrorCode.ORDER_NOT_PENDING_CHECKIN;
            failureMessage = orderStatusFailureMessage(order.getOrderStatus());
        }

        LocalDate today = LocalDate.now(clock);
        boolean datePassed = true;
        String dateMessage = "Current business date matches check-in date";
        if (order.getCheckInDate() == null || order.getCheckOutDate() == null) {
            datePassed = false;
            dateMessage = "Order check-in or check-out date is missing";
        } else if (today.isBefore(order.getCheckInDate())) {
            datePassed = false;
            dateMessage = "Check-in date has not arrived";
            if (failureCode == null) {
                failureCode = ErrorCode.CHECKIN_TOO_EARLY;
                failureMessage = dateMessage;
            }
        } else if (!today.isBefore(order.getCheckOutDate())) {
            datePassed = false;
            dateMessage = "Order has passed its valid check-in period";
            if (failureCode == null) {
                failureCode = ErrorCode.CHECKIN_EXPIRED;
                failureMessage = dateMessage;
            }
        }
        items.add(checkItem("CHECKIN_DATE", datePassed, dateMessage));

        RoomType roomType = order.getRoomTypeId() == null ? null : roomTypeMapper.selectById(order.getRoomTypeId());
        boolean roomTypePassed = roomType != null;
        items.add(checkItem("ROOM_TYPE", roomTypePassed,
                roomTypePassed ? "Order room type is valid" : "Order room type is invalid"));
        if (failureCode == null && !roomTypePassed) {
            failureCode = ErrorCode.ORDER_ROOM_TYPE_INVALID;
            failureMessage = "Order room type is invalid";
        }

        int requiredRoomCount = order.getRoomCount() == null ? 0 : order.getRoomCount();
        boolean roomCountPassed = requiredRoomCount > 0;
        items.add(checkItem("ROOM_COUNT", roomCountPassed,
                roomCountPassed ? "Order room count is valid" : "Order room count is invalid"));
        if (failureCode == null && !roomCountPassed) {
            failureCode = ErrorCode.ORDER_ROOM_COUNT_INVALID;
            failureMessage = "Order room count is invalid";
        }

        int assignedCount = countActiveAssignments(order.getId());
        int remainingRoomCount = Math.max(requiredRoomCount - assignedCount, 0);
        boolean assignmentPassed = assignedCount == 0;
        String assignmentMessage = "Order has no active room assignment";
        if (assignedCount >= requiredRoomCount && requiredRoomCount > 0) {
            assignmentMessage = "Order has completed room assignment";
            if (failureCode == null) {
                failureCode = ErrorCode.ORDER_ALREADY_ASSIGNED;
                failureMessage = assignmentMessage;
            }
        } else if (assignedCount > 0) {
            assignmentMessage = "Order has partial room assignment, remaining rooms: " + remainingRoomCount;
            if (failureCode == null) {
                failureCode = ErrorCode.ORDER_PARTIALLY_ASSIGNED;
                failureMessage = assignmentMessage;
            }
        }
        items.add(checkItem("ROOM_ASSIGNMENT", assignmentPassed, assignmentMessage));

        List<AllocatableRoomVO> allocatableRooms = roomTypePassed
                ? findAllocatableRooms(order.getRoomTypeId(), roomType.getName())
                : Collections.emptyList();
        int availableRoomCount = allocatableRooms.size();
        boolean roomAvailabilityPassed = roomTypePassed
                && roomCountPassed
                && availableRoomCount >= remainingRoomCount
                && remainingRoomCount > 0;
        String availabilityMessage = "Required rooms: " + remainingRoomCount
                + ", allocatable rooms: " + availableRoomCount;
        items.add(checkItem("ROOM_AVAILABILITY", roomAvailabilityPassed, availabilityMessage));
        if (failureCode == null && !roomAvailabilityPassed) {
            failureCode = ErrorCode.ALLOCATABLE_ROOM_NOT_ENOUGH;
            failureMessage = availabilityMessage;
        }

        boolean canCheckIn = items.stream().allMatch(item -> Boolean.TRUE.equals(item.getPassed()));
        if (canCheckIn) {
            failureCode = null;
            failureMessage = null;
        }

        String roomTypeName = roomType != null ? roomType.getName() : order.getRoomTypeName();
        return CheckinVerificationVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .orderStatusText(orderStatusText(order.getOrderStatus()))
                .paymentStatus(order.getPaymentStatus())
                .paymentStatusText(paymentStatusText(order.getPaymentStatus()))
                .checkInDate(order.getCheckInDate())
                .checkOutDate(order.getCheckOutDate())
                .roomTypeId(order.getRoomTypeId())
                .roomTypeName(roomTypeName)
                .requiredRoomCount(requiredRoomCount)
                .assignedRoomCount(assignedCount)
                .remainingRoomCount(remainingRoomCount)
                .availableRoomCount(availableRoomCount)
                .canCheckIn(canCheckIn)
                .failureCode(failureCode)
                .failureMessage(failureMessage)
                .checkItems(items)
                .allocatableRooms(allocatableRooms)
                .verifiedAt(LocalDateTime.now(clock))
                .build();
    }

    private List<AllocatableRoomVO> findAllocatableRooms(Long roomTypeId, String roomTypeName) {
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .eq(Room::getRoomTypeId, roomTypeId)
                .eq(Room::getRoomStatus, RoomStatus.AVAILABLE)
                .orderByAsc(Room::getFloor)
                .orderByAsc(Room::getRoomNo));
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
        Set<Long> assignedRoomIds = roomAssignmentMapper.selectList(new LambdaQueryWrapper<RoomAssignment>()
                        .in(RoomAssignment::getRoomId, roomIds)
                        .in(RoomAssignment::getAssignmentStatus, ACTIVE_ASSIGNMENT_STATUSES))
                .stream()
                .map(RoomAssignment::getRoomId)
                .collect(Collectors.toSet());

        return rooms.stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.AVAILABLE)
                .filter(room -> roomTypeId.equals(room.getRoomTypeId()))
                .filter(room -> !assignedRoomIds.contains(room.getId()))
                .sorted(Comparator.comparing(Room::getFloor).thenComparing(Room::getRoomNo))
                .map(room -> AllocatableRoomVO.builder()
                        .roomId(room.getId())
                        .roomNo(room.getRoomNo())
                        .floor(room.getFloor())
                        .roomTypeId(room.getRoomTypeId())
                        .roomTypeName(roomTypeName)
                        .roomStatus(room.getRoomStatus().name())
                        .roomStatusText(roomStatusText(room.getRoomStatus()))
                        .remark(room.getRemark())
                        .build())
                .collect(Collectors.toList());
    }

    private int countActiveAssignments(Long orderId) {
        return Math.toIntExact(roomAssignmentMapper.selectCount(new LambdaQueryWrapper<RoomAssignment>()
                .eq(RoomAssignment::getOrderId, orderId)
                .in(RoomAssignment::getAssignmentStatus, ACTIVE_ASSIGNMENT_STATUSES)));
    }

    private BookingOrder findRequiredOrder(Long orderId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found");
        }
        return order;
    }

    private Set<Long> findGuestMatchedOrderIds(String contactName) {
        if (contactName == null) {
            return Collections.emptySet();
        }
        return orderGuestMapper.selectList(new LambdaQueryWrapper<OrderGuest>()
                        .like(OrderGuest::getGuestName, contactName))
                .stream()
                .map(OrderGuest::getOrderId)
                .collect(Collectors.toSet());
    }

    private List<OrderGuest> findGuestsByOrderIds(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return orderGuestMapper.selectList(new LambdaQueryWrapper<OrderGuest>()
                .in(OrderGuest::getOrderId, orderIds)
                .orderByDesc(OrderGuest::getIsMain)
                .orderByAsc(OrderGuest::getId));
    }

    private Map<Long, OrderGuest> buildMainGuestMap(List<OrderGuest> guests) {
        Map<Long, OrderGuest> result = new HashMap<>();
        for (OrderGuest guest : guests) {
            OrderGuest current = result.get(guest.getOrderId());
            boolean main = guest.getIsMain() != null && guest.getIsMain() == 1;
            if (current == null || main) {
                result.put(guest.getOrderId(), guest);
            }
        }
        return result;
    }

    private List<Long> extractOrderIds(List<BookingOrder> orders) {
        return orders.stream().map(BookingOrder::getId).collect(Collectors.toList());
    }

    private FrontdeskOrderListVO buildListVO(BookingOrder order, OrderGuest mainGuest) {
        return FrontdeskOrderListVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .orderStatusText(orderStatusText(order.getOrderStatus()))
                .paymentStatus(order.getPaymentStatus())
                .paymentStatusText(paymentStatusText(order.getPaymentStatus()))
                .contactName(mainGuest != null ? mainGuest.getGuestName() : null)
                .maskedContactPhone(maskPhone(order.getPhone()))
                .roomTypeId(order.getRoomTypeId())
                .roomTypeName(order.getRoomTypeName())
                .checkInDate(order.getCheckInDate())
                .checkOutDate(order.getCheckOutDate())
                .roomCount(order.getRoomCount())
                .guestCount(order.getGuestCount())
                .amount(order.getTotalAmount())
                .expectedArrivalTime(null)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private FrontdeskOrderDetailVO buildDetailVO(BookingOrder order, OrderGuest mainGuest, List<OrderGuest> guests) {
        return FrontdeskOrderDetailVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .orderStatusText(orderStatusText(order.getOrderStatus()))
                .paymentStatus(order.getPaymentStatus())
                .paymentStatusText(paymentStatusText(order.getPaymentStatus()))
                .contactName(mainGuest != null ? mainGuest.getGuestName() : null)
                .maskedContactPhone(maskPhone(order.getPhone()))
                .roomTypeId(order.getRoomTypeId())
                .roomTypeName(order.getRoomTypeName())
                .roomCount(order.getRoomCount())
                .guestCount(order.getGuestCount())
                .checkInDate(order.getCheckInDate())
                .checkOutDate(order.getCheckOutDate())
                .totalNights(order.getTotalNights())
                .amount(order.getTotalAmount())
                .expectedArrivalTime(null)
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .remark(order.getRemark())
                .guests(guests.stream().map(this::buildGuestVO).collect(Collectors.toList()))
                .build();
    }

    private FrontdeskGuestVO buildGuestVO(OrderGuest guest) {
        return FrontdeskGuestVO.builder()
                .guestId(guest.getId())
                .guestName(guest.getGuestName())
                .maskedGuestPhone(maskPhone(guest.getGuestPhone()))
                .idType(guest.getIdType())
                .maskedIdNumber(maskIdNumber(guest.getIdNumber()))
                .main(guest.getIsMain() != null && guest.getIsMain() == 1)
                .build();
    }

    private CheckinCheckItemVO checkItem(String item, boolean passed, String message) {
        return CheckinCheckItemVO.builder()
                .item(item)
                .passed(passed)
                .message(message)
                .build();
    }

    private void normalize(FrontdeskOrderQueryRequest request) {
        request.setOrderNo(trimToNull(request.getOrderNo()));
        request.setPhone(trimToNull(request.getPhone()));
        request.setContactName(trimToNull(request.getContactName()));
        request.setOrderStatus(trimToNull(request.getOrderStatus()));
    }

    private boolean hasSearchCondition(FrontdeskOrderQueryRequest request) {
        return request.getOrderNo() != null
                || request.getPhone() != null
                || request.getContactName() != null
                || request.getCheckInDate() != null
                || request.getOrderStatus() != null;
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.length() < 8) {
            return idNumber;
        }
        return idNumber.substring(0, 3) + "********" + idNumber.substring(idNumber.length() - 4);
    }

    private String orderStatusText(String status) {
        if (status == null) {
            return null;
        }
        Map<String, String> texts = Map.of(
                OrderStatus.PENDING_PAYMENT.name(), "Pending payment",
                OrderStatus.PENDING_CHECK_IN.name(), "Pending check-in",
                OrderStatus.CHECKED_IN.name(), "Checked in",
                OrderStatus.COMPLETED.name(), "Completed",
                OrderStatus.CANCELLED.name(), "Cancelled",
                OrderStatus.CLOSED.name(), "Closed"
        );
        return texts.getOrDefault(status, status);
    }

    private String paymentStatusText(String status) {
        if (status == null) {
            return null;
        }
        Map<String, String> texts = Map.of(
                PaymentStatus.UNPAID.name(), "Unpaid",
                PaymentStatus.PAID.name(), "Paid",
                PaymentStatus.FAILED.name(), "Failed",
                PaymentStatus.REFUNDED.name(), "Refunded"
        );
        return texts.getOrDefault(status, status);
    }

    private String roomStatusText(RoomStatus status) {
        return status == null ? null : switch (status) {
            case AVAILABLE -> "Available";
            case RESERVED -> "Reserved";
            case OCCUPIED -> "Occupied";
            case DIRTY -> "Dirty";
            case MAINTENANCE -> "Maintenance";
            case DISABLED -> "Disabled";
        };
    }

    private String orderStatusFailureMessage(String status) {
        if (OrderStatus.PENDING_PAYMENT.name().equals(status)) {
            return "Order is still pending payment";
        }
        if (OrderStatus.CHECKED_IN.name().equals(status)) {
            return "Order has already checked in";
        }
        if (OrderStatus.COMPLETED.name().equals(status)) {
            return "Order has completed";
        }
        if (OrderStatus.CANCELLED.name().equals(status)) {
            return "Order has been cancelled";
        }
        if (OrderStatus.CLOSED.name().equals(status)) {
            return "Order has been closed";
        }
        return "Order is not pending check-in";
    }
}
