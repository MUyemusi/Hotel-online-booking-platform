package com.zhisu.hotel.booking.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontdeskOrderInfo {

    private Long orderId;
    private String orderNo;
    private String contactPhone;
    private Long roomTypeId;
    private Integer roomCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String orderStatus;
    private String paymentStatus;
    private List<GuestVO> guests;
}
