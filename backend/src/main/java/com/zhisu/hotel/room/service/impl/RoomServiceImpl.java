package com.zhisu.hotel.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.entity.OrderGuest;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.booking.mapper.OrderGuestMapper;
import com.zhisu.hotel.common.enums.AssignmentStatus;
import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.room.dto.RoomCreateRequest;
import com.zhisu.hotel.room.dto.RoomQueryRequest;
import com.zhisu.hotel.room.dto.RoomStatusChangeRequest;
import com.zhisu.hotel.room.dto.RoomUpdateRequest;
import com.zhisu.hotel.room.entity.Room;
import com.zhisu.hotel.room.entity.RoomAssignment;
import com.zhisu.hotel.room.entity.RoomStatusLog;
import com.zhisu.hotel.room.mapper.RoomAssignmentMapper;
import com.zhisu.hotel.room.mapper.RoomMapper;
import com.zhisu.hotel.room.mapper.RoomStatusLogMapper;
import com.zhisu.hotel.room.service.RoomService;
import com.zhisu.hotel.room.vo.FloorRoomStatisticsVO;
import com.zhisu.hotel.room.vo.RoomDetailVO;
import com.zhisu.hotel.room.vo.RoomStatusLogVO;
import com.zhisu.hotel.room.vo.RoomVO;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final RoomStatusLogMapper roomStatusLogMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final RoomAssignmentMapper roomAssignmentMapper;
    private final OrderGuestMapper orderGuestMapper;
    private final BookingOrderMapper bookingOrderMapper;

    private static final Set<RoomStatus> ADMIN_EDITABLE_STATUSES = Set.of(
            RoomStatus.AVAILABLE,
            RoomStatus.MAINTENANCE,
            RoomStatus.DISABLED
    );

    private static final Set<RoomStatus> PROTECTED_STATUSES = Set.of(
            RoomStatus.RESERVED,
            RoomStatus.OCCUPIED,
            RoomStatus.DIRTY
    );

    @Override
    @Transactional
    public Room createRoom(RoomCreateRequest request) {
        if (!checkRoomNoUnique(request.getRoomNo())) {
            throw new BusinessException(ErrorCode.ROOM_NO_DUPLICATE, "Room number already exists");
        }
        if (!validateRoomNoAndFloor(request.getRoomNo(), request.getFloor())) {
            throw new BusinessException(ErrorCode.ROOM_FLOOR_MISMATCH, "Room number does not match floor");
        }
        if (request.getFloor() < 3 || request.getFloor() > 8) {
            throw new BusinessException(ErrorCode.ROOM_NO_INVALID, "Floor must be between 3 and 8");
        }

        RoomType roomType = roomTypeMapper.selectById(request.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException(ErrorCode.ROOM_TYPE_NOT_FOUND, "Room type not found");
        }

        Room room = Room.builder()
                .roomNo(request.getRoomNo())
                .floor(request.getFloor())
                .roomTypeId(request.getRoomTypeId())
                .roomStatus(RoomStatus.AVAILABLE)
                .nearElevator(Boolean.TRUE.equals(request.getNearElevator()))
                .headRoom(Boolean.TRUE.equals(request.getHeadRoom()))
                .tailRoom(Boolean.TRUE.equals(request.getTailRoom()))
                .cadImageUrl(request.getCadImageUrl())
                .remark(request.getRemark())
                .isDeleted(0)
                .build();
        roomMapper.insert(room);
        return room;
    }

    @Override
    @Transactional
    public Room updateRoom(Long id, RoomUpdateRequest request) {
        Room room = roomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
        }

        RoomType roomType = roomTypeMapper.selectById(request.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException(ErrorCode.ROOM_TYPE_NOT_FOUND, "Room type not found");
        }

        room.setRoomTypeId(request.getRoomTypeId());
        room.setNearElevator(Boolean.TRUE.equals(request.getNearElevator()));
        room.setHeadRoom(Boolean.TRUE.equals(request.getHeadRoom()));
        room.setTailRoom(Boolean.TRUE.equals(request.getTailRoom()));
        room.setCadImageUrl(request.getCadImageUrl());
        room.setRemark(request.getRemark());
        int updated = roomMapper.updateRoomProfile(
                room.getId(),
                room.getRoomTypeId(),
                room.getNearElevator(),
                room.getHeadRoom(),
                room.getTailRoom(),
                room.getCadImageUrl(),
                room.getRemark(),
                LocalDateTime.now()
        );
        if (updated == 0) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
        }
        return roomMapper.selectById(id);
    }

    @Override
    public RoomDetailVO getRoomDetail(Long id) {
        Room room = roomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
        }

        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        CurrentGuestInfo currentGuestInfo = findCurrentGuestInfo(room);

        return RoomDetailVO.builder()
                .id(room.getId())
                .roomNo(room.getRoomNo())
                .floor(room.getFloor())
                .roomTypeId(room.getRoomTypeId())
                .roomTypeName(roomType != null ? roomType.getName() : null)
                .roomStatus(room.getRoomStatus())
                .roomStatusText(getRoomStatusText(room.getRoomStatus()))
                .maintenanceReason(room.getMaintenanceReason())
                .nearElevator(Boolean.TRUE.equals(room.getNearElevator()))
                .headRoom(Boolean.TRUE.equals(room.getHeadRoom()))
                .tailRoom(Boolean.TRUE.equals(room.getTailRoom()))
                .cadImageUrl(room.getCadImageUrl())
                .currentOrderId(currentGuestInfo.orderId())
                .currentOrderNo(currentGuestInfo.orderNo())
                .currentGuestName(currentGuestInfo.guestName())
                .currentGuestPhone(currentGuestInfo.guestPhone())
                .remark(room.getRemark())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    @Override
    public Page<RoomVO> pageRooms(int page, int size, RoomQueryRequest request) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        if (request.getFloor() != null) {
            wrapper.eq(Room::getFloor, request.getFloor());
        }
        if (request.getRoomTypeId() != null) {
            wrapper.eq(Room::getRoomTypeId, request.getRoomTypeId());
        }
        if (request.getRoomStatus() != null) {
            wrapper.eq(Room::getRoomStatus, request.getRoomStatus());
        }
        if (request.getRoomNo() != null && !request.getRoomNo().isBlank()) {
            wrapper.like(Room::getRoomNo, request.getRoomNo());
        }
        wrapper.orderByAsc(Room::getFloor).orderByAsc(Room::getRoomNo);

        Page<Room> roomPage = roomMapper.selectPage(new Page<>(page, size), wrapper);

        Set<Long> roomTypeIds = roomPage.getRecords().stream()
                .map(Room::getRoomTypeId)
                .collect(Collectors.toSet());

        Map<Long, RoomType> roomTypeMap = new HashMap<>();
        if (!roomTypeIds.isEmpty()) {
            List<RoomType> roomTypes = roomTypeMapper.selectBatchIds(roomTypeIds);
            roomTypeMap.putAll(roomTypes.stream()
                    .collect(Collectors.toMap(RoomType::getId, roomType -> roomType)));
        }

        List<RoomVO> roomVOs = roomPage.getRecords().stream()
                .map(room -> convertToVO(room, roomTypeMap.get(room.getRoomTypeId())))
                .collect(Collectors.toList());

        Page<RoomVO> resultPage = new Page<>(page, size, roomPage.getTotal());
        resultPage.setRecords(roomVOs);
        return resultPage;
    }

    @Override
    public List<RoomVO> getRoomsByFloor(Integer floor) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getFloor, floor);
        wrapper.orderByAsc(Room::getRoomNo);

        List<Room> rooms = roomMapper.selectList(wrapper);

        Set<Long> roomTypeIds = rooms.stream()
                .map(Room::getRoomTypeId)
                .collect(Collectors.toSet());

        Map<Long, RoomType> roomTypeMap = new HashMap<>();
        if (!roomTypeIds.isEmpty()) {
            List<RoomType> roomTypes = roomTypeMapper.selectBatchIds(roomTypeIds);
            roomTypeMap.putAll(roomTypes.stream()
                    .collect(Collectors.toMap(RoomType::getId, roomType -> roomType)));
        }

        return rooms.stream()
                .map(room -> convertToVO(room, roomTypeMap.get(room.getRoomTypeId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeRoomStatus(Long id, RoomStatusChangeRequest request) {
        Room room = roomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Room not found");
        }

        RoomStatus currentStatus = room.getRoomStatus();
        RoomStatus targetStatus = request.getTargetStatus();

        if (PROTECTED_STATUSES.contains(currentStatus)) {
            throw new BusinessException(
                    ErrorCode.ROOM_STATUS_INVALID,
                    "Current status cannot be changed directly; use the business workflow"
            );
        }

        if (!ADMIN_EDITABLE_STATUSES.contains(targetStatus)) {
            throw new BusinessException(
                    ErrorCode.ROOM_STATUS_INVALID,
                    "Target status cannot be set by this endpoint"
            );
        }

        if ((targetStatus == RoomStatus.MAINTENANCE || targetStatus == RoomStatus.DISABLED)
                && (request.getReason() == null || request.getReason().isBlank())) {
            throw new BusinessException(
                    ErrorCode.ROOM_MAINTENANCE_REASON_REQUIRED,
                    "Maintenance or disabled status requires a reason"
            );
        }

        String newMaintenanceReason;
        if (targetStatus == RoomStatus.MAINTENANCE) {
            newMaintenanceReason = request.getReason();
        } else if (currentStatus == RoomStatus.MAINTENANCE) {
            newMaintenanceReason = null;
        } else {
            newMaintenanceReason = room.getMaintenanceReason();
        }

        int updated = roomMapper.updateStatusWithCondition(
                room.getId(),
                currentStatus,
                targetStatus,
                newMaintenanceReason,
                room.getVersion()
        );

        if (updated == 0) {
            throw new BusinessException(
                    ErrorCode.ROOM_STATUS_INVALID,
                    "Room status was changed by another operation; refresh and retry"
            );
        }

        RoomStatusLog log = RoomStatusLog.builder()
                .roomId(room.getId())
                .beforeStatus(currentStatus)
                .afterStatus(targetStatus)
                .operatorId(UserContext.getUserId())
                .operatorType(UserContext.getUserType())
                .reason(request.getReason())
                .build();
        roomStatusLogMapper.insert(log);
    }

    @Override
    public List<RoomStatusLogVO> getRoomStatusLogs(Long roomId) {
        LambdaQueryWrapper<RoomStatusLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomStatusLog::getRoomId, roomId);
        wrapper.orderByDesc(RoomStatusLog::getCreatedAt);

        List<RoomStatusLog> logs = roomStatusLogMapper.selectList(wrapper);

        Room room = roomMapper.selectById(roomId);

        return logs.stream()
                .map(log -> RoomStatusLogVO.builder()
                        .id(log.getId())
                        .roomId(log.getRoomId())
                        .roomNo(room != null ? room.getRoomNo() : null)
                        .beforeStatus(log.getBeforeStatus())
                        .beforeStatusText(getRoomStatusText(log.getBeforeStatus()))
                        .afterStatus(log.getAfterStatus())
                        .afterStatusText(getRoomStatusText(log.getAfterStatus()))
                        .operatorId(log.getOperatorId())
                        .operatorType(log.getOperatorType())
                        .reason(log.getReason())
                        .relatedOrderId(log.getRelatedOrderId())
                        .createdAt(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<FloorRoomStatisticsVO> getFloorStatistics() {
        List<FloorRoomStatisticsVO> result = new ArrayList<>();

        for (int floor = 3; floor <= 8; floor++) {
            LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Room::getFloor, floor);
            List<Room> rooms = roomMapper.selectList(wrapper);

            FloorRoomStatisticsVO stats = FloorRoomStatisticsVO.builder()
                    .floor(floor)
                    .totalCount(rooms.size())
                    .availableCount((int) rooms.stream().filter(r -> r.getRoomStatus() == RoomStatus.AVAILABLE).count())
                    .reservedCount((int) rooms.stream().filter(r -> r.getRoomStatus() == RoomStatus.RESERVED).count())
                    .occupiedCount((int) rooms.stream().filter(r -> r.getRoomStatus() == RoomStatus.OCCUPIED).count())
                    .dirtyCount((int) rooms.stream().filter(r -> r.getRoomStatus() == RoomStatus.DIRTY).count())
                    .maintenanceCount((int) rooms.stream().filter(r -> r.getRoomStatus() == RoomStatus.MAINTENANCE).count())
                    .disabledCount((int) rooms.stream().filter(r -> r.getRoomStatus() == RoomStatus.DISABLED).count())
                    .build();

            result.add(stats);
        }

        return result;
    }

    @Override
    public boolean checkRoomNoUnique(String roomNo) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getRoomNo, roomNo);
        return roomMapper.selectCount(wrapper) == 0;
    }

    @Override
    public boolean validateRoomNoAndFloor(String roomNo, Integer floor) {
        try {
            int roomNoInt = Integer.parseInt(roomNo);
            int roomFloor = roomNoInt / 100;
            int roomSuffix = roomNoInt % 100;

            return roomFloor == floor && roomSuffix >= 1 && roomSuffix <= 20;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private RoomVO convertToVO(Room room, RoomType roomType) {
        return RoomVO.builder()
                .id(room.getId())
                .roomNo(room.getRoomNo())
                .floor(room.getFloor())
                .roomTypeId(room.getRoomTypeId())
                .roomTypeName(roomType != null ? roomType.getName() : null)
                .roomStatus(room.getRoomStatus())
                .roomStatusText(getRoomStatusText(room.getRoomStatus()))
                .maintenanceReason(room.getMaintenanceReason())
                .nearElevator(Boolean.TRUE.equals(room.getNearElevator()))
                .headRoom(Boolean.TRUE.equals(room.getHeadRoom()))
                .tailRoom(Boolean.TRUE.equals(room.getTailRoom()))
                .cadImageUrl(room.getCadImageUrl())
                .remark(room.getRemark())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    private CurrentGuestInfo findCurrentGuestInfo(Room room) {
        if (room.getRoomStatus() != RoomStatus.OCCUPIED) {
            return CurrentGuestInfo.empty();
        }
        List<RoomAssignment> assignments = roomAssignmentMapper.selectActiveByRoomIds(
                List.of(room.getId()),
                Set.of(AssignmentStatus.CHECKED_IN)
        );
        if (assignments.isEmpty()) {
            return CurrentGuestInfo.empty();
        }

        RoomAssignment assignment = assignments.get(0);
        BookingOrder order = bookingOrderMapper.selectById(assignment.getOrderId());
        OrderGuest mainGuest = orderGuestMapper.selectOne(new LambdaQueryWrapper<OrderGuest>()
                .eq(OrderGuest::getOrderId, assignment.getOrderId())
                .orderByDesc(OrderGuest::getIsMain)
                .orderByAsc(OrderGuest::getId)
                .last("LIMIT 1"));

        String guestName = mainGuest != null ? mainGuest.getGuestName() : null;
        String guestPhone = mainGuest != null && mainGuest.getGuestPhone() != null
                ? mainGuest.getGuestPhone()
                : order != null ? order.getPhone() : null;

        return new CurrentGuestInfo(
                assignment.getOrderId(),
                order != null ? order.getOrderNo() : null,
                guestName,
                guestPhone
        );
    }

    private record CurrentGuestInfo(Long orderId, String orderNo, String guestName, String guestPhone) {
        private static CurrentGuestInfo empty() {
            return new CurrentGuestInfo(null, null, null, null);
        }
    }

    private String getRoomStatusText(RoomStatus status) {
        return switch (status) {
            case AVAILABLE -> "Available";
            case RESERVED -> "Reserved";
            case OCCUPIED -> "Occupied";
            case DIRTY -> "Dirty";
            case MAINTENANCE -> "Maintenance";
            case DISABLED -> "Disabled";
        };
    }
}
