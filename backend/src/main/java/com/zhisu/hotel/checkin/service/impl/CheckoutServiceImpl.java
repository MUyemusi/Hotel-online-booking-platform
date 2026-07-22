package com.zhisu.hotel.checkin.service.impl;

import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.checkin.dto.ConfirmCheckoutRequest;
import com.zhisu.hotel.checkin.service.CheckoutService;
import com.zhisu.hotel.checkin.vo.CheckedOutRoomVO;
import com.zhisu.hotel.checkin.vo.ConfirmCheckoutVO;
import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.enums.AssignmentStatus;
import com.zhisu.hotel.common.enums.OrderStatus;
import com.zhisu.hotel.common.enums.PaymentStatus;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.room.entity.Room;
import com.zhisu.hotel.room.entity.RoomAssignment;
import com.zhisu.hotel.room.entity.RoomStatusLog;
import com.zhisu.hotel.room.mapper.RoomAssignmentMapper;
import com.zhisu.hotel.room.mapper.RoomMapper;
import com.zhisu.hotel.room.mapper.RoomStatusLogMapper;
import com.zhisu.hotel.user.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final BookingOrderMapper bookingOrderMapper;
    private final RoomAssignmentMapper roomAssignmentMapper;
    private final RoomMapper roomMapper;
    private final RoomStatusLogMapper roomStatusLogMapper;
    private final CreditService creditService;
    private final Clock clock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfirmCheckoutVO confirmCheckout(Long orderId, ConfirmCheckoutRequest request) {
        validateRequest(orderId, request);
        LocalDateTime now = LocalDateTime.now(clock);
        Long operatorId = UserContext.getUserId();

        BookingOrder order = bookingOrderMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found");
        }

        List<RoomAssignment> assignments = roomAssignmentMapper.selectByOrderIdForUpdate(orderId);
        if (OrderStatus.COMPLETED.name().equals(order.getOrderStatus())) {
            return handleCompletedOrder(order, assignments, request.getRequestToken(), operatorId, now);
        }
        validateOrder(order);
        validateAssignmentsForCheckout(order, assignments);

        List<Long> roomIds = assignments.stream()
                .map(RoomAssignment::getRoomId)
                .sorted()
                .toList();
        List<Room> rooms = roomMapper.selectByIdsForUpdate(roomIds);
        validateRoomsForCheckout(roomIds, rooms);

        for (RoomAssignment assignment : assignments.stream().sorted(Comparator.comparing(RoomAssignment::getRoomId)).toList()) {
            int updated = roomAssignmentMapper.markCheckedOutWithVersion(
                    assignment.getId(), assignment.getVersion(), now, now);
            if (updated != 1) {
                throw new BusinessException(ErrorCode.CHECKOUT_ASSIGNMENT_UPDATE_FAILED,
                        "Failed to update room assignment checkout status");
            }
            assignment.setAssignmentStatus(AssignmentStatus.CHECKED_OUT);
            assignment.setCheckOutTime(now);
        }

        Map<Long, Room> roomMap = rooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
        for (Long roomId : roomIds) {
            Room room = roomMap.get(roomId);
            int updated = roomMapper.markDirtyForCheckout(room.getId(), room.getVersion(), now);
            if (updated != 1) {
                throw new BusinessException(ErrorCode.CHECKOUT_ROOM_STATUS_CONFLICT,
                        "Failed to update room status for checkout");
            }
            RoomStatusLog log = RoomStatusLog.builder()
                    .roomId(room.getId())
                    .beforeStatus(RoomStatus.OCCUPIED)
                    .afterStatus(RoomStatus.DIRTY)
                    .operatorId(operatorId)
                    .operatorType(UserContext.getUserType() == null ? "FRONT_DESK" : UserContext.getUserType())
                    .reason(trimToNull(request.getRemark()) == null ? "CHECK_OUT" : trimToNull(request.getRemark()))
                    .relatedOrderId(orderId)
                    .createdAt(now)
                    .build();
            int inserted = roomStatusLogMapper.insert(log);
            if (inserted != 1) {
                throw new BusinessException(ErrorCode.CHECKOUT_LOG_FAILED, "Failed to write checkout room status log");
            }
            room.setRoomStatus(RoomStatus.DIRTY);
        }

        int orderUpdated = bookingOrderMapper.markCompletedIfCheckedIn(orderId, now);
        if (orderUpdated != 1) {
            throw new BusinessException(ErrorCode.CHECKOUT_ORDER_UPDATE_FAILED, "Failed to update order checkout status");
        }
        order.setOrderStatus(OrderStatus.COMPLETED.name());
        creditService.resetContinuousCancellation(order.getUserId(), order.getId());

        return buildVO(order, assignments, rooms, request.getRequestToken(), operatorId, now, "Checkout confirmed");
    }

    private void validateRequest(Long orderId, ConfirmCheckoutRequest request) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.CHECKOUT_REQUEST_INVALID, "Order id is invalid");
        }
        if (request == null || trimToNull(request.getRequestToken()) == null) {
            throw new BusinessException(ErrorCode.CHECKOUT_REQUEST_INVALID, "Request token is required");
        }
    }

    private void validateOrder(BookingOrder order) {
        if (OrderStatus.PENDING_CHECK_IN.name().equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CHECKED_IN, "Order is not checked in");
        }
        if (OrderStatus.CANCELLED.name().equals(order.getOrderStatus())
                || OrderStatus.CLOSED.name().equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CHECKOUT_NOT_ALLOWED, "Order cannot be checked out");
        }
        if (!OrderStatus.CHECKED_IN.name().equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CHECKED_IN, "Order is not checked in");
        }
        if (!PaymentStatus.PAID.name().equals(order.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAID, "Order is not paid");
        }
        if (order.getRoomCount() == null || order.getRoomCount() <= 0) {
            throw new BusinessException(ErrorCode.ORDER_ROOM_COUNT_INVALID, "Order room count is invalid");
        }
    }

    private void validateAssignmentsForCheckout(BookingOrder order, List<RoomAssignment> assignments) {
        if (assignments.isEmpty() || assignments.size() < order.getRoomCount()) {
            throw new BusinessException(ErrorCode.CHECKOUT_ASSIGNMENT_INCOMPLETE, "Checkout assignment is incomplete");
        }
        long checkedInCount = assignments.stream()
                .filter(assignment -> assignment.getAssignmentStatus() == AssignmentStatus.CHECKED_IN)
                .count();
        long checkedOutCount = assignments.stream()
                .filter(assignment -> assignment.getAssignmentStatus() == AssignmentStatus.CHECKED_OUT)
                .count();
        if (checkedInCount > 0 && checkedOutCount > 0) {
            throw new BusinessException(ErrorCode.CHECKOUT_PARTIAL_STATE_CONFLICT, "Checkout assignment is partially checked out");
        }
        if (checkedInCount != order.getRoomCount()) {
            throw new BusinessException(ErrorCode.CHECKOUT_ASSIGNMENT_INCOMPLETE, "Checkout assignment is incomplete");
        }
        Set<Long> roomIds = assignments.stream().map(RoomAssignment::getRoomId).collect(Collectors.toSet());
        if (roomIds.size() != assignments.size()) {
            throw new BusinessException(ErrorCode.CHECKOUT_DATA_INCONSISTENT, "Duplicate room assignments found");
        }
    }

    private void validateRoomsForCheckout(List<Long> roomIds, List<Room> rooms) {
        if (rooms.size() != roomIds.size()) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
        }
        Set<Long> foundIds = rooms.stream().map(Room::getId).collect(Collectors.toSet());
        if (!foundIds.equals(new HashSet<>(roomIds))) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
        }
        for (Room room : rooms) {
            if (room.getRoomStatus() != RoomStatus.OCCUPIED) {
                throw new BusinessException(ErrorCode.CHECKOUT_ROOM_NOT_OCCUPIED, "Room is not occupied");
            }
        }
    }

    private ConfirmCheckoutVO handleCompletedOrder(BookingOrder order, List<RoomAssignment> assignments,
                                                   String requestToken, Long operatorId, LocalDateTime now) {
        if (assignments.isEmpty() || assignments.stream().anyMatch(a -> a.getAssignmentStatus() == AssignmentStatus.CHECKED_IN)) {
            throw new BusinessException(ErrorCode.CHECKOUT_DATA_INCONSISTENT, "Completed order has inconsistent assignments");
        }
        List<Long> roomIds = assignments.stream().map(RoomAssignment::getRoomId).sorted().toList();
        List<Room> rooms = roomIds.isEmpty() ? List.of() : roomMapper.selectByIdsForUpdate(roomIds);
        boolean invalidRoomState = rooms.stream().anyMatch(room -> room.getRoomStatus() == RoomStatus.OCCUPIED);
        boolean missingCheckoutTime = assignments.stream().anyMatch(assignment -> assignment.getCheckOutTime() == null);
        if (invalidRoomState || missingCheckoutTime || rooms.size() != roomIds.size()) {
            throw new BusinessException(ErrorCode.CHECKOUT_DATA_INCONSISTENT, "Completed order checkout data is inconsistent");
        }
        return buildVO(order, assignments, rooms, requestToken, operatorId, now, "Order has already checked out");
    }

    private ConfirmCheckoutVO buildVO(BookingOrder order, List<RoomAssignment> assignments, List<Room> rooms,
                                      String requestToken, Long operatorId, LocalDateTime completedAt, String message) {
        Map<Long, Room> roomMap = rooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
        List<CheckedOutRoomVO> checkedOutRooms = assignments.stream()
                .sorted(Comparator.comparing(RoomAssignment::getRoomId))
                .map(assignment -> {
                    Room room = roomMap.get(assignment.getRoomId());
                    return CheckedOutRoomVO.builder()
                            .assignmentId(assignment.getId())
                            .roomId(assignment.getRoomId())
                            .roomNo(room == null ? null : room.getRoomNo())
                            .floor(room == null ? null : room.getFloor())
                            .roomTypeId(room == null ? order.getRoomTypeId() : room.getRoomTypeId())
                            .roomTypeName(order.getRoomTypeName())
                            .previousRoomStatus(RoomStatus.OCCUPIED.name())
                            .currentRoomStatus(room == null ? null : room.getRoomStatus().name())
                            .assignmentStatus(assignment.getAssignmentStatus().name())
                            .checkInTime(assignment.getCheckInTime())
                            .checkOutTime(assignment.getCheckOutTime())
                            .build();
                })
                .toList();
        LocalDateTime actualCheckOutTime = assignments.stream()
                .map(RoomAssignment::getCheckOutTime)
                .filter(time -> time != null)
                .min(LocalDateTime::compareTo)
                .orElse(completedAt);
        return ConfirmCheckoutVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .scheduledCheckInDate(order.getCheckInDate())
                .scheduledCheckOutDate(order.getCheckOutDate())
                .actualCheckOutTime(actualCheckOutTime)
                .roomCount(order.getRoomCount())
                .checkedOutRoomCount(checkedOutRooms.size())
                .checkedOutRooms(checkedOutRooms)
                .requestToken(requestToken)
                .operatorId(operatorId)
                .completedAt(completedAt)
                .message(message)
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
