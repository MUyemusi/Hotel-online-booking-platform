package com.zhisu.hotel.pricing.service;

import com.zhisu.hotel.pricing.vo.PriceQuoteVO;

import java.time.LocalDate;

public interface PriceQuoteService {

    PriceQuoteVO quotePrice(
            Long roomTypeId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer roomCount
    );
}