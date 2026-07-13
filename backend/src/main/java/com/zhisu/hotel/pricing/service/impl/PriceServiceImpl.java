package com.zhisu.hotel.pricing.service.impl;

import com.zhisu.hotel.pricing.service.PriceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PriceServiceImpl implements PriceService {

    @Override
    public BigDecimal calculatePrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count) {
        return BigDecimal.ZERO;
    }
}
