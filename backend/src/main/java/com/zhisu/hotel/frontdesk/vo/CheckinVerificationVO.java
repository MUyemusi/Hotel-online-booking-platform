package com.zhisu.hotel.frontdesk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinVerificationVO {

    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private String orderStatusText;
    private String paymentStatus;
    private String paymentStatusText;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long roomTypeId;
    private String roomTypeName;
    private Integer requiredRoomCount;
    private Integer assignedRoomCount;
    private Integer remainingRoomCount;
    private Integer availableRoomCount;
    private Boolean canCheckIn;
    private String failureCode;
    private String failureMessage;
    private List<CheckinCheckItemVO> checkItems;
    private List<AllocatableRoomVO> allocatableRooms;
    private LocalDateTime verifiedAt;
}
