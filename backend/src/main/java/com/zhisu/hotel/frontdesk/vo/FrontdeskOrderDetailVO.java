package com.zhisu.hotel.frontdesk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontdeskOrderDetailVO {

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
    private Integer roomCount;
    private Integer guestCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer totalNights;
    private BigDecimal amount;
    private LocalDateTime expectedArrivalTime;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private String remark;
    private List<FrontdeskGuestVO> guests;
}
