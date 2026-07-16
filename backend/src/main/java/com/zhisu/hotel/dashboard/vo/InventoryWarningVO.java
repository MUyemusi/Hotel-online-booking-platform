package com.zhisu.hotel.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryWarningVO {

    private Long roomTypeId;

    private String roomTypeName;

    private LocalDate inventoryDate;

    private Integer availableRooms;

    private Integer totalRooms;

    private String status;
}