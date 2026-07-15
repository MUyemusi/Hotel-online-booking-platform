package com.zhisu.hotel.room.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorRoomStatisticsVO {

    private Integer floor;

    private Integer totalCount;

    private Integer availableCount;

    private Integer reservedCount;

    private Integer occupiedCount;

    private Integer dirtyCount;

    private Integer maintenanceCount;

    private Integer disabledCount;
}
