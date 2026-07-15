package com.zhisu.hotel.room.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.enums.AssignmentStatus;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.room.dto.CompleteCleaningRequest;
import com.zhisu.hotel.room.dto.ReportCleaningIssueRequest;
import com.zhisu.hotel.room.entity.Room;
import com.zhisu.hotel.room.entity.RoomAssignment;
import com.zhisu.hotel.room.entity.RoomStatusLog;
import com.zhisu.hotel.room.mapper.RoomAssignmentMapper;
import com.zhisu.hotel.room.mapper.RoomMapper;
import com.zhisu.hotel.room.mapper.RoomStatusLogMapper;
import com.zhisu.hotel.room.service.RoomCleaningService;
import com.zhisu.hotel.room.vo.CleaningResultVO;
import com.zhisu.hotel.room.vo.DirtyRoomVO;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomCleaningServiceImpl implements RoomCleaningService {

    private static final Set<AssignmentStatus> ACTIVE_ASSIGNMENT_STATUSES =
            Set.of(AssignmentStatus.PENDING, AssignmentStatus.CHECKED_IN);

    private final RoomMapper roomMapper;
    private final RoomAssignmentMapper roomAssignmentMapper;
    private final RoomStatusLogMapper roomStatusLogMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final BookingOrderMapper bookingOrderMapper;
    private final Clock clock;

    @Override
    public PageResponse<DirtyRoomVO> listPending(Integer floor, Long roomTypeId, int page, int size) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        Page<Room> roomPage = roomMapper.pageDirtyRooms(new Page<>(current, pageSize), floor, roomTypeId);
        List<Room> rooms = roomPage.getRecords();
        if (rooms.isEmpty()) {
            return new PageResponse<>(List.of(), roomPage.getTotal(), roomPage.getCurrent(),
                    roomPage.getSize(), roomPage.getPages());
        }

        List<Long> roomIds = rooms.stream().map(Room::getId).toList();
        Map<Long, RoomType> typeMap = roomTypeMapper.selectBatchIds(
                        rooms.stream().map(Room::getRoomTypeId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .collect(Collectors.toMap(RoomType::getId, Function.identity()));
        Map<Long, RoomAssignment> latestAssignmentMap = roomAssignmentMapper.selectCheckedOutByRoomIds(roomIds).stream()
                .collect(Collectors.toMap(RoomAssignment::getRoomId, Function.identity(), (first, ignored) -> first));
        Map<Long, BookingOrder> orderMap = bookingOrderMapper.selectBatchIds(
                        latestAssignmentMap.values().stream().map(RoomAssignment::getOrderId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(BookingOrder::getId, Function.identity()));
        Map<Long, RoomStatusLog> dirtyLogMap = roomStatusLogMapper.selectDirtyLogsByRoomIds(roomIds).stream()
                .collect(Collectors.toMap(RoomStatusLog::getRoomId, Function.identity(), (first, ignored) -> first));

        List<DirtyRoomVO> records = rooms.stream()
                .map(room -> buildDirtyRoomVO(room, typeMap.get(room.getRoomTypeId()),
                        latestAssignmentMap.get(room.getId()), orderMap, dirtyLogMap.get(room.getId())))
                .toList();
        return new PageResponse<>(records, roomPage.getTotal(), roomPage.getCurrent(),
                roomPage.getSize(), roomPage.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CleaningResultVO completeCleaning(Long roomId, CompleteCleaningRequest request) {
        validateRoomId(roomId);
        validateToken(request == null ? null : request.getRequestToken());
        LocalDateTime now = LocalDateTime.now(clock);
        Long operatorId = UserContext.getUserId();
        Room room = lockRoom(roomId);

        if (room.getRoomStatus() == RoomStatus.AVAILABLE) {
            return buildResult(room, RoomStatus.AVAILABLE, RoomStatus.AVAILABLE, now, operatorId,
                    null, "Room has already been cleaned");
        }
        ensureDirtyAndNoActiveAssignment(room);

        int updated = roomMapper.markAvailableAfterCleaning(roomId, room.getVersion(), now);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.CLEANING_STATUS_CONFLICT, "Failed to complete room cleaning");
        }
        insertLog(roomId, RoomStatus.DIRTY, RoomStatus.AVAILABLE, operatorId,
                defaultReason(request.getRemark(), "CLEANING_COMPLETE"), null, now);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setMaintenanceReason(null);
        return buildResult(room, RoomStatus.DIRTY, RoomStatus.AVAILABLE, now, operatorId,
                null, "Room cleaning completed");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CleaningResultVO reportIssue(Long roomId, ReportCleaningIssueRequest request) {
        validateRoomId(roomId);
        validateToken(request == null ? null : request.getRequestToken());
        String reason = trimToNull(request == null ? null : request.getReason());
        if (reason == null) {
            throw new BusinessException(ErrorCode.CLEANING_ISSUE_REASON_REQUIRED, "Cleaning issue reason is required");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        Long operatorId = UserContext.getUserId();
        Room room = lockRoom(roomId);
        ensureDirtyAndNoActiveAssignment(room);

        int updated = roomMapper.markMaintenanceFromDirty(roomId, room.getVersion(), reason, now);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.CLEANING_STATUS_CONFLICT, "Failed to report cleaning issue");
        }
        insertLog(roomId, RoomStatus.DIRTY, RoomStatus.MAINTENANCE, operatorId,
                defaultReason(request.getRemark(), reason), null, now);
        room.setRoomStatus(RoomStatus.MAINTENANCE);
        room.setMaintenanceReason(reason);
        return buildResult(room, RoomStatus.DIRTY, RoomStatus.MAINTENANCE, now, operatorId,
                reason, "Cleaning issue reported");
    }

    private DirtyRoomVO buildDirtyRoomVO(Room room, RoomType type, RoomAssignment latestAssignment,
                                         Map<Long, BookingOrder> orderMap, RoomStatusLog dirtyLog) {
        BookingOrder order = latestAssignment == null ? null : orderMap.get(latestAssignment.getOrderId());
        return DirtyRoomVO.builder()
                .roomId(room.getId())
                .roomNo(room.getRoomNo())
                .floor(room.getFloor())
                .roomTypeId(room.getRoomTypeId())
                .roomTypeName(type == null ? null : type.getName())
                .roomStatus(room.getRoomStatus().name())
                .latestOrderId(order == null ? null : order.getId())
                .latestOrderNo(order == null ? null : order.getOrderNo())
                .latestCheckOutTime(latestAssignment == null ? null : latestAssignment.getCheckOutTime())
                .dirtySince(dirtyLog == null ? null : dirtyLog.getCreatedAt())
                .remark(room.getRemark())
                .build();
    }

    private Room lockRoom(Long roomId) {
        Room room = roomMapper.selectByIdForUpdate(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
        }
        return room;
    }

    private void ensureDirtyAndNoActiveAssignment(Room room) {
        if (room.getRoomStatus() != RoomStatus.DIRTY) {
            throw new BusinessException(ErrorCode.CLEANING_NOT_ALLOWED, "Only dirty rooms can be cleaned");
        }
        List<RoomAssignment> activeAssignments = roomAssignmentMapper.selectActiveByRoomIds(
                List.of(room.getId()), ACTIVE_ASSIGNMENT_STATUSES);
        if (!activeAssignments.isEmpty()) {
            throw new BusinessException(ErrorCode.CLEANING_NOT_ALLOWED,
                    "Room still has active assignment and cannot be cleaned");
        }
    }

    private void insertLog(Long roomId, RoomStatus beforeStatus, RoomStatus afterStatus, Long operatorId,
                           String reason, Long relatedOrderId, LocalDateTime now) {
        RoomStatusLog log = RoomStatusLog.builder()
                .roomId(roomId)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .operatorId(operatorId)
                .operatorType(UserContext.getUserType() == null ? "CLEANER" : UserContext.getUserType())
                .reason(reason)
                .relatedOrderId(relatedOrderId)
                .createdAt(now)
                .build();
        int inserted = roomStatusLogMapper.insert(log);
        if (inserted != 1) {
            throw new BusinessException(ErrorCode.CLEANING_LOG_FAILED, "Failed to write cleaning room status log");
        }
    }

    private CleaningResultVO buildResult(Room room, RoomStatus previousStatus, RoomStatus currentStatus,
                                         LocalDateTime cleanedAt, Long operatorId,
                                         String maintenanceReason, String message) {
        return CleaningResultVO.builder()
                .roomId(room.getId())
                .roomNo(room.getRoomNo())
                .floor(room.getFloor())
                .previousStatus(previousStatus.name())
                .currentStatus(currentStatus.name())
                .cleanedAt(cleanedAt)
                .operatorId(operatorId)
                .maintenanceReason(maintenanceReason)
                .message(message)
                .build();
    }

    private void validateRoomId(Long roomId) {
        if (roomId == null || roomId <= 0) {
            throw new BusinessException(ErrorCode.CHECKOUT_REQUEST_INVALID, "Room id is invalid");
        }
    }

    private void validateToken(String requestToken) {
        if (trimToNull(requestToken) == null) {
            throw new BusinessException(ErrorCode.CHECKOUT_REQUEST_INVALID, "Request token is required");
        }
    }

    private String defaultReason(String remark, String fallback) {
        String trimmedRemark = trimToNull(remark);
        return trimmedRemark == null ? fallback : trimmedRemark;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
