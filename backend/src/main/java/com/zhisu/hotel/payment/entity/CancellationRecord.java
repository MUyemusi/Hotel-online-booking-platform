package com.zhisu.hotel.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("cancellation_record")
public class CancellationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private String cancelType;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private String orderStatusBefore;
    private String paymentStatusBefore;
    private BigDecimal refundAmount;
    private Integer refundAmountFen;
    private String refundId;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
