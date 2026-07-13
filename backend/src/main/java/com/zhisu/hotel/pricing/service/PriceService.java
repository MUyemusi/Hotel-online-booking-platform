package com.zhisu.hotel.pricing.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PriceService {
    
    BigDecimal calculatePrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count);
}
