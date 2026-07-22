package com.zhisu.hotel.audit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhisu.hotel.audit.entity.OperationLog;
import com.zhisu.hotel.audit.service.OperationLogService;
import com.zhisu.hotel.common.context.UserContext;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OperationLogFilterTest {

    @Mock
    private OperationLogService operationLogService;

    private OperationLogFilter operationLogFilter;

    @BeforeEach
    void setUp() {
        operationLogFilter = new OperationLogFilter(
                operationLogService,
                new ObjectMapper()
        );
        UserContext.setUserId(1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldRecordSuccessfulWriteOperation() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("PUT", "/api/admin/users/4/status");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"code\":\"SUCCESS\",\"message\":\"操作成功\"}");
        };

        operationLogFilter.doFilterInternal(request, response, chain);

        OperationLog log = captureLog();
        assertEquals(1L, log.getOperatorId());
        assertEquals("USERS", log.getModule());
        assertEquals("UPDATE", log.getAction());
        assertEquals("SUCCESS", log.getResult());
        assertEquals("127.0.0.1", log.getIpAddress());
    }

    @Test
    void shouldRecordBusinessFailureEvenWhenHttpStatusIs200() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("PUT", "/api/admin/users/4/credit/unban");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(
                    "{\"code\":\"INVALID_REQUEST\",\"message\":\"用户当前未被限制预订\"}"
            );
        };

        operationLogFilter.doFilterInternal(request, response, chain);

        OperationLog log = captureLog();
        assertEquals("UNBAN", log.getAction());
        assertEquals("FAILED", log.getResult());
    }

    @Test
    void shouldNotRecordReadOperation() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/admin/operation-logs");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
                ((jakarta.servlet.http.HttpServletResponse) res).setStatus(200);

        operationLogFilter.doFilterInternal(request, response, chain);

        verify(operationLogService, never()).record(
                org.mockito.ArgumentMatchers.any(OperationLog.class)
        );
    }

    @Test
    void shouldResolveModuleAndSpecialAction() {
        assertEquals("ORDERS", OperationLogFilter.resolveModule("/api/admin/orders/10/cancel"));
        assertEquals("CANCEL", OperationLogFilter.resolveAction("PUT", "/api/orders/10/cancel"));
        assertEquals("LOGIN", OperationLogFilter.resolveAction("POST", "/api/auth/login"));
    }

    private OperationLog captureLog() {
        ArgumentCaptor<OperationLog> captor =
                ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogService).record(captor.capture());
        return captor.getValue();
    }
}
