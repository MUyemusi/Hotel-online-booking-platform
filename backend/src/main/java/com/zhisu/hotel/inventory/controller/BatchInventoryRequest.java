package com.zhisu.hotel.inventory.controller;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BatchInventoryRequest {
    private Long roomTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalRooms;
    private String status;
}
