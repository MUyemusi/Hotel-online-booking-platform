package com.zhisu.hotel.user.service.impl;

import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.user.entity.CreditRecord;
import com.zhisu.hotel.user.entity.SysUser;
import com.zhisu.hotel.user.mapper.CreditRecordMapper;
import com.zhisu.hotel.user.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-07-21T00:00:00Z");

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private CreditRecordMapper creditRecordMapper;

    private CreditServiceImpl creditService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        creditService = new CreditServiceImpl(sysUserMapper, creditRecordMapper, clock);
    }

    @Test
    void shouldAllowEnabledUserWithoutBan() {
        when(sysUserMapper.selectByIdForUpdate(4L)).thenReturn(user(0, null));

        assertTrue(creditService.checkBookingPermission(4L));

        verify(sysUserMapper, never()).update(any(), any());
    }

    @Test
    void shouldRejectUserDuringActiveBan() {
        LocalDateTime banUntil = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).plusDays(1);
        when(sysUserMapper.selectByIdForUpdate(4L)).thenReturn(user(3, banUntil));

        assertFalse(creditService.checkBookingPermission(4L));

        verify(sysUserMapper, never()).update(any(), any());
        verify(creditRecordMapper, never()).insert(any(CreditRecord.class));
    }

    @Test
    void shouldClearExpiredBanAutomatically() {
        LocalDateTime banUntil = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).minusMinutes(1);
        when(sysUserMapper.selectByIdForUpdate(4L)).thenReturn(user(3, banUntil));
        when(sysUserMapper.update(any(), any())).thenReturn(1);
        when(creditRecordMapper.insert(any(CreditRecord.class))).thenReturn(1);

        assertTrue(creditService.checkBookingPermission(4L));

        CreditRecord record = captureRecord();
        assertEquals("UNBAN", record.getChangeType());
        assertEquals(3, record.getBeforeCount());
        assertEquals(0, record.getAfterCount());
    }

    @Test
    void shouldIncrementCancellationCount() {
        when(sysUserMapper.selectByIdForUpdate(4L)).thenReturn(user(1, null));
        when(creditRecordMapper.selectCount(any())).thenReturn(0L);
        when(sysUserMapper.update(any(), any())).thenReturn(1);
        when(creditRecordMapper.insert(any(CreditRecord.class))).thenReturn(1);

        creditService.recordUserCancellation(4L, 101L);

        CreditRecord record = captureRecord();
        assertEquals(101L, record.getOrderId());
        assertEquals("CANCEL", record.getChangeType());
        assertEquals(1, record.getBeforeCount());
        assertEquals(2, record.getAfterCount());
    }

    @Test
    void shouldBanUserAfterThirdCancellation() {
        when(sysUserMapper.selectByIdForUpdate(4L)).thenReturn(user(2, null));
        when(creditRecordMapper.selectCount(any())).thenReturn(0L);
        when(sysUserMapper.update(any(), any())).thenReturn(1);
        when(creditRecordMapper.insert(any(CreditRecord.class))).thenReturn(1);

        creditService.recordUserCancellation(4L, 102L);

        CreditRecord record = captureRecord();
        assertEquals("BAN", record.getChangeType());
        assertEquals(3, record.getAfterCount());
        assertEquals(
                LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).plusDays(7),
                record.getBanUntil()
        );
    }

    @Test
    void shouldResetStateAfterSuccessfulStay() {
        LocalDateTime banUntil = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).plusDays(2);
        when(sysUserMapper.selectByIdForUpdate(4L)).thenReturn(user(2, banUntil));
        when(creditRecordMapper.selectCount(any())).thenReturn(0L);
        when(sysUserMapper.update(any(), any())).thenReturn(1);
        when(creditRecordMapper.insert(any(CreditRecord.class))).thenReturn(1);

        creditService.resetContinuousCancellation(4L, 103L);

        CreditRecord record = captureRecord();
        assertEquals("COMPLETE", record.getChangeType());
        assertEquals(2, record.getBeforeCount());
        assertEquals(0, record.getAfterCount());
    }

    @Test
    void shouldAllowAdminToUnbanRestrictedUser() {
        LocalDateTime banUntil = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).plusDays(2);
        when(sysUserMapper.selectByIdForUpdate(4L)).thenReturn(user(3, banUntil));
        when(sysUserMapper.update(any(), any())).thenReturn(1);
        when(creditRecordMapper.insert(any(CreditRecord.class))).thenReturn(1);

        creditService.unbanUser(4L, 1L, "人工核实后解除");

        CreditRecord record = captureRecord();
        assertEquals("UNBAN", record.getChangeType());
        assertEquals(1L, record.getOperatorId());
        assertEquals("人工核实后解除", record.getReason());
    }

    @Test
    void shouldRejectUnbanWhenUserIsNotRestricted() {
        when(sysUserMapper.selectByIdForUpdate(4L)).thenReturn(user(0, null));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> creditService.unbanUser(4L, 1L, "无需限制")
        );

        assertEquals("用户当前未被限制预订", exception.getMessage());
    }

    private CreditRecord captureRecord() {
        ArgumentCaptor<CreditRecord> captor = ArgumentCaptor.forClass(CreditRecord.class);
        verify(creditRecordMapper).insert(captor.capture());
        return captor.getValue();
    }

    private SysUser user(int count, LocalDateTime banUntil) {
        return SysUser.builder()
                .id(4L)
                .username("student")
                .status("ENABLED")
                .consecutiveCancelCount(count)
                .banUntil(banUntil)
                .build();
    }
}
