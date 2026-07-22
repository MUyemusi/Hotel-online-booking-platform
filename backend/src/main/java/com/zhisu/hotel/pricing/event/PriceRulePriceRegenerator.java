package com.zhisu.hotel.pricing.event;

import com.zhisu.hotel.pricing.service.DailyPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceRulePriceRegenerator {
    private final DailyPriceService dailyPriceService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void regenerate(PriceRuleChangedEvent event) {
        LocalDate start = event.startDate().isBefore(LocalDate.now()) ? LocalDate.now() : event.startDate();
        if (event.endDate().isBefore(start)) return;
        LocalDate chunkStart = start;
        while (!chunkStart.isAfter(event.endDate())) {
            LocalDate chunkEnd = chunkStart.plusDays(449);
            if (chunkEnd.isAfter(event.endDate())) chunkEnd = event.endDate();
            try {
                if (event.roomTypeId() == null) {
                    dailyPriceService.generateAllRoomTypePrices(chunkStart, chunkEnd);
                } else {
                    dailyPriceService.generateRoomTypePrices(event.roomTypeId(), chunkStart, chunkEnd);
                }
            } catch (Exception e) {
                log.error("价格规则变更后的每日价格重生成失败: roomTypeId={}, {}~{}", event.roomTypeId(), chunkStart, chunkEnd, e);
                return;
            }
            chunkStart = chunkEnd.plusDays(1);
        }
    }
}