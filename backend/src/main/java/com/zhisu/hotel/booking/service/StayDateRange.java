package com.zhisu.hotel.booking.service;

import com.zhisu.hotel.common.enums.StayType;

import java.time.LocalDate;

public record StayDateRange(
        StayType stayType,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        LocalDate inventoryStartDate,
        LocalDate inventoryEndExclusive,
        int stayNights
) {
    public LocalDate lastInventoryDate() {
        return inventoryEndExclusive.minusDays(1);
    }
}
