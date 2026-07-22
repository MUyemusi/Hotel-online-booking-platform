package com.zhisu.hotel.dashboard.service.impl;

import com.zhisu.hotel.dashboard.vo.DashboardOverviewVO;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DashboardServiceImplTest {
    @Test
    void overviewUsesPaidFlowAndUserCancelStatuses() {
        DashboardServiceImpl service = new DashboardServiceImpl(mock(JdbcTemplate.class)) {
            @Override protected boolean tableExists(String name) { return true; }
            @Override protected int systemInteger(String key, int fallback) { return 2; }
            @Override protected int queryForInt(String sql, Object... args) { return 6; }
            @Override protected BigDecimal queryForDecimal(String sql, Object... args) {
                assertTrue(sql.contains("payment_status = 'PAID'"));
                return new BigDecimal("256.00");
            }
            @Override protected long queryForLong(String sql, Object... args) {
                if (sql.contains("COUNT(DISTINCT order_id)")) {
                    assertTrue(sql.contains("payment_status = 'PAID'"));
                    return 2;
                }
                if (sql.contains("cancel_type")) {
                    assertTrue(sql.contains("cancel_type = 'USER_CANCEL'"));
                    return 3;
                }
                if (sql.contains("room_status = 'OCCUPIED'")) return 4;
                if (sql.contains("room_status = 'DIRTY'")) return 1;
                if (sql.contains("room_status NOT IN")) return 8;
                return 1;
            }
        };

        DashboardOverviewVO result = service.getOverview();

        assertEquals(new BigDecimal("256.00"), result.getTodayRevenue());
        assertEquals(2, result.getTodayPaidOrderCount());
        assertEquals(3, result.getTodayUserCancellationCount());
        assertEquals(new BigDecimal("50.00"), result.getOccupancyRate());
    }
}