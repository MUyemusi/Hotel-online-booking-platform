package com.zhisu.hotel.audit.service.impl;

import com.zhisu.hotel.audit.entity.OperationLog;
import com.zhisu.hotel.audit.mapper.OperationLogMapper;
import com.zhisu.hotel.audit.vo.OperationLogVO;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationLogServiceImplTest {

    @Mock
    private OperationLogMapper operationLogMapper;

    private OperationLogServiceImpl operationLogService;

    @BeforeEach
    void setUp() {
        operationLogService = new OperationLogServiceImpl(operationLogMapper);
    }

    @Test
    void shouldInsertOperationLog() {
        OperationLog log = OperationLog.builder()
                .module("USERS")
                .action("UPDATE")
                .result("SUCCESS")
                .build();
        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        operationLogService.record(log);
    }

    @Test
    void shouldReturnOperationLogDetail() {
        OperationLog log = OperationLog.builder()
                .id(8L)
                .operatorId(1L)
                .module("USERS")
                .action("UPDATE")
                .result("SUCCESS")
                .build();
        when(operationLogMapper.selectById(8L)).thenReturn(log);

        OperationLogVO result = operationLogService.getOperationLogById(8L);

        assertEquals(8L, result.getId());
        assertEquals("USERS", result.getModule());
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> operationLogService.getOperationLogPage(
                        1,
                        10,
                        null,
                        null,
                        null,
                        null,
                        java.time.LocalDateTime.of(2026, 7, 22, 0, 0),
                        java.time.LocalDateTime.of(2026, 7, 21, 0, 0)
                )
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getCode());
    }
}
