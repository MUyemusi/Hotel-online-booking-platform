package com.zhisu.hotel.pricing.service;

import com.zhisu.hotel.pricing.entity.DailyPrice;
import com.zhisu.hotel.pricing.vo.DatePriceRule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PriceService {

    BigDecimal calculatePrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count);

    BigDecimal calculateDailyPrice(BigDecimal basePrice, LocalDate date);

    DatePriceRule resolveDateRule(LocalDate date);

    List<DailyPrice> listDailyPrices(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);
}