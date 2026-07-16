package com.zhisu.hotel.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewVO {

    private long todayOrderCount;

    private BigDecimal todayRevenue;

    private long todayCheckInCount;

    private long todayCheckOutCount;

    private int availableRoomCount;

    private long inventoryWarningCount;

    private long publishedAnnouncementCount;

    private long onlineBannerCount;

    private long enabledSystemConfigCount;
}