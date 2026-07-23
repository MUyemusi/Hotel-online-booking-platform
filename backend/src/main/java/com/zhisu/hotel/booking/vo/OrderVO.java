
package com.zhisu.hotel.booking.vo;

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
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long roomTypeId;
    private String roomTypeName;
    private Integer roomCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String stayType;
    private Integer totalNights;
    private Integer guestCount;
    private BigDecimal totalAmount;
    private String phone;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime paymentTimeout;
    private LocalDateTime createdAt;
}
