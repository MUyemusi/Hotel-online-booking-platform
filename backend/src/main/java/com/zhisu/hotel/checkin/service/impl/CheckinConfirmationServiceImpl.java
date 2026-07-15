package com.zhisu.hotel.checkin.service.impl;

import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.checkin.dto.ConfirmCheckinRequest;
import com.zhisu.hotel.checkin.service.CheckinConfirmationService;
import com.zhisu.hotel.checkin.service.impl.CheckinQrServiceImpl;
import com.zhisu.hotel.checkin.util.CheckinQrTokenHasher;
import com.zhisu.hotel.checkin.vo.AssignedRoomVO;
import com.zhisu.hotel.checkin.vo.ConfirmCheckinVO;
import com.zhisu.hotel.checkin.vo.RoomAssignmentVO;
import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.enums.AssignmentStatus;
import com.zhisu.hotel.common.enums.OrderStatus;
import com.zhisu.hotel.common.enums.PaymentStatus;
import com.zhisu.hotel.common.enums.QrTokenStatus;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.room.entity.CheckinQrToken;
import com.zhisu.hotel.room.entity.Room;
import com.zhisu.hotel.room.entity.RoomAssignment;
import com.zhisu.hotel.room.entity.RoomStatusLog;
import com.zhisu.hotel.room.mapper.CheckinQrTokenMapper;
import com.zhisu.hotel.room.mapper.RoomAssignmentMapper;
import com.zhisu.hotel.room.mapper.RoomMapper;
import com.zhisu.hotel.room.mapper.RoomStatusLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckinConfirmationServiceImpl implements CheckinConfirmationService {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{32,128}$");
    private static final Set<AssignmentStatus> ACTIVE_ASSIGNMENT_STATUSES = Set.of(
            AssignmentStatus.PENDING,
            AssignmentStatus.CHECKED_IN
    );

    private final BookingOrderMapper bookingOrderMapper;
    private final RoomMapper roomMapper;
    private final RoomAssignmentMapper roomAssignmentMapper;
    private final RoomStatusLogMapper roomStatusLogMapper;
    private final CheckinQrTokenMapper checkinQrTokenMapper;
    private final CheckinQrTokenHasher tokenHasher;
    private final Clock clock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfirmCheckinVO confirm(Long orderId, ConfirmCheckinRequest request) {
        validateBasicRequest(orderId, request);
        List<Long> roomIds = normalizeAndValidateRoomIds(request.getRoomIds());

        LocalDateTime now = LocalDateTime.now(clock);
        BookingOrder order = bookingOrderMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found");
        }

        String plainToken = extractToken(request.getQrContent());
        CheckinQrToken token = checkinQrTokenMapper.selectByTokenHashForUpdate(tokenHasher.hash(plainToken));
        if (token == null) {
            throw new BusinessException(ErrorCode.QR_INVALID, "QR code is invalid");
        }
        if (!orderId.equals(token.getOrderId())) {
            throw new BusinessException(ErrorCode.QR_ORDER_MISMATCH, "QR code does not belong to this order");
        }

        List<RoomAssignment> existingAssignments = roomAssignmentMapper.selectActiveByOrderId(
                orderId, ACTIVE_ASSIGNMENT_STATUSES);
        if (OrderStatus.CHECKED_IN.name().equals(order.getOrderStatus())) {
            return handleAlreadyCheckedIn(order, token, existingAssignments, roomIds,
                    trimToNull(request.getRequestToken()), now);
        }

        validateTokenForConsumption(token, now);
        validateOrderForCheckin(order);
        validateRoomCount(order, roomIds);
        validateExistingOrderAssignments(order, existingAssignments);

        List<Room> rooms = roomMapper.selectByIdsForUpdate(roomIds);
        validateLockedRooms(order, roomIds, rooms);
        validateRoomsHaveNoActiveAssignments(orderId, roomIds);

        Long operatorId = UserContext.getUserId();
        List<RoomAssignment> createdAssignments = createAssignments(orderId, roomIds, request, operatorId, now);
        occupyRoomsAndWriteLogs(orderId, rooms, operatorId, request.getRemark(), now);

        int orderUpdated = bookingOrderMapper.markCheckedInIfPending(orderId, now);
        if (orderUpdated != 1) {
            throw new BusinessException(ErrorCode.CHECKIN_ORDER_UPDATE_FAILED, "Order status changed, please refresh and retry");
        }

        int tokenUpdated = checkinQrTokenMapper.markUsedForCheckinWithVersion(
                token.getId(), orderId, token.getVersion(), now);
        if (tokenUpdated != 1) {
            throw new BusinessException(ErrorCode.QR_TOKEN_CONSUME_NOT_ALLOWED, "Current QR token cannot be consumed");
        }

        order.setOrderStatus(OrderStatus.CHECKED_IN.name());
        token.setTokenStatus(QrTokenStatus.USED);
        token.setUsedAt(now);
        return buildConfirmVO(order, token, createdAssignments, rooms, operatorId, "Check-in confirmed", now);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomAssignmentVO> listAssignments(Long orderId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found");
        }
        List<RoomAssignment> assignments = roomAssignmentMapper.selectActiveByOrderId(orderId, ACTIVE_ASSIGNMENT_STATUSES);
        if (assignments.isEmpty()) {
            return List.of();
        }
        List<Long> roomIds = assignments.stream().map(RoomAssignment::getRoomId).toList();
        Map<Long, Room> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(Room::getId, Function.identity()));
        return assignments.stream()
                .map(assignment -> {
                    Room room = roomMap.get(assignment.getRoomId());
                    return RoomAssignmentVO.builder()
                            .assignmentId(assignment.getId())
                            .orderId(assignment.getOrderId())
                            .roomId(assignment.getRoomId())
                            .roomNo(room == null ? null : room.getRoomNo())
                            .floor(room == null ? null : room.getFloor())
                            .roomTypeName(order.getRoomTypeName())
                            .assignmentStatus(assignment.getAssignmentStatus().name())
                            .assignedAt(assignment.getAssignedAt())
                            .checkInTime(assignment.getCheckInTime())
                            .checkOutTime(assignment.getCheckOutTime())
                            .build();
                })
                .toList();
    }

    private void validateBasicRequest(Long orderId, ConfirmCheckinRequest request) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.CHECKIN_REQUEST_INVALID, "Order id is invalid");
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.CHECKIN_REQUEST_INVALID, "Check-in request is required");
        }
        if (request.getQrContent() == null || request.getQrContent().isBlank()) {
            throw new BusinessException(ErrorCode.CHECKIN_QR_REQUIRED, "QR content is required");
        }
    }

    private List<Long> normalizeAndValidateRoomIds(List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            throw new BusinessException(ErrorCode.ROOM_SELECTION_COUNT_MISMATCH, "Selected room count does not match order");
        }
        List<Long> normalized = new ArrayList<>();
        for (Long roomId : roomIds) {
            if (roomId == null || roomId <= 0) {
                throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
            }
            normalized.add(roomId);
        }
        if (new HashSet<>(normalized).size() != normalized.size()) {
            throw new BusinessException(ErrorCode.ROOM_SELECTION_DUPLICATE, "Selected rooms contain duplicates");
        }
        return normalized.stream().sorted().toList();
    }

    private String extractToken(String qrContent) {
        if (qrContent == null || qrContent.isBlank() || !qrContent.startsWith(CheckinQrServiceImpl.QR_PREFIX)) {
            throw new BusinessException(ErrorCode.QR_FORMAT_INVALID, "QR code format is invalid");
        }
        String token = qrContent.substring(CheckinQrServiceImpl.QR_PREFIX.length());
        if (!TOKEN_PATTERN.matcher(token).matches()) {
            throw new BusinessException(ErrorCode.QR_FORMAT_INVALID, "QR code format is invalid");
        }
        return token;
    }

    private void validateTokenForConsumption(CheckinQrToken token, LocalDateTime now) {
        if (token.getTokenStatus() == QrTokenStatus.USED) {
            throw new BusinessException(ErrorCode.QR_ALREADY_USED, "QR code has already been used");
        }
        if (token.getTokenStatus() == QrTokenStatus.REVOKED) {
            throw new BusinessException(ErrorCode.QR_REVOKED, "QR code has been revoked");
        }
        if (token.getTokenStatus() == QrTokenStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.QR_EXPIRED, "QR code has expired");
        }
        if (token.getTokenStatus() != QrTokenStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.QR_TOKEN_CONSUME_NOT_ALLOWED, "Current QR token cannot be consumed");
        }
        if (token.getExpireAt() == null || !now.isBefore(token.getExpireAt())) {
            throw new BusinessException(ErrorCode.QR_EXPIRED, "QR code has expired");
        }
    }

    private void validateOrderForCheckin(BookingOrder order) {
        if (!PaymentStatus.PAID.name().equals(order.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAID, "Order is not paid");
        }
        if (!OrderStatus.PENDING_CHECK_IN.name().equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PENDING_CHECKIN, "Order is not pending check-in");
        }
        if (order.getRoomTypeId() == null) {
            throw new BusinessException(ErrorCode.ORDER_ROOM_TYPE_INVALID, "Order room type is invalid");
        }
        if (order.getRoomCount() == null || order.getRoomCount() <= 0) {
            throw new BusinessException(ErrorCode.ORDER_ROOM_COUNT_INVALID, "Order room count is invalid");
        }
        LocalDate today = LocalDate.now(clock);
        if (order.getCheckInDate() == null || order.getCheckOutDate() == null) {
            throw new BusinessException(ErrorCode.CHECKIN_REQUEST_INVALID, "Order check-in or check-out date is missing");
        }
        if (today.isBefore(order.getCheckInDate())) {
            throw new BusinessException(ErrorCode.CHECKIN_TOO_EARLY, "Check-in date has not arrived");
        }
        if (!today.isBefore(order.getCheckOutDate())) {
            throw new BusinessException(ErrorCode.CHECKIN_EXPIRED, "Order has passed its valid check-in period");
        }
    }

    private void validateRoomCount(BookingOrder order, List<Long> roomIds) {
        if (roomIds.size() != order.getRoomCount()) {
            throw new BusinessException(ErrorCode.ROOM_SELECTION_COUNT_MISMATCH,
                    "Selected room count does not match order room count");
        }
    }

    private void validateExistingOrderAssignments(BookingOrder order, List<RoomAssignment> existingAssignments) {
        if (existingAssignments.isEmpty()) {
            return;
        }
        if (existingAssignments.size() >= order.getRoomCount()) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_ASSIGNED, "Order already has active room assignments");
        }
        throw new BusinessException(ErrorCode.ORDER_PARTIALLY_ASSIGNED, "Order has partial room assignments");
    }

    private void validateLockedRooms(BookingOrder order, List<Long> roomIds, List<Room> rooms) {
        if (rooms.size() != roomIds.size()) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
        }
        Set<Long> foundIds = rooms.stream().map(Room::getId).collect(Collectors.toSet());
        for (Long roomId : roomIds) {
            if (!foundIds.contains(roomId)) {
                throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
            }
        }
        for (Room room : rooms) {
            if (!order.getRoomTypeId().equals(room.getRoomTypeId())) {
                throw new BusinessException(ErrorCode.ROOM_TYPE_MISMATCH, "Selected room type does not match order");
            }
            if (room.getRoomStatus() != RoomStatus.AVAILABLE) {
                throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE, "Room is not available");
            }
        }
    }

    private void validateRoomsHaveNoActiveAssignments(Long orderId, List<Long> roomIds) {
        List<RoomAssignment> roomAssignments = roomAssignmentMapper.selectActiveByRoomIds(roomIds, ACTIVE_ASSIGNMENT_STATUSES);
        boolean occupiedByOtherOrder = roomAssignments.stream()
                .anyMatch(assignment -> !orderId.equals(assignment.getOrderId()));
        if (occupiedByOtherOrder || !roomAssignments.isEmpty()) {
            throw new BusinessException(ErrorCode.ROOM_ALREADY_ASSIGNED, "Room already has active assignment");
        }
    }

    private List<RoomAssignment> createAssignments(Long orderId, List<Long> roomIds, ConfirmCheckinRequest request,
                                                   Long operatorId, LocalDateTime now) {
        List<RoomAssignment> assignments = new ArrayList<>();
        for (Long roomId : roomIds) {
            RoomAssignment assignment = RoomAssignment.builder()
                    .orderId(orderId)
                    .roomId(roomId)
                    .assignmentStatus(AssignmentStatus.CHECKED_IN)
                    .assignedBy(operatorId)
                    .assignedAt(now)
                    .checkInTime(now)
                    .requestToken(trimToNull(request.getRequestToken()))
                    .version(0)
                    .isDeleted(0)
                    .build();
            int inserted = roomAssignmentMapper.insert(assignment);
            if (inserted != 1) {
                throw new BusinessException(ErrorCode.CHECKIN_ASSIGNMENT_FAILED, "Failed to create room assignment");
            }
            assignments.add(assignment);
        }
        return assignments;
    }

    private void occupyRoomsAndWriteLogs(Long orderId, List<Room> rooms, Long operatorId, String remark, LocalDateTime now) {
        for (Room room : rooms.stream().sorted(Comparator.comparing(Room::getId)).toList()) {
            int updated = roomMapper.markOccupiedForCheckin(room.getId(), room.getVersion(), now);
            if (updated != 1) {
                throw new BusinessException(ErrorCode.CHECKIN_ROOM_UPDATE_FAILED, "Failed to update room status");
            }
            RoomStatusLog log = RoomStatusLog.builder()
                    .roomId(room.getId())
                    .beforeStatus(RoomStatus.AVAILABLE)
                    .afterStatus(RoomStatus.OCCUPIED)
                    .operatorId(operatorId)
                    .operatorType(UserContext.getUserType() == null ? "FRONT_DESK" : UserContext.getUserType())
                    .reason(trimToNull(remark) == null ? "CHECK_IN" : trimToNull(remark))
                    .relatedOrderId(orderId)
                    .createdAt(now)
                    .build();
            int inserted = roomStatusLogMapper.insert(log);
            if (inserted != 1) {
                throw new BusinessException(ErrorCode.CHECKIN_CONFLICT, "Failed to write room status log");
            }
            room.setRoomStatus(RoomStatus.OCCUPIED);
        }
    }

    private ConfirmCheckinVO handleAlreadyCheckedIn(BookingOrder order, CheckinQrToken token,
                                                    List<RoomAssignment> assignments, List<Long> roomIds,
                                                    String requestToken, LocalDateTime now) {
        if (assignments.isEmpty() || assignments.size() != order.getRoomCount()) {
            throw new BusinessException(ErrorCode.ORDER_PARTIALLY_ASSIGNED, "Order checked in but assignments are incomplete");
        }
        Set<String> existingRequestTokens = assignments.stream()
                .map(RoomAssignment::getRequestToken)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toSet());
        if (requestToken != null && !existingRequestTokens.isEmpty() && !existingRequestTokens.contains(requestToken)) {
            throw new BusinessException(ErrorCode.CHECKIN_CONFLICT, "Check-in request token conflicts with existing result");
        }
        Set<Long> existingRoomIds = assignments.stream().map(RoomAssignment::getRoomId).collect(Collectors.toSet());
        if (!existingRoomIds.equals(new HashSet<>(roomIds))) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CHECKED_IN, "Order has already checked in with different rooms");
        }
        List<Room> rooms = roomMapper.selectBatchIds(roomIds);
        return buildConfirmVO(order, token, assignments, rooms, null, "Order has already checked in", now);
    }

    private ConfirmCheckinVO buildConfirmVO(BookingOrder order, CheckinQrToken token, List<RoomAssignment> assignments,
                                            List<Room> rooms, Long operatorId, String message, LocalDateTime completedAt) {
        Map<Long, Room> roomMap = rooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
        List<AssignedRoomVO> assignedRooms = assignments.stream()
                .sorted(Comparator.comparing(RoomAssignment::getRoomId))
                .map(assignment -> {
                    Room room = roomMap.get(assignment.getRoomId());
                    return AssignedRoomVO.builder()
                            .assignmentId(assignment.getId())
                            .roomId(assignment.getRoomId())
                            .roomNo(room == null ? null : room.getRoomNo())
                            .floor(room == null ? null : room.getFloor())
                            .roomTypeId(room == null ? order.getRoomTypeId() : room.getRoomTypeId())
                            .roomTypeName(order.getRoomTypeName())
                            .roomStatus(room == null ? RoomStatus.OCCUPIED.name() : room.getRoomStatus().name())
                            .assignmentStatus(assignment.getAssignmentStatus().name())
                            .assignedAt(assignment.getAssignedAt())
                            .checkInTime(assignment.getCheckInTime())
                            .build();
                })
                .toList();
        LocalDateTime checkInTime = assignments.stream()
                .map(RoomAssignment::getCheckInTime)
                .filter(time -> time != null)
                .min(LocalDateTime::compareTo)
                .orElse(completedAt);
        return ConfirmCheckinVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .checkInTime(checkInTime)
                .checkInDate(order.getCheckInDate())
                .checkOutDate(order.getCheckOutDate())
                .roomTypeId(order.getRoomTypeId())
                .roomTypeName(order.getRoomTypeName())
                .requiredRoomCount(order.getRoomCount())
                .assignedRoomCount(assignedRooms.size())
                .assignedRooms(assignedRooms)
                .tokenId(token.getId())
                .tokenStatus(token.getTokenStatus().name())
                .message(message)
                .operatorId(operatorId)
                .completedAt(completedAt)
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
