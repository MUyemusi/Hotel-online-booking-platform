package com.zhisu.hotel.checkin.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignedRoomVO {
    private Long assignmentId;
    private Long roomId;
    private String roomNo;
    private Integer floor;
    private Long roomTypeId;
    private String roomTypeName;
    private String roomStatus;
    private String assignmentStatus;
    private LocalDateTime assignedAt;
    private LocalDateTime checkInTime;
}
