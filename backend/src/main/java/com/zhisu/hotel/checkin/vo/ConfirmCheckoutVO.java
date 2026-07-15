package com.zhisu.hotel.checkin.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ConfirmCheckoutVO {
    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private String paymentStatus;
    private LocalDate scheduledCheckInDate;
    private LocalDate scheduledCheckOutDate;
    private LocalDateTime actualCheckOutTime;
    private Integer roomCount;
    private Integer checkedOutRoomCount;
    private List<CheckedOutRoomVO> checkedOutRooms;
    private String requestToken;
    private Long operatorId;
    private LocalDateTime completedAt;
    private String message;
}
