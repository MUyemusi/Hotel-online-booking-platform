package com.zhisu.hotel.dashboard.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardCategoryStatisticVO {
    private String code;
    private String label;
    private Long count;
    private BigDecimal amount;
}
