package com.zhisu.hotel.pricing.task;

import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.pricing.service.DailyPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyPriceRollingTask {

    private final DailyPriceService dailyPriceService;
    private final InventoryService inventoryService;

    @Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Shanghai")
    public void generateRollingDailyPrices() {
        int priceProcessed = dailyPriceService.ensureRollingWindowPrices();
        int inventoryProcessed = inventoryService.ensureRollingWindowInventory();
        log.info("Rolling daily task completed. priceProcessed={}, inventoryProcessed={}", priceProcessed, inventoryProcessed);
    }
}