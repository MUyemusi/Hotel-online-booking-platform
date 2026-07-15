package com.zhisu.hotel.room.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CleaningResultVO {
    private Long roomId;
    private String roomNo;
    private Integer floor;
    private String previousStatus;
    private String currentStatus;
    private LocalDateTime cleanedAt;
    private Long operatorId;
    private String maintenanceReason;
    private String message;
}
