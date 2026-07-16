package com.zhisu.hotel.checkin.service.impl;

import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.checkin.dto.ConfirmCheckinRequest;
import com.zhisu.hotel.checkin.util.CheckinQrTokenHasher;
import com.zhisu.hotel.checkin.vo.ConfirmCheckinVO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CheckinConfirmationServiceImplTest {

    private static final String PLAIN_TOKEN = "abcdefghijklmnopqrstuvwxyzABCDE_0123456789";

    private final BookingOrderMapper bookingOrderMapper = mock(BookingOrderMapper.class);
    private final RoomMapper roomMapper = mock(RoomMapper.class);
    private final RoomAssignmentMapper assignmentMapper = mock(RoomAssignmentMapper.class);
    private final RoomStatusLogMapper roomStatusLogMapper = mock(RoomStatusLogMapper.class);
    private final CheckinQrTokenMapper tokenMapper = mock(CheckinQrTokenMapper.class);
    private final CheckinQrTokenHasher hasher = new CheckinQrTokenHasher();
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-15T02:00:00Z"), ZoneId.of("Asia/Shanghai"));

    private CheckinConfirmationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CheckinConfirmationServiceImpl(
                bookingOrderMapper,
                roomMapper,
                assignmentMapper,
                roomStatusLogMapper,
                tokenMapper,
                hasher,
                clock
        );
    }

    @Test
    void confirmSingleRoomSucceedsAndConsumesTokenLast() {
        BookingOrder order = order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1);
        CheckinQrToken token = activeToken();
        Room room = room(101L, RoomStatus.AVAILABLE);
        stubSuccessfulCheckin(order, token, List.of(room));

        ConfirmCheckinVO result = service.confirm(1L, request(List.of(101L)));

        assertEquals(OrderStatus.CHECKED_IN.name(), result.getOrderStatus());
        assertEquals(QrTokenStatus.USED.name(), result.getTokenStatus());
        assertEquals(PaymentStatus.PAID.name(), result.getPaymentStatus());
        assertEquals(1, result.getAssignedRoomCount());
        assertEquals(101L, result.getAssignedRooms().get(0).getRoomId());
        verify(roomMapper).markOccupiedForCheckin(eq(101L), eq(0), any());
        verify(roomStatusLogMapper).insert(any(RoomStatusLog.class));
        verify(tokenMapper).markUsedForCheckinWithVersion(eq(9L), eq(1L), eq(0), any());
    }

    @Test
    void duplicateRoomIdsAreRejectedBeforeWrites() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirm(1L, request(List.of(101L, 101L))));

        assertEquals(ErrorCode.ROOM_SELECTION_DUPLICATE, ex.getCode());
        verifyNoInteractions(bookingOrderMapper, roomMapper, assignmentMapper, roomStatusLogMapper, tokenMapper);
    }

    @Test
    void alreadyCheckedInSameRoomsReturnsExistingResultWithoutWrites() {
        BookingOrder order = order(OrderStatus.CHECKED_IN.name(), PaymentStatus.PAID.name(), 1);
        CheckinQrToken token = activeToken();
        token.setTokenStatus(QrTokenStatus.USED);
        RoomAssignment assignment = RoomAssignment.builder()
                .id(301L)
                .orderId(1L)
                .roomId(101L)
                .assignmentStatus(AssignmentStatus.CHECKED_IN)
                .assignedAt(now())
                .checkInTime(now())
                .build();
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order);
        when(tokenMapper.selectByTokenHashForUpdate(hasher.hash(PLAIN_TOKEN))).thenReturn(token);
        when(assignmentMapper.selectActiveByOrderId(eq(1L), anySet())).thenReturn(List.of(assignment));
        when(roomMapper.selectBatchIds(List.of(101L))).thenReturn(List.of(room(101L, RoomStatus.OCCUPIED)));

        ConfirmCheckinVO result = service.confirm(1L, request(List.of(101L)));

        assertEquals("Order has already checked in", result.getMessage());
        assertEquals(1, result.getAssignedRoomCount());
        verify(assignmentMapper, never()).insert(any(RoomAssignment.class));
        verify(roomMapper, never()).markOccupiedForCheckin(anyLong(), anyInt(), any());
        verify(roomStatusLogMapper, never()).insert(any(RoomStatusLog.class));
        verify(tokenMapper, never()).markUsedForCheckinWithVersion(anyLong(), anyLong(), anyInt(), any());
    }

    @Test
    void alreadyCheckedInDifferentRoomsFails() {
        BookingOrder order = order(OrderStatus.CHECKED_IN.name(), PaymentStatus.PAID.name(), 1);
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order);
        when(tokenMapper.selectByTokenHashForUpdate(hasher.hash(PLAIN_TOKEN))).thenReturn(activeToken());
        when(assignmentMapper.selectActiveByOrderId(eq(1L), anySet())).thenReturn(List.of(RoomAssignment.builder()
                .orderId(1L)
                .roomId(101L)
                .assignmentStatus(AssignmentStatus.CHECKED_IN)
                .build()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirm(1L, request(List.of(102L))));

        assertEquals(ErrorCode.ORDER_ALREADY_CHECKED_IN, ex.getCode());
    }

    @Test
    void alreadyCheckedInDifferentRequestTokenFails() {
        BookingOrder order = order(OrderStatus.CHECKED_IN.name(), PaymentStatus.PAID.name(), 1);
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order);
        when(tokenMapper.selectByTokenHashForUpdate(hasher.hash(PLAIN_TOKEN))).thenReturn(activeToken());
        when(assignmentMapper.selectActiveByOrderId(eq(1L), anySet())).thenReturn(List.of(RoomAssignment.builder()
                .orderId(1L)
                .roomId(101L)
                .requestToken("OTHER-REQUEST")
                .assignmentStatus(AssignmentStatus.CHECKED_IN)
                .build()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirm(1L, request(List.of(101L))));

        assertEquals(ErrorCode.CHECKIN_CONFLICT, ex.getCode());
    }

    @Test
    void unpaidOrderIsRejectedBeforeRoomWrites() {
        assertPendingOrderFailure(order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.UNPAID.name(), 1),
                ErrorCode.ORDER_NOT_PAID);
    }

    @Test
    void nonPendingCheckinOrderIsRejected() {
        assertPendingOrderFailure(order(OrderStatus.CANCELLED.name(), PaymentStatus.PAID.name(), 1),
                ErrorCode.ORDER_NOT_PENDING_CHECKIN);
    }

    @Test
    void tooEarlyOrderIsRejected() {
        BookingOrder order = order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1);
        order.setCheckInDate(LocalDate.of(2026, 7, 16));
        assertPendingOrderFailure(order, ErrorCode.CHECKIN_TOO_EARLY);
    }

    @Test
    void expiredCheckinWindowIsRejected() {
        BookingOrder order = order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1);
        order.setCheckOutDate(LocalDate.of(2026, 7, 15));
        assertPendingOrderFailure(order, ErrorCode.CHECKIN_EXPIRED);
    }

    @Test
    void invalidQrFormatIsRejectedBeforeDatabaseLookup() {
        ConfirmCheckinRequest request = request(List.of(101L));
        request.setQrContent("BAD");
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(
                order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.confirm(1L, request));

        assertEquals(ErrorCode.QR_FORMAT_INVALID, ex.getCode());
        verify(tokenMapper, never()).selectByTokenHashForUpdate(anyString());
    }

    @Test
    void missingQrTokenIsRejected() {
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1));
        when(tokenMapper.selectByTokenHashForUpdate(hasher.hash(PLAIN_TOKEN))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));

        assertEquals(ErrorCode.QR_INVALID, ex.getCode());
    }

    @Test
    void expiredRevokedAndUsedTokensAreRejected() {
        assertTokenStatusFailure(QrTokenStatus.EXPIRED, ErrorCode.QR_EXPIRED);
        reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
        assertTokenStatusFailure(QrTokenStatus.REVOKED, ErrorCode.QR_REVOKED);
        reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
        assertTokenStatusFailure(QrTokenStatus.USED, ErrorCode.QR_ALREADY_USED);
    }

    @Test
    void qrBoundToOtherOrderIsRejected() {
        CheckinQrToken token = activeToken();
        token.setOrderId(99L);
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1));
        when(tokenMapper.selectByTokenHashForUpdate(hasher.hash(PLAIN_TOKEN))).thenReturn(token);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));

        assertEquals(ErrorCode.QR_ORDER_MISMATCH, ex.getCode());
    }

    @Test
    void emptyAndMismatchedRoomCountsAreRejected() {
        BusinessException empty = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of())));
        assertEquals(ErrorCode.ROOM_SELECTION_COUNT_MISMATCH, empty.getCode());

        BookingOrder twoRooms = order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 2);
        stubOrderAndToken(twoRooms, activeToken(), List.of());
        BusinessException less = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));
        assertEquals(ErrorCode.ROOM_SELECTION_COUNT_MISMATCH, less.getCode());

        reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
        stubOrderAndToken(order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1), activeToken(), List.of());
        BusinessException more = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L, 102L))));
        assertEquals(ErrorCode.ROOM_SELECTION_COUNT_MISMATCH, more.getCode());
    }

    @Test
    void roomNotFoundAndTypeMismatchAreRejected() {
        BookingOrder order = order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1);
        stubOrderAndToken(order, activeToken(), List.of());
        when(roomMapper.selectByIdsForUpdate(List.of(101L))).thenReturn(List.of());
        BusinessException notFound = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, notFound.getCode());

        reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
        stubOrderAndToken(order, activeToken(), List.of());
        Room otherType = room(101L, RoomStatus.AVAILABLE);
        otherType.setRoomTypeId(3L);
        when(roomMapper.selectByIdsForUpdate(List.of(101L))).thenReturn(List.of(otherType));
        BusinessException mismatch = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));
        assertEquals(ErrorCode.ROOM_TYPE_MISMATCH, mismatch.getCode());
    }

    @Test
    void unavailableRoomStatusesAreRejected() {
        for (RoomStatus status : List.of(RoomStatus.RESERVED, RoomStatus.OCCUPIED, RoomStatus.DIRTY,
                RoomStatus.MAINTENANCE, RoomStatus.DISABLED)) {
            reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
            stubOrderAndToken(order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1), activeToken(), List.of());
            when(roomMapper.selectByIdsForUpdate(List.of(101L))).thenReturn(List.of(room(101L, status)));

            BusinessException ex = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));

            assertEquals(ErrorCode.ROOM_NOT_AVAILABLE, ex.getCode());
        }
    }

    @Test
    void roomWithActiveAssignmentAndPartialOrderAssignmentsAreRejected() {
        BookingOrder order = order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1);
        stubOrderAndToken(order, activeToken(), List.of());
        when(roomMapper.selectByIdsForUpdate(List.of(101L))).thenReturn(List.of(room(101L, RoomStatus.AVAILABLE)));
        when(assignmentMapper.selectActiveByRoomIds(eq(List.of(101L)), anySet())).thenReturn(List.of(RoomAssignment.builder()
                .orderId(99L)
                .roomId(101L)
                .assignmentStatus(AssignmentStatus.CHECKED_IN)
                .build()));
        BusinessException roomAssigned = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));
        assertEquals(ErrorCode.ROOM_ALREADY_ASSIGNED, roomAssigned.getCode());

        reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
        BookingOrder twoRooms = order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 2);
        stubOrderAndToken(twoRooms, activeToken(), List.of(RoomAssignment.builder()
                .orderId(1L)
                .roomId(101L)
                .assignmentStatus(AssignmentStatus.CHECKED_IN)
                .build()));
        BusinessException partial = assertThrows(BusinessException.class,
                () -> service.confirm(1L, request(List.of(101L, 102L))));
        assertEquals(ErrorCode.ORDER_PARTIALLY_ASSIGNED, partial.getCode());
    }

    @Test
    void multiRoomSuccessCreatesTwoAssignmentsLogsAndOccupiesBothRooms() {
        BookingOrder order = order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 2);
        stubSuccessfulCheckin(order, activeToken(), List.of(room(101L, RoomStatus.AVAILABLE), room(102L, RoomStatus.AVAILABLE)));

        ConfirmCheckinVO result = service.confirm(1L, request(List.of(102L, 101L)));

        assertEquals(2, result.getAssignedRoomCount());
        verify(assignmentMapper, times(2)).insert(any(RoomAssignment.class));
        verify(roomMapper).markOccupiedForCheckin(eq(101L), eq(0), any());
        verify(roomMapper).markOccupiedForCheckin(eq(102L), eq(0), any());
        verify(roomStatusLogMapper, times(2)).insert(any(RoomStatusLog.class));
    }

    @Test
    void writeFailuresRaiseBusinessErrorsAndStopLaterWrites() {
        BookingOrder order = order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1);
        stubSuccessfulCheckin(order, activeToken(), List.of(room(101L, RoomStatus.AVAILABLE)));
        when(assignmentMapper.insert(any(RoomAssignment.class))).thenReturn(0);
        BusinessException assignmentFail = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));
        assertEquals(ErrorCode.CHECKIN_ASSIGNMENT_FAILED, assignmentFail.getCode());
        verify(roomMapper, never()).markOccupiedForCheckin(anyLong(), anyInt(), any());

        reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
        stubSuccessfulCheckin(order, activeToken(), List.of(room(101L, RoomStatus.AVAILABLE)));
        when(roomMapper.markOccupiedForCheckin(eq(101L), eq(0), any())).thenReturn(0);
        BusinessException roomFail = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));
        assertEquals(ErrorCode.CHECKIN_ROOM_UPDATE_FAILED, roomFail.getCode());
        verify(bookingOrderMapper, never()).markCheckedInIfPending(anyLong(), any());

        reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
        stubSuccessfulCheckin(order, activeToken(), List.of(room(101L, RoomStatus.AVAILABLE)));
        when(roomStatusLogMapper.insert(any(RoomStatusLog.class))).thenReturn(0);
        BusinessException logFail = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));
        assertEquals(ErrorCode.CHECKIN_CONFLICT, logFail.getCode());

        reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
        stubSuccessfulCheckin(order, activeToken(), List.of(room(101L, RoomStatus.AVAILABLE)));
        when(bookingOrderMapper.markCheckedInIfPending(eq(1L), any())).thenReturn(0);
        BusinessException orderFail = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));
        assertEquals(ErrorCode.CHECKIN_ORDER_UPDATE_FAILED, orderFail.getCode());

        reset(bookingOrderMapper, tokenMapper, roomMapper, assignmentMapper, roomStatusLogMapper);
        stubSuccessfulCheckin(order, activeToken(), List.of(room(101L, RoomStatus.AVAILABLE)));
        when(tokenMapper.markUsedForCheckinWithVersion(eq(9L), eq(1L), eq(0), any())).thenReturn(0);
        BusinessException tokenFail = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));
        assertEquals(ErrorCode.QR_TOKEN_CONSUME_NOT_ALLOWED, tokenFail.getCode());
    }

    private ConfirmCheckinRequest request(List<Long> roomIds) {
        ConfirmCheckinRequest request = new ConfirmCheckinRequest();
        request.setQrContent(CheckinQrServiceImpl.QR_PREFIX + PLAIN_TOKEN);
        request.setRoomIds(roomIds);
        request.setRequestToken("M4S4-REQUEST-001");
        return request;
    }

    private BookingOrder order(String orderStatus, String paymentStatus, int roomCount) {
        return BookingOrder.builder()
                .id(1L)
                .orderNo("M4S4-CHECKIN-SINGLE")
                .roomTypeId(2L)
                .roomTypeName("Standard")
                .roomCount(roomCount)
                .checkInDate(LocalDate.of(2026, 7, 15))
                .checkOutDate(LocalDate.of(2026, 7, 17))
                .totalAmount(BigDecimal.valueOf(456))
                .orderStatus(orderStatus)
                .paymentStatus(paymentStatus)
                .build();
    }

    private CheckinQrToken activeToken() {
        return CheckinQrToken.builder()
                .id(9L)
                .orderId(1L)
                .tokenHash(hasher.hash(PLAIN_TOKEN))
                .tokenStatus(QrTokenStatus.ACTIVE)
                .expireAt(now().plusMinutes(10))
                .version(0)
                .build();
    }

    private void assertPendingOrderFailure(BookingOrder order, String expectedCode) {
        stubOrderAndToken(order, activeToken(), List.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));

        assertEquals(expectedCode, ex.getCode());
        verify(roomMapper, never()).selectByIdsForUpdate(anyList());
        verify(tokenMapper, never()).markUsedForCheckinWithVersion(anyLong(), anyLong(), anyInt(), any());
    }

    private void assertTokenStatusFailure(QrTokenStatus status, String expectedCode) {
        CheckinQrToken token = activeToken();
        token.setTokenStatus(status);
        stubOrderAndToken(order(OrderStatus.PENDING_CHECK_IN.name(), PaymentStatus.PAID.name(), 1), token, List.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.confirm(1L, request(List.of(101L))));

        assertEquals(expectedCode, ex.getCode());
        verify(roomMapper, never()).selectByIdsForUpdate(anyList());
    }

    private void stubOrderAndToken(BookingOrder order, CheckinQrToken token, List<RoomAssignment> existingAssignments) {
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order);
        when(tokenMapper.selectByTokenHashForUpdate(hasher.hash(PLAIN_TOKEN))).thenReturn(token);
        when(assignmentMapper.selectActiveByOrderId(eq(1L), anySet())).thenReturn(existingAssignments);
    }

    private void stubSuccessfulCheckin(BookingOrder order, CheckinQrToken token, List<Room> rooms) {
        List<Long> roomIds = rooms.stream().map(Room::getId).sorted().toList();
        stubOrderAndToken(order, token, List.of());
        when(roomMapper.selectByIdsForUpdate(roomIds)).thenReturn(rooms.stream()
                .sorted(java.util.Comparator.comparing(Room::getId))
                .toList());
        when(assignmentMapper.selectActiveByRoomIds(eq(roomIds), anySet())).thenReturn(List.of());
        when(assignmentMapper.insert(any(RoomAssignment.class))).thenAnswer(invocation -> {
            RoomAssignment assignment = invocation.getArgument(0, RoomAssignment.class);
            assignment.setId(300L + assignment.getRoomId());
            return 1;
        });
        for (Room room : rooms) {
            when(roomMapper.markOccupiedForCheckin(eq(room.getId()), eq(room.getVersion()), any())).thenReturn(1);
        }
        when(roomStatusLogMapper.insert(any(RoomStatusLog.class))).thenReturn(1);
        when(bookingOrderMapper.markCheckedInIfPending(eq(1L), any())).thenReturn(1);
        when(tokenMapper.markUsedForCheckinWithVersion(eq(token.getId()), eq(1L), eq(token.getVersion()), any())).thenReturn(1);
    }

    private Room room(Long id, RoomStatus status) {
        return Room.builder()
                .id(id)
                .roomNo("301")
                .floor(3)
                .roomTypeId(2L)
                .roomStatus(status)
                .isDeleted(0)
                .version(0)
                .build();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
