package com.zhisu.hotel.room.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DirtyRoomVO {
    private Long roomId;
    private String roomNo;
    private Integer floor;
    private Long roomTypeId;
    private String roomTypeName;
    private String roomStatus;
    private Long latestOrderId;
    private String latestOrderNo;
    private LocalDateTime latestCheckOutTime;
    private LocalDateTime dirtySince;
    private String remark;
}
