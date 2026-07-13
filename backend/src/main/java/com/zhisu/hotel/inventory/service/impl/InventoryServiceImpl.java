package com.zhisu.hotel.inventory.service.impl;

import com.zhisu.hotel.inventory.service.InventoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Override
    public boolean checkInventory(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count) {
        return true;
    }

    @Override
    public boolean lockInventory(Long orderId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count) {
        return true;
    }

    @Override
    public boolean confirmInventory(Long orderId) {
        return true;
    }

    @Override
    public boolean releaseInventory(Long orderId) {
        return true;
    }
}
