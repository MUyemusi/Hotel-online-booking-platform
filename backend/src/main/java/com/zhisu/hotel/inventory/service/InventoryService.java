package com.zhisu.hotel.inventory.service;

import com.zhisu.hotel.inventory.entity.DailyInventory;

import java.time.LocalDate;
import java.util.List;

public interface InventoryService {

    boolean checkInventory(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count);

    boolean lockInventory(Long orderId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count);

    boolean confirmInventory(Long orderId);

    boolean releaseInventory(Long orderId);

    void generateDailyInventory(Long roomTypeId, int totalRooms, int days);

    void generateAllDailyInventory(int days);

    void ensureInventoryExists(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    int ensureRollingWindowInventory();

    List<DailyInventory> getAvailableInventory(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    int getMinAvailableCount(Long roomTypeId, LocalDate startDate, LocalDate endDate);
}