package com.zhisu.hotel.frontdesk.service.impl;

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
import com.zhisu.hotel.frontdesk.vo.CheckinVerificationVO;
import com.zhisu.hotel.frontdesk.vo.FrontdeskOrderListVO;
import com.zhisu.hotel.room.entity.Room;
import com.zhisu.hotel.room.entity.RoomAssignment;
import com.zhisu.hotel.room.mapper.RoomAssignmentMapper;
import com.zhisu.hotel.room.mapper.RoomMapper;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FrontdeskOrderQueryServiceImplTest {

    private final BookingOrderMapper bookingOrderMapper = mock(BookingOrderMapper.class);
    private final OrderGuestMapper orderGuestMapper = mock(OrderGuestMapper.class);
    private final RoomMapper roomMapper = mock(RoomMapper.class);
    private final RoomAssignmentMapper roomAssignmentMapper = mock(RoomAssignmentMapper.class);
    private final RoomTypeMapper roomTypeMapper = mock(RoomTypeMapper.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-15T02:00:00Z"), ZoneId.of("Asia/Shanghai"));

    private FrontdeskOrderQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FrontdeskOrderQueryServiceImpl(
                bookingOrderMapper,
                orderGuestMapper,
                roomMapper,
                roomAssignmentMapper,
                roomTypeMapper,
                clock
        );
    }

    @Test
    void searchOrdersRejectsEmptyConditions() {
        FrontdeskOrderQueryRequest request = new FrontdeskOrderQueryRequest();

        BusinessException ex = assertThrows(BusinessException.class, () -> service.searchOrders(request));

        assertEquals(ErrorCode.FRONTDESK_SEARCH_CONDITION_REQUIRED, ex.getCode());
        verify(bookingOrderMapper, never()).selectPage(any(), any());
    }

    @Test
    void searchOrdersMasksPhoneAndReturnsPagedResult() {
        BookingOrder order = order(1L, PaymentStatus.PAID.name(), OrderStatus.PENDING_CHECK_IN.name(),
                LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 16), 1, 1L);
        Page<BookingOrder> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(order));
        when(bookingOrderMapper.selectPage(any(), any())).thenReturn(page);
        when(orderGuestMapper.selectList(any())).thenReturn(List.of(mainGuest(10L, 1L, "Alice", "13812345678")));

        FrontdeskOrderQueryRequest request = new FrontdeskOrderQueryRequest();
        request.setPhone(" 13812345678 ");
        PageResponse<FrontdeskOrderListVO> response = service.searchOrders(request);

        assertEquals(1, response.getTotal());
        assertEquals("138****5678", response.getRecords().get(0).getMaskedContactPhone());
        assertEquals("Alice", response.getRecords().get(0).getContactName());
    }

    @Test
    void verifyCheckinPassesWhenPaidPendingCheckinAndRoomsEnough() {
        BookingOrder order = order(1L, PaymentStatus.PAID.name(), OrderStatus.PENDING_CHECK_IN.name(),
                LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 16), 1, 1L);
        when(bookingOrderMapper.selectById(1L)).thenReturn(order);
        when(roomTypeMapper.selectById(1L)).thenReturn(roomType(1L, "Deluxe"));
        when(roomAssignmentMapper.selectCount(any())).thenReturn(0L);
        when(roomMapper.selectList(any())).thenReturn(List.of(room(101L, "301", 3, 1L, RoomStatus.AVAILABLE)));
        when(roomAssignmentMapper.selectList(any())).thenReturn(List.of());

        CheckinVerificationVO result = service.verifyCheckin(1L);

        assertTrue(result.getCanCheckIn());
        assertNull(result.getFailureCode());
        assertEquals(1, result.getAvailableRoomCount());
        assertEquals("301", result.getAllocatableRooms().get(0).getRoomNo());
    }

    @Test
    void verifyCheckinFailsWhenOrderUnpaid() {
        BookingOrder order = order(1L, PaymentStatus.UNPAID.name(), OrderStatus.PENDING_CHECK_IN.name(),
                LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 16), 1, 1L);
        stubValidRoomContext(order, 1);

        CheckinVerificationVO result = service.verifyCheckin(1L);

        assertFalse(result.getCanCheckIn());
        assertEquals(ErrorCode.ORDER_NOT_PAID, result.getFailureCode());
    }

    @Test
    void verifyCheckinFailsWhenTooEarly() {
        BookingOrder order = order(1L, PaymentStatus.PAID.name(), OrderStatus.PENDING_CHECK_IN.name(),
                LocalDate.of(2026, 7, 16), LocalDate.of(2026, 7, 17), 1, 1L);
        stubValidRoomContext(order, 1);

        CheckinVerificationVO result = service.verifyCheckin(1L);

        assertFalse(result.getCanCheckIn());
        assertEquals(ErrorCode.CHECKIN_TOO_EARLY, result.getFailureCode());
    }

    @Test
    void verifyCheckinFailsWhenExpired() {
        BookingOrder order = order(1L, PaymentStatus.PAID.name(), OrderStatus.PENDING_CHECK_IN.name(),
                LocalDate.of(2026, 7, 14), LocalDate.of(2026, 7, 15), 1, 1L);
        stubValidRoomContext(order, 1);

        CheckinVerificationVO result = service.verifyCheckin(1L);

        assertFalse(result.getCanCheckIn());
        assertEquals(ErrorCode.CHECKIN_EXPIRED, result.getFailureCode());
    }

    @Test
    void verifyCheckinFailsWhenRoomsNotEnough() {
        BookingOrder order = order(1L, PaymentStatus.PAID.name(), OrderStatus.PENDING_CHECK_IN.name(),
                LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 16), 2, 1L);
        stubValidRoomContext(order, 1);

        CheckinVerificationVO result = service.verifyCheckin(1L);

        assertFalse(result.getCanCheckIn());
        assertEquals(ErrorCode.ALLOCATABLE_ROOM_NOT_ENOUGH, result.getFailureCode());
        assertEquals(2, result.getRequiredRoomCount());
        assertEquals(1, result.getAvailableRoomCount());
    }

    @Test
    void verifyCheckinFiltersUnavailableOtherTypeAndAssignedRooms() {
        BookingOrder order = order(1L, PaymentStatus.PAID.name(), OrderStatus.PENDING_CHECK_IN.name(),
                LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 16), 1, 1L);
        when(bookingOrderMapper.selectById(1L)).thenReturn(order);
        when(roomTypeMapper.selectById(1L)).thenReturn(roomType(1L, "Deluxe"));
        when(roomAssignmentMapper.selectCount(any())).thenReturn(0L);
        Room available = room(101L, "301", 3, 1L, RoomStatus.AVAILABLE);
        Room maintenance = room(102L, "302", 3, 1L, RoomStatus.MAINTENANCE);
        Room otherType = room(103L, "303", 3, 2L, RoomStatus.AVAILABLE);
        Room assigned = room(104L, "304", 3, 1L, RoomStatus.AVAILABLE);
        when(roomMapper.selectList(any())).thenReturn(List.of(assigned, otherType, maintenance, available));
        when(roomAssignmentMapper.selectList(any())).thenReturn(List.of(RoomAssignment.builder()
                .orderId(99L)
                .roomId(104L)
                .assignmentStatus(AssignmentStatus.PENDING)
                .build()));

        CheckinVerificationVO result = service.verifyCheckin(1L);

        assertTrue(result.getCanCheckIn());
        assertEquals(1, result.getAvailableRoomCount());
        assertEquals("301", result.getAllocatableRooms().get(0).getRoomNo());
    }

    @Test
    void verifyCheckinReportsFullAssignment() {
        BookingOrder order = order(1L, PaymentStatus.PAID.name(), OrderStatus.PENDING_CHECK_IN.name(),
                LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 16), 1, 1L);
        when(bookingOrderMapper.selectById(1L)).thenReturn(order);
        when(roomTypeMapper.selectById(1L)).thenReturn(roomType(1L, "Deluxe"));
        when(roomAssignmentMapper.selectCount(any())).thenReturn(1L);
        when(roomMapper.selectList(any())).thenReturn(List.of(room(101L, "301", 3, 1L, RoomStatus.AVAILABLE)));
        when(roomAssignmentMapper.selectList(any())).thenReturn(List.of());

        CheckinVerificationVO result = service.verifyCheckin(1L);

        assertFalse(result.getCanCheckIn());
        assertEquals(ErrorCode.ORDER_ALREADY_ASSIGNED, result.getFailureCode());
        assertEquals(1, result.getAssignedRoomCount());
        assertEquals(0, result.getRemainingRoomCount());
    }

    private void stubValidRoomContext(BookingOrder order, int availableRooms) {
        when(bookingOrderMapper.selectById(order.getId())).thenReturn(order);
        when(roomTypeMapper.selectById(order.getRoomTypeId())).thenReturn(roomType(order.getRoomTypeId(), "Deluxe"));
        when(roomAssignmentMapper.selectCount(any())).thenReturn(0L);
        List<Room> rooms = java.util.stream.IntStream.range(0, availableRooms)
                .mapToObj(i -> room(100L + i, "30" + (i + 1), 3, order.getRoomTypeId(), RoomStatus.AVAILABLE))
                .toList();
        when(roomMapper.selectList(any())).thenReturn(rooms);
        when(roomAssignmentMapper.selectList(any())).thenReturn(List.of());
    }

    private BookingOrder order(Long id, String paymentStatus, String orderStatus,
                               LocalDate checkInDate, LocalDate checkOutDate,
                               Integer roomCount, Long roomTypeId) {
        return BookingOrder.builder()
                .id(id)
                .orderNo("BO202607150001")
                .roomTypeId(roomTypeId)
                .roomTypeName("Deluxe")
                .roomCount(roomCount)
                .guestCount(2)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .totalAmount(BigDecimal.valueOf(588))
                .phone("13812345678")
                .paymentStatus(paymentStatus)
                .orderStatus(orderStatus)
                .build();
    }

    private OrderGuest mainGuest(Long id, Long orderId, String name, String phone) {
        return OrderGuest.builder()
                .id(id)
                .orderId(orderId)
                .guestName(name)
                .guestPhone(phone)
                .isMain(1)
                .build();
    }

    private Room room(Long id, String roomNo, Integer floor, Long roomTypeId, RoomStatus status) {
        return Room.builder()
                .id(id)
                .roomNo(roomNo)
                .floor(floor)
                .roomTypeId(roomTypeId)
                .roomStatus(status)
                .isDeleted(0)
                .build();
    }

    private RoomType roomType(Long id, String name) {
        return RoomType.builder()
                .id(id)
                .name(name)
                .status("ONLINE")
                .build();
    }
}
