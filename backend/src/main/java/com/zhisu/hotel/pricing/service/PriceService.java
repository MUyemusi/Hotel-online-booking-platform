package com.zhisu.hotel.pricing.service;

import com.zhisu.hotel.pricing.vo.DatePriceRule;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PriceService {

    BigDecimal calculatePrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count);

    BigDecimal calculateDailyPrice(BigDecimal basePrice, LocalDate date);

    DatePriceRule resolveDateRule(LocalDate date);
}