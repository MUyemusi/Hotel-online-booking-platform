package com.zhisu.hotel.payment.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationVO {

    private Long id;
    private Long orderId;
    private String cancelType;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private String orderStatusBefore;
    private BigDecimal refundAmount;
    private String refundId;
}
