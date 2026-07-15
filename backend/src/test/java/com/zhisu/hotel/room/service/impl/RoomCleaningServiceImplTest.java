package com.zhisu.hotel.room.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
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
import com.zhisu.hotel.room.vo.CleaningResultVO;
import com.zhisu.hotel.room.vo.DirtyRoomVO;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoomCleaningServiceImplTest {

    private final RoomMapper roomMapper = mock(RoomMapper.class);
    private final RoomAssignmentMapper assignmentMapper = mock(RoomAssignmentMapper.class);
    private final RoomStatusLogMapper roomStatusLogMapper = mock(RoomStatusLogMapper.class);
    private final RoomTypeMapper roomTypeMapper = mock(RoomTypeMapper.class);
    private final BookingOrderMapper bookingOrderMapper = mock(BookingOrderMapper.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-15T04:00:00Z"), ZoneId.of("Asia/Shanghai"));

    private RoomCleaningServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RoomCleaningServiceImpl(roomMapper, assignmentMapper, roomStatusLogMapper,
                roomTypeMapper, bookingOrderMapper, clock);
    }

    @Test
    void listPendingDirtyRoomsEnrichesLatestCheckoutAndDirtyTime() {
        Room room = room(RoomStatus.DIRTY);
        Page<Room> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(room));
        when(roomMapper.pageDirtyRooms(any(Page.class), eq(3), eq(2L))).thenReturn(page);
        when(roomTypeMapper.selectBatchIds(anyCollection())).thenReturn(List.of(RoomType.builder().id(2L).name("Standard").build()));
        RoomAssignment assignment = assignment(301L, AssignmentStatus.CHECKED_OUT);
        assignment.setCheckOutTime(now().minusMinutes(15));
        when(assignmentMapper.selectCheckedOutByRoomIds(List.of(101L))).thenReturn(List.of(assignment));
        when(bookingOrderMapper.selectBatchIds(anyCollection())).thenReturn(List.of(BookingOrder.builder()
                .id(1L)
                .orderNo("M4S5-CHECKOUT-SINGLE")
                .build()));
        when(roomStatusLogMapper.selectDirtyLogsByRoomIds(List.of(101L))).thenReturn(List.of(RoomStatusLog.builder()
                .roomId(101L)
                .afterStatus(RoomStatus.DIRTY)
                .createdAt(now().minusMinutes(10))
                .build()));

        PageResponse<DirtyRoomVO> result = service.listPending(3, 2L, 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals("Standard", result.getRecords().get(0).getRoomTypeName());
        assertEquals("M4S5-CHECKOUT-SINGLE", result.getRecords().get(0).getLatestOrderNo());
        assertEquals(RoomStatus.DIRTY.name(), result.getRecords().get(0).getRoomStatus());
    }

    @Test
    void completeCleaningMovesDirtyRoomToAvailableAndWritesLog() {
        Room room = room(RoomStatus.DIRTY);
        when(roomMapper.selectByIdForUpdate(101L)).thenReturn(room);
        when(assignmentMapper.selectActiveByRoomIds(eq(List.of(101L)), anySet())).thenReturn(List.of());
        when(roomMapper.markAvailableAfterCleaning(101L, 0, now())).thenReturn(1);
        when(roomStatusLogMapper.insert(any(RoomStatusLog.class))).thenReturn(1);

        CleaningResultVO result = service.completeCleaning(101L, completeRequest());

        assertEquals(RoomStatus.AVAILABLE.name(), result.getCurrentStatus());
        verify(roomMapper).markAvailableAfterCleaning(101L, 0, now());
        verify(roomStatusLogMapper).insert(any(RoomStatusLog.class));
    }

    @Test
    void completeCleaningIsIdempotentWhenRoomAlreadyAvailable() {
        when(roomMapper.selectByIdForUpdate(101L)).thenReturn(room(RoomStatus.AVAILABLE));

        CleaningResultVO result = service.completeCleaning(101L, completeRequest());

        assertEquals("Room has already been cleaned", result.getMessage());
        verify(roomMapper, never()).markAvailableAfterCleaning(anyLong(), anyInt(), any());
        verify(roomStatusLogMapper, never()).insert(any(RoomStatusLog.class));
    }

    @Test
    void reportIssueMovesDirtyRoomToMaintenanceWithReason() {
        Room room = room(RoomStatus.DIRTY);
        when(roomMapper.selectByIdForUpdate(101L)).thenReturn(room);
        when(assignmentMapper.selectActiveByRoomIds(eq(List.of(101L)), anySet())).thenReturn(List.of());
        when(roomMapper.markMaintenanceFromDirty(101L, 0, "AC leak", now())).thenReturn(1);
        when(roomStatusLogMapper.insert(any(RoomStatusLog.class))).thenReturn(1);

        CleaningResultVO result = service.reportIssue(101L, issueRequest("AC leak"));

        assertEquals(RoomStatus.MAINTENANCE.name(), result.getCurrentStatus());
        assertEquals("AC leak", result.getMaintenanceReason());
        verify(roomMapper).markMaintenanceFromDirty(101L, 0, "AC leak", now());
    }

    @Test
    void cleaningNonDirtyRoomIsRejected() {
        when(roomMapper.selectByIdForUpdate(101L)).thenReturn(room(RoomStatus.OCCUPIED));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.completeCleaning(101L, completeRequest()));

        assertEquals(ErrorCode.CLEANING_NOT_ALLOWED, ex.getCode());
        verify(roomMapper, never()).markAvailableAfterCleaning(anyLong(), anyInt(), any());
    }

    @Test
    void reportIssueRequiresReason() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.reportIssue(101L, issueRequest(" ")));

        assertEquals(ErrorCode.CLEANING_ISSUE_REASON_REQUIRED, ex.getCode());
        verifyNoInteractions(roomMapper);
    }

    private CompleteCleaningRequest completeRequest() {
        CompleteCleaningRequest request = new CompleteCleaningRequest();
        request.setRequestToken("M4S5-CLEANING-COMPLETE");
        request.setRemark("cleaned");
        return request;
    }

    private ReportCleaningIssueRequest issueRequest(String reason) {
        ReportCleaningIssueRequest request = new ReportCleaningIssueRequest();
        request.setRequestToken("M4S5-CLEANING-ISSUE");
        request.setReason(reason);
        request.setRemark("needs repair");
        return request;
    }

    private Room room(RoomStatus status) {
        return Room.builder()
                .id(101L)
                .roomNo("311")
                .floor(3)
                .roomTypeId(2L)
                .roomStatus(status)
                .version(0)
                .isDeleted(0)
                .build();
    }

    private RoomAssignment assignment(Long id, AssignmentStatus status) {
        return RoomAssignment.builder()
                .id(id)
                .orderId(1L)
                .roomId(101L)
                .assignmentStatus(status)
                .version(0)
                .build();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
