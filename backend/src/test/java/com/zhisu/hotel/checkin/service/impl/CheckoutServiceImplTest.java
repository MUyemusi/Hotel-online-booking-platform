package com.zhisu.hotel.checkin.service.impl;

import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.checkin.dto.ConfirmCheckoutRequest;
import com.zhisu.hotel.checkin.vo.ConfirmCheckoutVO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CheckoutServiceImplTest {

    private final BookingOrderMapper bookingOrderMapper = mock(BookingOrderMapper.class);
    private final RoomAssignmentMapper assignmentMapper = mock(RoomAssignmentMapper.class);
    private final RoomMapper roomMapper = mock(RoomMapper.class);
    private final RoomStatusLogMapper roomStatusLogMapper = mock(RoomStatusLogMapper.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-15T03:00:00Z"), ZoneId.of("Asia/Shanghai"));

    private CheckoutServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CheckoutServiceImpl(bookingOrderMapper, assignmentMapper, roomMapper, roomStatusLogMapper, clock);
    }

    @Test
    void confirmCheckoutMarksAssignmentsRoomsAndOrder() {
        BookingOrder order = order(OrderStatus.CHECKED_IN.name(), 1);
        RoomAssignment assignment = assignment(301L, 101L, AssignmentStatus.CHECKED_IN);
        Room room = room(101L, RoomStatus.OCCUPIED);
        stubCheckout(order, List.of(assignment), List.of(room));

        ConfirmCheckoutVO result = service.confirmCheckout(1L, request("M4S5-CHECKOUT-REQ"));

        assertEquals(OrderStatus.COMPLETED.name(), result.getOrderStatus());
        assertEquals(1, result.getCheckedOutRoomCount());
        assertEquals(RoomStatus.DIRTY.name(), result.getCheckedOutRooms().get(0).getCurrentRoomStatus());
        verify(assignmentMapper).markCheckedOutWithVersion(eq(301L), eq(0), any(), any());
        verify(roomMapper).markDirtyForCheckout(eq(101L), eq(0), any());
        verify(roomStatusLogMapper).insert(any(RoomStatusLog.class));
        verify(bookingOrderMapper).markCompletedIfCheckedIn(eq(1L), any());
    }

    @Test
    void completedOrderReturnsIdempotentResultWithoutWrites() {
        BookingOrder order = order(OrderStatus.COMPLETED.name(), 1);
        RoomAssignment assignment = assignment(301L, 101L, AssignmentStatus.CHECKED_OUT);
        assignment.setCheckOutTime(now().minusMinutes(5));
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order);
        when(assignmentMapper.selectByOrderIdForUpdate(1L)).thenReturn(List.of(assignment));
        when(roomMapper.selectByIdsForUpdate(List.of(101L))).thenReturn(List.of(room(101L, RoomStatus.DIRTY)));

        ConfirmCheckoutVO result = service.confirmCheckout(1L, request("M4S5-IDEMPOTENT"));

        assertEquals("Order has already checked out", result.getMessage());
        verify(assignmentMapper, never()).markCheckedOutWithVersion(anyLong(), anyInt(), any(), any());
        verify(roomMapper, never()).markDirtyForCheckout(anyLong(), anyInt(), any());
        verify(bookingOrderMapper, never()).markCompletedIfCheckedIn(anyLong(), any());
    }

    @Test
    void pendingCheckinOrderIsRejected() {
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order(OrderStatus.PENDING_CHECK_IN.name(), 1));
        when(assignmentMapper.selectByOrderIdForUpdate(1L)).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmCheckout(1L, request("M4S5-PENDING")));

        assertEquals(ErrorCode.ORDER_NOT_CHECKED_IN, ex.getCode());
        verify(roomMapper, never()).selectByIdsForUpdate(anyList());
    }

    @Test
    void partialCheckedOutAssignmentsAreRejected() {
        BookingOrder order = order(OrderStatus.CHECKED_IN.name(), 2);
        RoomAssignment checkedIn = assignment(301L, 101L, AssignmentStatus.CHECKED_IN);
        RoomAssignment checkedOut = assignment(302L, 102L, AssignmentStatus.CHECKED_OUT);
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order);
        when(assignmentMapper.selectByOrderIdForUpdate(1L)).thenReturn(List.of(checkedIn, checkedOut));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmCheckout(1L, request("M4S5-PARTIAL")));

        assertEquals(ErrorCode.CHECKOUT_PARTIAL_STATE_CONFLICT, ex.getCode());
    }

    @Test
    void nonOccupiedRoomIsRejectedBeforeWrites() {
        BookingOrder order = order(OrderStatus.CHECKED_IN.name(), 1);
        RoomAssignment assignment = assignment(301L, 101L, AssignmentStatus.CHECKED_IN);
        stubCheckout(order, List.of(assignment), List.of(room(101L, RoomStatus.AVAILABLE)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmCheckout(1L, request("M4S5-BAD-ROOM")));

        assertEquals(ErrorCode.CHECKOUT_ROOM_NOT_OCCUPIED, ex.getCode());
        verify(assignmentMapper, never()).markCheckedOutWithVersion(anyLong(), anyInt(), any(), any());
    }

    @Test
    void roomStatusUpdateConflictStopsOrderCompletion() {
        BookingOrder order = order(OrderStatus.CHECKED_IN.name(), 1);
        RoomAssignment assignment = assignment(301L, 101L, AssignmentStatus.CHECKED_IN);
        stubCheckout(order, List.of(assignment), List.of(room(101L, RoomStatus.OCCUPIED)));
        when(roomMapper.markDirtyForCheckout(eq(101L), eq(0), any())).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmCheckout(1L, request("M4S5-CONFLICT")));

        assertEquals(ErrorCode.CHECKOUT_ROOM_STATUS_CONFLICT, ex.getCode());
        verify(bookingOrderMapper, never()).markCompletedIfCheckedIn(anyLong(), any());
    }

    private void stubCheckout(BookingOrder order, List<RoomAssignment> assignments, List<Room> rooms) {
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order);
        when(assignmentMapper.selectByOrderIdForUpdate(1L)).thenReturn(assignments);
        when(roomMapper.selectByIdsForUpdate(assignments.stream().map(RoomAssignment::getRoomId).sorted().toList()))
                .thenReturn(rooms);
        for (RoomAssignment assignment : assignments) {
            when(assignmentMapper.markCheckedOutWithVersion(eq(assignment.getId()), eq(assignment.getVersion()), any(), any()))
                    .thenReturn(1);
        }
        for (Room room : rooms) {
            when(roomMapper.markDirtyForCheckout(eq(room.getId()), eq(room.getVersion()), any())).thenReturn(1);
        }
        when(roomStatusLogMapper.insert(any(RoomStatusLog.class))).thenReturn(1);
        when(bookingOrderMapper.markCompletedIfCheckedIn(eq(1L), any())).thenReturn(1);
    }

    private ConfirmCheckoutRequest request(String token) {
        ConfirmCheckoutRequest request = new ConfirmCheckoutRequest();
        request.setRequestToken(token);
        request.setRemark("frontdesk checkout");
        return request;
    }

    private BookingOrder order(String orderStatus, int roomCount) {
        return BookingOrder.builder()
                .id(1L)
                .orderNo("M4S5-CHECKOUT")
                .roomTypeId(2L)
                .roomTypeName("Standard")
                .roomCount(roomCount)
                .checkInDate(LocalDate.of(2026, 7, 15))
                .checkOutDate(LocalDate.of(2026, 7, 17))
                .orderStatus(orderStatus)
                .paymentStatus(PaymentStatus.PAID.name())
                .build();
    }

    private RoomAssignment assignment(Long id, Long roomId, AssignmentStatus status) {
        return RoomAssignment.builder()
                .id(id)
                .orderId(1L)
                .roomId(roomId)
                .assignmentStatus(status)
                .checkInTime(now().minusHours(1))
                .version(0)
                .build();
    }

    private Room room(Long id, RoomStatus status) {
        return Room.builder()
                .id(id)
                .roomNo("311")
                .floor(3)
                .roomTypeId(2L)
                .roomStatus(status)
                .version(0)
                .isDeleted(0)
                .build();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
