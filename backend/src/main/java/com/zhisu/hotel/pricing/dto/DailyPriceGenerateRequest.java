package com.zhisu.hotel.pricing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request payload for batch daily-price generation.
 */
@Data
public class DailyPriceGenerateRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}