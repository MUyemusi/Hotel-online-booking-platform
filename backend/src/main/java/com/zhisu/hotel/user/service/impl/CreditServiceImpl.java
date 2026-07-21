package com.zhisu.hotel.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.user.entity.CreditRecord;
import com.zhisu.hotel.user.entity.SysUser;
import com.zhisu.hotel.user.mapper.CreditRecordMapper;
import com.zhisu.hotel.user.mapper.SysUserMapper;
import com.zhisu.hotel.user.service.CreditService;
import com.zhisu.hotel.user.vo.CreditRecordVO;
import com.zhisu.hotel.user.vo.CreditSummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class CreditServiceImpl implements CreditService {

    public static final int CANCELLATION_LIMIT = 3;
    public static final int BAN_DAYS = 7;

    private static final String USER_CANCELLATION = "CANCEL";
    private static final String BOOKING_BANNED = "BAN";
    private static final String SUCCESSFUL_STAY_RESET = "COMPLETE";
    private static final String BAN_EXPIRED = "UNBAN";
    private static final String ADMIN_UNBAN = "UNBAN";

    private final SysUserMapper sysUserMapper;
    private final CreditRecordMapper creditRecordMapper;
    private final Clock clock;

    public CreditServiceImpl(
            SysUserMapper sysUserMapper,
            CreditRecordMapper creditRecordMapper,
            Clock clock) {
        this.sysUserMapper = sysUserMapper;
        this.creditRecordMapper = creditRecordMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public boolean checkBookingPermission(Long userId) {
        SysUser user = requireUserForUpdate(userId);
        if (!"ENABLED".equalsIgnoreCase(user.getStatus())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (user.getBanUntil() == null) {
            return true;
        }
        if (user.getBanUntil().isAfter(now)) {
            return false;
        }

        int beforeCount = safeCount(user.getConsecutiveCancelCount());
        updateCreditState(userId, 0, null);
        insertRecord(CreditRecord.builder()
                .userId(userId)
                .changeType(BAN_EXPIRED)
                .beforeCount(beforeCount)
                .afterCount(0)
                .reason("禁订期已自动结束")
                .build());
        return true;
    }

    @Override
    @Transactional
    public void recordUserCancellation(Long userId, Long orderId) {
        SysUser user = requireUserForUpdate(userId);
        if (hasOrderRecord(orderId, Set.of(USER_CANCELLATION, BOOKING_BANNED))) {
            return;
        }
        int beforeCount = safeCount(user.getConsecutiveCancelCount());
        int afterCount = beforeCount + 1;
        LocalDateTime banUntil = user.getBanUntil();
        String changeType = USER_CANCELLATION;
        String reason = "用户主动取消订单，连续取消次数加一";

        if (afterCount >= CANCELLATION_LIMIT) {
            banUntil = LocalDateTime.now(clock).plusDays(BAN_DAYS);
            changeType = BOOKING_BANNED;
            reason = "连续主动取消达到" + CANCELLATION_LIMIT
                    + "次，限制预订" + BAN_DAYS + "天";
        }

        updateCreditState(userId, afterCount, banUntil);
        insertRecord(CreditRecord.builder()
                .userId(userId)
                .orderId(orderId)
                .changeType(changeType)
                .beforeCount(beforeCount)
                .afterCount(afterCount)
                .banUntil(banUntil)
                .reason(reason)
                .build());
    }

    @Override
    @Transactional
    public void resetContinuousCancellation(Long userId, Long orderId) {
        SysUser user = requireUserForUpdate(userId);
        if (hasOrderRecord(orderId, Set.of(SUCCESSFUL_STAY_RESET))) {
            return;
        }
        int beforeCount = safeCount(user.getConsecutiveCancelCount());
        if (beforeCount == 0 && user.getBanUntil() == null) {
            return;
        }

        updateCreditState(userId, 0, null);
        insertRecord(CreditRecord.builder()
                .userId(userId)
                .orderId(orderId)
                .changeType(SUCCESSFUL_STAY_RESET)
                .beforeCount(beforeCount)
                .afterCount(0)
                .reason("订单正常完成，连续取消次数已重置")
                .build());
    }

    @Override
    @Transactional
    public CreditSummaryVO getCreditSummary(Long userId) {
        SysUser user = requireUserForUpdate(userId);
        LocalDateTime now = LocalDateTime.now(clock);
        if (user.getBanUntil() != null && !user.getBanUntil().isAfter(now)) {
            int beforeCount = safeCount(user.getConsecutiveCancelCount());
            updateCreditState(userId, 0, null);
            insertRecord(CreditRecord.builder()
                    .userId(userId)
                    .changeType(BAN_EXPIRED)
                    .beforeCount(beforeCount)
                    .afterCount(0)
                    .reason("禁订期已自动结束")
                    .build());
            user.setConsecutiveCancelCount(0);
            user.setBanUntil(null);
        }
        return toSummary(user, now);
    }

    @Override
    public PageResponse<CreditRecordVO> getCreditRecords(
            Long userId,
            long current,
            long size) {
        requireUser(userId);
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<CreditRecord> page = creditRecordMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                new LambdaQueryWrapper<CreditRecord>()
                        .eq(CreditRecord::getUserId, userId)
                        .orderByDesc(CreditRecord::getCreatedAt)
                        .orderByDesc(CreditRecord::getId)
        );
        List<CreditRecordVO> records = page.getRecords().stream()
                .map(this::toRecordVO)
                .toList();
        return PageResponse.<CreditRecordVO>builder()
                .records(records)
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    @Transactional
    public void unbanUser(Long userId, Long operatorId, String reason) {
        SysUser user = requireUserForUpdate(userId);
        int beforeCount = safeCount(user.getConsecutiveCancelCount());
        if (beforeCount == 0 && user.getBanUntil() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户当前未被限制预订");
        }

        updateCreditState(userId, 0, null);
        insertRecord(CreditRecord.builder()
                .userId(userId)
                .changeType(ADMIN_UNBAN)
                .beforeCount(beforeCount)
                .afterCount(0)
                .reason(reason.trim())
                .operatorId(operatorId)
                .build());
    }

    private SysUser requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户ID不能为空");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private SysUser requireUserForUpdate(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户ID不能为空");
        }
        SysUser user = sysUserMapper.selectByIdForUpdate(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void updateCreditState(
            Long userId,
            int consecutiveCancelCount,
            LocalDateTime banUntil) {
        int updated = sysUserMapper.update(
                null,
                new UpdateWrapper<SysUser>()
                        .eq("id", userId)
                        .set("consecutive_cancel_count", consecutiveCancelCount)
                        .set("ban_until", banUntil)
        );
        if (updated != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "用户信用状态更新失败");
        }
    }

    private boolean hasOrderRecord(Long orderId, Set<String> changeTypes) {
        if (orderId == null) {
            return false;
        }
        return creditRecordMapper.selectCount(
                new LambdaQueryWrapper<CreditRecord>()
                        .eq(CreditRecord::getOrderId, orderId)
                        .in(CreditRecord::getChangeType, changeTypes)
        ) > 0;
    }

    private void insertRecord(CreditRecord record) {
        if (creditRecordMapper.insert(record) != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "信用记录写入失败");
        }
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private CreditSummaryVO toSummary(SysUser user, LocalDateTime now) {
        boolean allowed = "ENABLED".equalsIgnoreCase(user.getStatus())
                && (user.getBanUntil() == null || !user.getBanUntil().isAfter(now));
        return CreditSummaryVO.builder()
                .userId(user.getId())
                .consecutiveCancelCount(safeCount(user.getConsecutiveCancelCount()))
                .cancellationLimit(CANCELLATION_LIMIT)
                .banUntil(user.getBanUntil())
                .bookingAllowed(allowed)
                .build();
    }

    private CreditRecordVO toRecordVO(CreditRecord record) {
        return CreditRecordVO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .orderId(record.getOrderId())
                .changeType(record.getChangeType())
                .beforeCount(record.getBeforeCount())
                .afterCount(record.getAfterCount())
                .banUntil(record.getBanUntil())
                .reason(record.getReason())
                .operatorId(record.getOperatorId())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
