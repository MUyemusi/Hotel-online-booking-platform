package com.zhisu.hotel.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.checkin.service.CheckinQrService;
import com.zhisu.hotel.checkin.util.CheckinQrImageGenerator;
import com.zhisu.hotel.checkin.util.CheckinQrTokenGenerator;
import com.zhisu.hotel.checkin.util.CheckinQrTokenHasher;
import com.zhisu.hotel.checkin.vo.CheckinQrGenerationVO;
import com.zhisu.hotel.checkin.vo.CheckinQrStatusVO;
import com.zhisu.hotel.checkin.vo.CheckinQrVerificationVO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CheckinQrServiceImpl implements CheckinQrService {

    public static final String QR_PREFIX = "ZHISU_CHECKIN:";
    private static final String EXPIRE_CONFIG_KEY = "checkin_qr_expire_minutes";
    private static final int DEFAULT_EXPIRE_MINUTES = 10;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{32,128}$");

    private final BookingOrderMapper bookingOrderMapper;
    private final CheckinQrTokenMapper checkinQrTokenMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final FrontdeskOrderQueryService frontdeskOrderQueryService;
    private final CheckinQrTokenGenerator tokenGenerator;
    private final CheckinQrTokenHasher tokenHasher;
    private final CheckinQrImageGenerator qrImageGenerator;
    private final Clock clock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinQrGenerationVO generateForOrder(Long orderId, Long requesterUserId) {
        BookingOrder order = bookingOrderMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found");
        }
        // Real ownership and role checks are pending member 1 authentication integration.
        if (requesterUserId != null && order.getUserId() != null && !order.getUserId().equals(requesterUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission to generate QR for this order");
        }

        CheckinVerificationVO eligibility = frontdeskOrderQueryService.verifyCheckin(orderId);
        if (!Boolean.TRUE.equals(eligibility.getCanCheckIn())) {
            throw new BusinessException(ErrorCode.QR_GENERATION_NOT_ALLOWED,
                    eligibility.getFailureMessage() == null ? "Current order cannot generate check-in QR" : eligibility.getFailureMessage());
        }

        LocalDateTime now = LocalDateTime.now(clock);
        checkinQrTokenMapper.revokeActiveByOrderId(orderId, now, "REGENERATED");

        String plainToken = tokenGenerator.generateToken();
        String tokenHash = tokenHasher.hash(plainToken);
        LocalDateTime expiresAt = now.plusMinutes(resolveExpireMinutes());
        CheckinQrToken token = CheckinQrToken.builder()
                .orderId(orderId)
                .tokenHash(tokenHash)
                .tokenStatus(QrTokenStatus.ACTIVE)
                .expireAt(expiresAt)
                .version(0)
                .build();
        checkinQrTokenMapper.insert(token);

        String qrContent = QR_PREFIX + plainToken;
        return CheckinQrGenerationVO.builder()
                .tokenId(token.getId())
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .tokenStatus(QrTokenStatus.ACTIVE.name())
                .expiresAt(expiresAt)
                .generatedAt(now)
                .qrContent(qrContent)
                .qrImageDataUrl(qrImageGenerator.generateDataUrl(qrContent))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckinQrStatusVO getActiveStatus(Long orderId, Long requesterUserId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found");
        }
        if (requesterUserId != null && order.getUserId() != null && !order.getUserId().equals(requesterUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission to view QR status for this order");
        }
        CheckinQrToken token = checkinQrTokenMapper.selectOne(new LambdaQueryWrapper<CheckinQrToken>()
                .eq(CheckinQrToken::getOrderId, orderId)
                .eq(CheckinQrToken::getTokenStatus, QrTokenStatus.ACTIVE)
                .orderByDesc(CheckinQrToken::getCreatedAt)
                .last("LIMIT 1"));
        if (token == null) {
            return CheckinQrStatusVO.builder().hasActiveToken(false).build();
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return CheckinQrStatusVO.builder()
                .hasActiveToken(true)
                .tokenId(token.getId())
                .tokenStatus(token.getTokenStatus().name())
                .expiresAt(token.getExpireAt())
                .expired(!now.isBefore(token.getExpireAt()))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinQrVerificationVO verify(String qrContent) {
        String plainToken = extractToken(qrContent);
        String tokenHash = tokenHasher.hash(plainToken);
        CheckinQrToken token = checkinQrTokenMapper.selectOne(new LambdaQueryWrapper<CheckinQrToken>()
                .eq(CheckinQrToken::getTokenHash, tokenHash)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now(clock);
        if (token == null) {
            return invalid(ErrorCode.QR_INVALID, "QR code is invalid", now);
        }

        if (token.getTokenStatus() == QrTokenStatus.USED) {
            return invalidWithToken(token, ErrorCode.QR_ALREADY_USED, "QR code has already been used", now);
        }
        if (token.getTokenStatus() == QrTokenStatus.REVOKED) {
            return invalidWithToken(token, ErrorCode.QR_REVOKED, "QR code has been revoked", now);
        }
        if (token.getTokenStatus() == QrTokenStatus.EXPIRED) {
            return invalidWithToken(token, ErrorCode.QR_EXPIRED, "QR code has expired", now);
        }
        if (token.getTokenStatus() != QrTokenStatus.ACTIVE) {
            return invalidWithToken(token, ErrorCode.QR_INVALID, "QR code is invalid", now);
        }
        if (!now.isBefore(token.getExpireAt())) {
            checkinQrTokenMapper.markExpiredIfActive(token.getId(), now);
            token.setTokenStatus(QrTokenStatus.EXPIRED);
            return invalidWithToken(token, ErrorCode.QR_EXPIRED, "QR code has expired", now);
        }

        CheckinVerificationVO checkin = frontdeskOrderQueryService.verifyCheckin(token.getOrderId());
        if (!Boolean.TRUE.equals(checkin.getCanCheckIn())) {
            return CheckinQrVerificationVO.builder()
                    .valid(false)
                    .tokenId(token.getId())
                    .tokenStatus(token.getTokenStatus().name())
                    .expiresAt(token.getExpireAt())
                    .failureCode(checkin.getFailureCode())
                    .failureMessage(checkin.getFailureMessage())
                    .orderId(checkin.getOrderId())
                    .orderNo(checkin.getOrderNo())
                    .orderStatus(checkin.getOrderStatus())
                    .paymentStatus(checkin.getPaymentStatus())
                    .checkInDate(checkin.getCheckInDate())
                    .checkOutDate(checkin.getCheckOutDate())
                    .roomTypeId(checkin.getRoomTypeId())
                    .roomTypeName(checkin.getRoomTypeName())
                    .requiredRoomCount(checkin.getRequiredRoomCount())
                    .availableRoomCount(checkin.getAvailableRoomCount())
                    .checkItems(checkin.getCheckItems())
                    .allocatableRooms(checkin.getAllocatableRooms())
                    .verifiedAt(now)
                    .build();
        }

        FrontdeskOrderDetailVO detail = frontdeskOrderQueryService.getOrderDetail(token.getOrderId());
        return CheckinQrVerificationVO.builder()
                .valid(true)
                .tokenId(token.getId())
                .tokenStatus(token.getTokenStatus().name())
                .expiresAt(token.getExpireAt())
                .orderId(checkin.getOrderId())
                .orderNo(checkin.getOrderNo())
                .orderStatus(checkin.getOrderStatus())
                .paymentStatus(checkin.getPaymentStatus())
                .maskedContactPhone(detail.getMaskedContactPhone())
                .contactName(detail.getContactName())
                .checkInDate(checkin.getCheckInDate())
                .checkOutDate(checkin.getCheckOutDate())
                .roomTypeId(checkin.getRoomTypeId())
                .roomTypeName(checkin.getRoomTypeName())
                .requiredRoomCount(checkin.getRequiredRoomCount())
                .availableRoomCount(checkin.getAvailableRoomCount())
                .checkItems(checkin.getCheckItems())
                .allocatableRooms(checkin.getAllocatableRooms())
                .verifiedAt(now)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeForSuccessfulCheckin(Long tokenId, Long orderId) {
        int updated = checkinQrTokenMapper.markUsedForCheckin(tokenId, orderId, QrTokenStatus.ACTIVE, LocalDateTime.now(clock));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.QR_TOKEN_CONSUME_NOT_ALLOWED, "Current QR token cannot be consumed");
        }
    }

    private String extractToken(String qrContent) {
        if (qrContent == null || qrContent.isBlank() || !qrContent.startsWith(QR_PREFIX)) {
            throw new BusinessException(ErrorCode.QR_FORMAT_INVALID, "QR code format is invalid");
        }
        String token = qrContent.substring(QR_PREFIX.length());
        if (!TOKEN_PATTERN.matcher(token).matches()) {
            throw new BusinessException(ErrorCode.QR_FORMAT_INVALID, "QR code format is invalid");
        }
        return token;
    }

    private Integer resolveExpireMinutes() {
        SystemConfig config = systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, EXPIRE_CONFIG_KEY)
                .last("LIMIT 1"));
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return DEFAULT_EXPIRE_MINUTES;
        }
        try {
            int value = Integer.parseInt(config.getConfigValue());
            return value > 0 ? value : DEFAULT_EXPIRE_MINUTES;
        } catch (NumberFormatException e) {
            return DEFAULT_EXPIRE_MINUTES;
        }
    }

    private CheckinQrVerificationVO invalid(String code, String message, LocalDateTime verifiedAt) {
        return CheckinQrVerificationVO.builder()
                .valid(false)
                .failureCode(code)
                .failureMessage(message)
                .verifiedAt(verifiedAt)
                .build();
    }

    private CheckinQrVerificationVO invalidWithToken(CheckinQrToken token, String code, String message, LocalDateTime verifiedAt) {
        return CheckinQrVerificationVO.builder()
                .valid(false)
                .tokenId(token.getId())
                .tokenStatus(token.getTokenStatus().name())
                .expiresAt(token.getExpireAt())
                .failureCode(code)
                .failureMessage(message)
                .verifiedAt(verifiedAt)
                .build();
    }
}
