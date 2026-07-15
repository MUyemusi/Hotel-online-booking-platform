package com.zhisu.hotel.checkin.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ConfirmCheckinVO {
    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime checkInTime;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long roomTypeId;
    private String roomTypeName;
    private Integer requiredRoomCount;
    private Integer assignedRoomCount;
    private List<AssignedRoomVO> assignedRooms;
    private Long tokenId;
    private String tokenStatus;
    private String message;
    private Long operatorId;
    private LocalDateTime completedAt;
}
