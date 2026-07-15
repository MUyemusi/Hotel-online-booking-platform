package com.zhisu.hotel.checkin.service.impl;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.checkin.util.CheckinQrImageGenerator;
import com.zhisu.hotel.checkin.util.CheckinQrTokenGenerator;
import com.zhisu.hotel.checkin.util.CheckinQrTokenHasher;
import com.zhisu.hotel.checkin.vo.CheckinQrGenerationVO;
import com.zhisu.hotel.checkin.vo.CheckinQrVerificationVO;
import com.zhisu.hotel.common.enums.OrderStatus;
import com.zhisu.hotel.common.enums.PaymentStatus;
import com.zhisu.hotel.common.enums.QrTokenStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.frontdesk.service.FrontdeskOrderQueryService;
import com.zhisu.hotel.frontdesk.vo.CheckinVerificationVO;
import com.zhisu.hotel.frontdesk.vo.FrontdeskOrderDetailVO;
import com.zhisu.hotel.room.entity.CheckinQrToken;
import com.zhisu.hotel.room.mapper.CheckinQrTokenMapper;
import com.zhisu.hotel.systemconfig.entity.SystemConfig;
import com.zhisu.hotel.systemconfig.mapper.SystemConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CheckinQrServiceImplTest {

    private final BookingOrderMapper bookingOrderMapper = mock(BookingOrderMapper.class);
    private final CheckinQrTokenMapper tokenMapper = mock(CheckinQrTokenMapper.class);
    private final SystemConfigMapper systemConfigMapper = mock(SystemConfigMapper.class);
    private final FrontdeskOrderQueryService frontdeskOrderQueryService = mock(FrontdeskOrderQueryService.class);
    private final CheckinQrTokenGenerator tokenGenerator = mock(CheckinQrTokenGenerator.class);
    private final CheckinQrTokenHasher tokenHasher = new CheckinQrTokenHasher();
    private final CheckinQrImageGenerator qrImageGenerator = new CheckinQrImageGenerator();
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-15T02:00:00Z"), ZoneId.of("Asia/Shanghai"));

    private CheckinQrServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CheckinQrServiceImpl(
                bookingOrderMapper,
                tokenMapper,
                systemConfigMapper,
                frontdeskOrderQueryService,
                tokenGenerator,
                tokenHasher,
                qrImageGenerator,
                clock
        );
    }

    @Test
    void generateForPaidPendingCheckinOrderStoresHashAndReturnsDecodableQr() throws Exception {
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order(1L));
        when(frontdeskOrderQueryService.verifyCheckin(1L)).thenReturn(validCheckin());
        when(systemConfigMapper.selectOne(any())).thenReturn(config("10"));
        when(tokenGenerator.generateToken()).thenReturn("abcdefghijklmnopqrstuvwxyzABCDE_0123456789");
        when(tokenMapper.insert(any(CheckinQrToken.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, CheckinQrToken.class).setId(100L);
            return 1;
        });

        CheckinQrGenerationVO result = service.generateForOrder(1L, null);

        ArgumentCaptor<CheckinQrToken> tokenCaptor = ArgumentCaptor.forClass(CheckinQrToken.class);
        verify(tokenMapper).revokeActiveByOrderId(eq(1L), any(), eq("REGENERATED"));
        verify(tokenMapper).insert(tokenCaptor.capture());
        CheckinQrToken saved = tokenCaptor.getValue();
        assertEquals(QrTokenStatus.ACTIVE, saved.getTokenStatus());
        assertNotEquals("abcdefghijklmnopqrstuvwxyzABCDE_0123456789", saved.getTokenHash());
        assertEquals(tokenHasher.hash("abcdefghijklmnopqrstuvwxyzABCDE_0123456789"), saved.getTokenHash());
        assertEquals("ZHISU_CHECKIN:abcdefghijklmnopqrstuvwxyzABCDE_0123456789", result.getQrContent());
        assertEquals(result.getQrContent(), decodeDataUrl(result.getQrImageDataUrl()));
        assertEquals(clock.instant().atZone(clock.getZone()).toLocalDateTime().plusMinutes(10), result.getExpiresAt());
    }

    @Test
    void generateRejectsUnpaidOrderThroughSharedEligibility() {
        when(bookingOrderMapper.selectByIdForUpdate(1L)).thenReturn(order(1L));
        when(frontdeskOrderQueryService.verifyCheckin(1L)).thenReturn(CheckinVerificationVO.builder()
                .canCheckIn(false)
                .failureCode(ErrorCode.ORDER_NOT_PAID)
                .failureMessage("Order is not paid")
                .build());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.generateForOrder(1L, null));

        assertEquals(ErrorCode.QR_GENERATION_NOT_ALLOWED, ex.getCode());
        verify(tokenMapper, never()).insert(any(CheckinQrToken.class));
    }

    @Test
    void generateRejectsMissingOrder() {
        when(bookingOrderMapper.selectByIdForUpdate(404L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.generateForOrder(404L, null));

        assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getCode());
    }

    @Test
    void verifyActiveTokenReturnsOrderAndDoesNotMarkUsed() {
        String plain = "abcdefghijklmnopqrstuvwxyzABCDE_0123456789";
        CheckinQrToken token = activeToken(9L, 1L, plain);
        when(tokenMapper.selectOne(any())).thenReturn(token);
        when(frontdeskOrderQueryService.verifyCheckin(1L)).thenReturn(validCheckin());
        when(frontdeskOrderQueryService.getOrderDetail(1L)).thenReturn(orderDetail());

        CheckinQrVerificationVO result = service.verify("ZHISU_CHECKIN:" + plain);

        assertTrue(result.getValid());
        assertEquals(QrTokenStatus.ACTIVE.name(), result.getTokenStatus());
        assertEquals("138****0001", result.getMaskedContactPhone());
        verify(tokenMapper, never()).markUsedForCheckin(anyLong(), anyLong(), any(), any());
    }

    @Test
    void verifyInvalidFormatThrowsBusinessException() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.verify("INVALID_CONTENT"));

        assertEquals(ErrorCode.QR_FORMAT_INVALID, ex.getCode());
        verify(tokenMapper, never()).selectOne(any());
    }

    @Test
    void verifyMissingTokenReturnsInvalid() {
        when(tokenMapper.selectOne(any())).thenReturn(null);

        CheckinQrVerificationVO result = service.verify("ZHISU_CHECKIN:abcdefghijklmnopqrstuvwxyzABCDE_0123456789");

        assertFalse(result.getValid());
        assertEquals(ErrorCode.QR_INVALID, result.getFailureCode());
        assertNull(result.getOrderId());
    }

    @Test
    void verifyExpiredActiveTokenMarksExpiredConditionally() {
        String plain = "abcdefghijklmnopqrstuvwxyzABCDE_0123456789";
        CheckinQrToken token = activeToken(9L, 1L, plain);
        token.setExpireAt(clock.instant().atZone(clock.getZone()).toLocalDateTime().minusMinutes(1));
        when(tokenMapper.selectOne(any())).thenReturn(token);

        CheckinQrVerificationVO result = service.verify("ZHISU_CHECKIN:" + plain);

        assertFalse(result.getValid());
        assertEquals(ErrorCode.QR_EXPIRED, result.getFailureCode());
        verify(tokenMapper).markExpiredIfActive(eq(9L), any());
    }

    @Test
    void verifyRevokedTokenFails() {
        CheckinQrToken token = tokenWithStatus(QrTokenStatus.REVOKED);
        when(tokenMapper.selectOne(any())).thenReturn(token);

        CheckinQrVerificationVO result = service.verify("ZHISU_CHECKIN:abcdefghijklmnopqrstuvwxyzABCDE_0123456789");

        assertFalse(result.getValid());
        assertEquals(ErrorCode.QR_REVOKED, result.getFailureCode());
    }

    @Test
    void verifyUsedTokenFails() {
        CheckinQrToken token = tokenWithStatus(QrTokenStatus.USED);
        when(tokenMapper.selectOne(any())).thenReturn(token);

        CheckinQrVerificationVO result = service.verify("ZHISU_CHECKIN:abcdefghijklmnopqrstuvwxyzABCDE_0123456789");

        assertFalse(result.getValid());
        assertEquals(ErrorCode.QR_ALREADY_USED, result.getFailureCode());
    }

    @Test
    void verifyOrderChangedAfterGenerationFailsWithSharedCheckResult() {
        String plain = "abcdefghijklmnopqrstuvwxyzABCDE_0123456789";
        when(tokenMapper.selectOne(any())).thenReturn(activeToken(9L, 1L, plain));
        when(frontdeskOrderQueryService.verifyCheckin(1L)).thenReturn(CheckinVerificationVO.builder()
                .orderId(1L)
                .orderNo("M4S2-CHECKIN-OK")
                .orderStatus(OrderStatus.CANCELLED.name())
                .paymentStatus(PaymentStatus.PAID.name())
                .canCheckIn(false)
                .failureCode(ErrorCode.ORDER_NOT_PENDING_CHECKIN)
                .failureMessage("Order has been cancelled")
                .build());

        CheckinQrVerificationVO result = service.verify("ZHISU_CHECKIN:" + plain);

        assertFalse(result.getValid());
        assertEquals(ErrorCode.ORDER_NOT_PENDING_CHECKIN, result.getFailureCode());
    }

    @Test
    void consumeUsesConditionalUpdate() {
        when(tokenMapper.markUsedForCheckin(eq(9L), eq(1L), eq(QrTokenStatus.ACTIVE), any())).thenReturn(1);

        service.consumeForSuccessfulCheckin(9L, 1L);

        verify(tokenMapper).markUsedForCheckin(eq(9L), eq(1L), eq(QrTokenStatus.ACTIVE), any());
    }

    private BookingOrder order(Long id) {
        return BookingOrder.builder()
                .id(id)
                .orderNo("M4S2-CHECKIN-OK")
                .userId(3L)
                .orderStatus(OrderStatus.PENDING_CHECK_IN.name())
                .paymentStatus(PaymentStatus.PAID.name())
                .build();
    }

    private CheckinVerificationVO validCheckin() {
        return CheckinVerificationVO.builder()
                .orderId(1L)
                .orderNo("M4S2-CHECKIN-OK")
                .orderStatus(OrderStatus.PENDING_CHECK_IN.name())
                .paymentStatus(PaymentStatus.PAID.name())
                .checkInDate(LocalDate.of(2026, 7, 15))
                .checkOutDate(LocalDate.of(2026, 7, 17))
                .roomTypeId(2L)
                .roomTypeName("Standard")
                .requiredRoomCount(1)
                .availableRoomCount(20)
                .canCheckIn(true)
                .checkItems(List.of())
                .allocatableRooms(List.of())
                .build();
    }

    private FrontdeskOrderDetailVO orderDetail() {
        return FrontdeskOrderDetailVO.builder()
                .orderId(1L)
                .contactName("M4S2 Test Guest A")
                .maskedContactPhone("138****0001")
                .build();
    }

    private SystemConfig config(String value) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("checkin_qr_expire_minutes");
        config.setConfigValue(value);
        return config;
    }

    private CheckinQrToken activeToken(Long id, Long orderId, String plain) {
        return CheckinQrToken.builder()
                .id(id)
                .orderId(orderId)
                .tokenHash(tokenHasher.hash(plain))
                .tokenStatus(QrTokenStatus.ACTIVE)
                .expireAt(clock.instant().atZone(clock.getZone()).toLocalDateTime().plusMinutes(5))
                .version(0)
                .build();
    }

    private CheckinQrToken tokenWithStatus(QrTokenStatus status) {
        String plain = "abcdefghijklmnopqrstuvwxyzABCDE_0123456789";
        CheckinQrToken token = activeToken(9L, 1L, plain);
        token.setTokenStatus(status);
        return token;
    }

    private String decodeDataUrl(String dataUrl) throws Exception {
        String base64 = dataUrl.substring(dataUrl.indexOf(',') + 1);
        byte[] bytes = Base64.getDecoder().decode(base64);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(ImageIO.read(new ByteArrayInputStream(bytes)))));
        return new MultiFormatReader().decode(bitmap).getText();
    }
}
