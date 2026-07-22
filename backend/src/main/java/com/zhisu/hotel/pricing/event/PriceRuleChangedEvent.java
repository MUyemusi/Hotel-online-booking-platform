package com.zhisu.hotel.pricing.event;

import java.time.LocalDate;

public record PriceRuleChangedEvent(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
}