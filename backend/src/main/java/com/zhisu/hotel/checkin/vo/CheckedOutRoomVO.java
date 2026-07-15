package com.zhisu.hotel.checkin.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CheckedOutRoomVO {
    private Long assignmentId;
    private Long roomId;
    private String roomNo;
    private Integer floor;
    private Long roomTypeId;
    private String roomTypeName;
    private String previousRoomStatus;
    private String currentRoomStatus;
    private String assignmentStatus;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}
