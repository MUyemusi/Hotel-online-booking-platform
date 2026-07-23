
package com.zhisu.hotel.pricing.service;

import com.zhisu.hotel.pricing.vo.PriceQuoteVO;

import java.time.LocalDate;

public interface PriceQuoteService {

    default PriceQuoteVO quotePrice(
            Long roomTypeId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer roomCount
    ) {
        return quotePrice(roomTypeId, checkInDate, checkOutDate, roomCount, "STANDARD");
    }

    PriceQuoteVO quotePrice(
            Long roomTypeId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer roomCount,
            String stayType
    );
}
