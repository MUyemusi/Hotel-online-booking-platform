package com.zhisu.hotel.frontdesk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontdeskOrderListVO {

    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private String orderStatusText;
    private String paymentStatus;
    private String paymentStatusText;
    private String contactName;
    private String maskedContactPhone;
    private Long roomTypeId;
    private String roomTypeName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer roomCount;
    private Integer guestCount;
    private BigDecimal amount;
    private LocalDateTime expectedArrivalTime;
    private LocalDateTime createdAt;
}
