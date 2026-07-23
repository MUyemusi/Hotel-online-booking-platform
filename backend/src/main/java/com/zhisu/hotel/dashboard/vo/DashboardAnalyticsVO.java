package com.zhisu.hotel.dashboard.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardAnalyticsVO {
    private Integer periodDays;
    private BigDecimal currentPeriodRevenue;
    private BigDecimal previousPeriodRevenue;
    private BigDecimal revenueGrowthRate;
    private List<DashboardTrendPointVO> trends;
    private List<RoomTypeStatisticVO> roomTypeStatistics;
    private List<DashboardCategoryStatisticVO> paymentMethods;
    private List<DashboardCategoryStatisticVO> orderStatuses;
}
