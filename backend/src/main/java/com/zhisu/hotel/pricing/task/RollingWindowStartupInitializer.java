package com.zhisu.hotel.pricing.task;

import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.pricing.service.DailyPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RollingWindowStartupInitializer {

    private final DailyPriceService dailyPriceService;
    private final InventoryService inventoryService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRollingWindow() {
        int priceProcessed = dailyPriceService.ensureRollingWindowPrices();
        int inventoryProcessed = inventoryService.ensureRollingWindowInventory();
        log.info("Rolling window initialized on startup. priceProcessed={}, inventoryProcessed={}", priceProcessed, inventoryProcessed);
    }
}