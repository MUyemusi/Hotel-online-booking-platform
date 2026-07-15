package com.zhisu.hotel.pricing.vo;

import java.math.BigDecimal;

public record DatePriceRule(
        BigDecimal coefficient,
        String priceSource,
        String description,
        String sourceStatus,
        Long holidayCalendarId
) {
}