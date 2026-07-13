package com.zhisu.hotel.inventory.service;

import java.time.LocalDate;

public interface InventoryService {
    
    boolean checkInventory(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count);
    
    boolean lockInventory(Long orderId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count);
    
    boolean confirmInventory(Long orderId);
    
    boolean releaseInventory(Long orderId);
}
