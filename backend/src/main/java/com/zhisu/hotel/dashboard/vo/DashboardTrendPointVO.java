package com.zhisu.hotel.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendPointVO {

    private LocalDate date;

    private long orderCount;

    private BigDecimal revenue;
}