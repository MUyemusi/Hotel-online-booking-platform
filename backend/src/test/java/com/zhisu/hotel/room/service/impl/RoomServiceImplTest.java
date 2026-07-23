package com.zhisu.hotel.room.service.impl;

import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.booking.mapper.OrderGuestMapper;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.room.dto.RoomCreateRequest;
import com.zhisu.hotel.room.dto.RoomStatusChangeRequest;
import com.zhisu.hotel.room.entity.Room;
import com.zhisu.hotel.room.mapper.RoomAssignmentMapper;
import com.zhisu.hotel.room.entity.RoomStatusLog;
import com.zhisu.hotel.room.mapper.RoomMapper;
import com.zhisu.hotel.room.mapper.RoomStatusLogMapper;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoomServiceImplTest {

    private final RoomMapper roomMapper = mock(RoomMapper.class);
    private final RoomStatusLogMapper roomStatusLogMapper = mock(RoomStatusLogMapper.class);
    private final RoomTypeMapper roomTypeMapper = mock(RoomTypeMapper.class);
    private final RoomAssignmentMapper roomAssignmentMapper = mock(RoomAssignmentMapper.class);
    private final OrderGuestMapper orderGuestMapper = mock(OrderGuestMapper.class);
    private final BookingOrderMapper bookingOrderMapper = mock(BookingOrderMapper.class);
    private final RoomServiceImpl roomService = new RoomServiceImpl(
            roomMapper,
            roomStatusLogMapper,
            roomTypeMapper,
            roomAssignmentMapper,
            orderGuestMapper,
            bookingOrderMapper
    );

    @Test
    void validateRoomNoAndFloorAcceptsValidRoomNumber() {
        assertTrue(roomService.validateRoomNoAndFloor("301", 3));
    }

    @Test
    void validateRoomNoAndFloorRejectsMismatchedFloor() {
        assertFalse(roomService.validateRoomNoAndFloor("803", 7));
    }

    @Test
    void validateRoomNoAndFloorRejectsIllegalRoomNumbers() {
        assertFalse(roomService.validateRoomNoAndFloor("300", 3));
        assertFalse(roomService.validateRoomNoAndFloor("321", 3));
        assertFalse(roomService.validateRoomNoAndFloor("900", 9));
    }

    @Test
    void createRoomRejectsDuplicateRoomNumber() {
        when(roomMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> roomService.createRoom(RoomCreateRequest.builder()
                .roomNo("803")
                .floor(8)
                .roomTypeId(1L)
                .build()));

        verify(roomMapper, never()).insert(any(Room.class));
    }

    @Test
    void changeRoomStatusMovesAvailableToMaintenanceAndWritesLog() {
        Room room = room(1L, "803", RoomStatus.AVAILABLE, null, 4);
        when(roomMapper.selectById(1L)).thenReturn(room);
        when(roomMapper.updateStatusWithCondition(1L, RoomStatus.AVAILABLE, RoomStatus.MAINTENANCE,
                "AC repair", 4)).thenReturn(1);
        when(roomStatusLogMapper.insert(any(RoomStatusLog.class))).thenReturn(1);

        roomService.changeRoomStatus(1L, RoomStatusChangeRequest.builder()
                .targetStatus(RoomStatus.MAINTENANCE)
                .reason("AC repair")
                .build());

        ArgumentCaptor<RoomStatusLog> logCaptor = ArgumentCaptor.forClass(RoomStatusLog.class);
        verify(roomStatusLogMapper).insert(logCaptor.capture());
        assertEquals(RoomStatus.AVAILABLE, logCaptor.getValue().getBeforeStatus());
        assertEquals(RoomStatus.MAINTENANCE, logCaptor.getValue().getAfterStatus());
        assertEquals("AC repair", logCaptor.getValue().getReason());
    }

    @Test
    void changeRoomStatusRequiresReasonForMaintenance() {
        when(roomMapper.selectById(1L)).thenReturn(room(1L, "803", RoomStatus.AVAILABLE, null, 0));

        assertThrows(BusinessException.class, () -> roomService.changeRoomStatus(1L,
                RoomStatusChangeRequest.builder().targetStatus(RoomStatus.MAINTENANCE).reason(" ").build()));

        verify(roomMapper, never()).updateStatusWithCondition(anyLong(), any(), any(), any(), anyInt());
        verify(roomStatusLogMapper, never()).insert(any(RoomStatusLog.class));
    }

    @Test
    void changeRoomStatusClearsMaintenanceReasonWhenRestoredToAvailable() {
        Room room = room(1L, "803", RoomStatus.MAINTENANCE, "AC repair", 5);
        when(roomMapper.selectById(1L)).thenReturn(room);
        when(roomMapper.updateStatusWithCondition(1L, RoomStatus.MAINTENANCE, RoomStatus.AVAILABLE,
                null, 5)).thenReturn(1);
        when(roomStatusLogMapper.insert(any(RoomStatusLog.class))).thenReturn(1);

        roomService.changeRoomStatus(1L, RoomStatusChangeRequest.builder()
                .targetStatus(RoomStatus.AVAILABLE)
                .reason("Repair complete")
                .build());

        verify(roomMapper).updateStatusWithCondition(1L, RoomStatus.MAINTENANCE, RoomStatus.AVAILABLE,
                null, 5);
    }

    @Test
    void changeRoomStatusRejectsProtectedCurrentStatus() {
        when(roomMapper.selectById(1L)).thenReturn(room(1L, "803", RoomStatus.OCCUPIED, null, 2));

        assertThrows(BusinessException.class, () -> roomService.changeRoomStatus(1L,
                RoomStatusChangeRequest.builder().targetStatus(RoomStatus.AVAILABLE).reason("done").build()));

        verify(roomMapper, never()).updateStatusWithCondition(anyLong(), any(), any(), any(), anyInt());
        verify(roomStatusLogMapper, never()).insert(any(RoomStatusLog.class));
    }

    @Test
    void changeRoomStatusThrowsWhenConditionalUpdateAffectsNoRows() {
        when(roomMapper.selectById(1L)).thenReturn(room(1L, "803", RoomStatus.AVAILABLE, null, 3));
        when(roomMapper.updateStatusWithCondition(anyLong(), any(), any(), any(), anyInt())).thenReturn(0);

        assertThrows(BusinessException.class, () -> roomService.changeRoomStatus(1L,
                RoomStatusChangeRequest.builder().targetStatus(RoomStatus.DISABLED).reason("offline").build()));

        verify(roomStatusLogMapper, never()).insert(any(RoomStatusLog.class));
    }

    @Test
    void changeRoomStatusPropagatesLogInsertFailureForTransactionalRollback() {
        when(roomMapper.selectById(1L)).thenReturn(room(1L, "803", RoomStatus.AVAILABLE, null, 6));
        when(roomMapper.updateStatusWithCondition(anyLong(), any(), any(), any(), anyInt())).thenReturn(1);
        when(roomStatusLogMapper.insert(any(RoomStatusLog.class))).thenThrow(new RuntimeException("insert failed"));

        assertThrows(RuntimeException.class, () -> roomService.changeRoomStatus(1L,
                RoomStatusChangeRequest.builder().targetStatus(RoomStatus.DISABLED).reason("offline").build()));
    }

    @Test
    void changeRoomStatusIsTransactional() throws NoSuchMethodException {
        Method method = RoomServiceImpl.class.getMethod("changeRoomStatus", Long.class, RoomStatusChangeRequest.class);
        assertNotNull(method.getAnnotation(org.springframework.transaction.annotation.Transactional.class));
    }

    private static Room room(Long id, String roomNo, RoomStatus status, String maintenanceReason, Integer version) {
        return Room.builder()
                .id(id)
                .roomNo(roomNo)
                .floor(Integer.parseInt(roomNo) / 100)
                .roomTypeId(1L)
                .roomStatus(status)
                .maintenanceReason(maintenanceReason)
                .version(version)
                .isDeleted(0)
                .build();
    }
}
