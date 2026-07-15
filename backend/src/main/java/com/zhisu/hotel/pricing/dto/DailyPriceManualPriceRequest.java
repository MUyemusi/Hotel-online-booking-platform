package com.zhisu.hotel.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyPriceManualPriceRequest {

    @NotNull(message = "Manual price is required")
    @DecimalMin(value = "0.01", message = "Manual price must be greater than 0")
    private BigDecimal manualPrice;

    private String overrideReason;
}