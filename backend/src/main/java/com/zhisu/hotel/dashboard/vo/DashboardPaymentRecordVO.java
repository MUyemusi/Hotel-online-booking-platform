package com.zhisu.hotel.dashboard.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DashboardPaymentRecordVO {
    private Long paymentId;
    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private String paymentMethod;
    private BigDecimal amount;
    private LocalDateTime paidAt;
}

