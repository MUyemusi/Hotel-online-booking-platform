package com.zhisu.hotel.pricing.service;

import com.zhisu.hotel.pricing.entity.DailyPrice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DailyPriceService {

    int generateAllRoomTypePrices(LocalDate startDate, LocalDate endDate);

    int generateRoomTypePrices(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    List<DailyPrice> listDailyPrices(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    int ensureRollingWindowPrices();

    void ensurePricesExist(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    DailyPrice setManualOverride(Long roomTypeId, LocalDate businessDate, BigDecimal manualPrice, String overrideReason);

    DailyPrice clearManualOverride(Long roomTypeId, LocalDate businessDate);

    DailyPrice setManualOverrideById(Long dailyPriceId, BigDecimal manualPrice, String overrideReason);

    DailyPrice clearManualOverrideById(Long dailyPriceId);
}