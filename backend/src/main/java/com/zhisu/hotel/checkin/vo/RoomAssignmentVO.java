package com.zhisu.hotel.checkin.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomAssignmentVO {
    private Long assignmentId;
    private Long orderId;
    private Long roomId;
    private String roomNo;
    private Integer floor;
    private String roomTypeName;
    private String assignmentStatus;
    private LocalDateTime assignedAt;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}
