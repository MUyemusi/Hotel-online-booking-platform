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
public class PaymentVO {

    private Long id;
    private String paymentNo;
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private String paymentStatus;
    private LocalDateTime paidAt;
    private BigDecimal refundAmount;
    private String refundTransactionId;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
}
