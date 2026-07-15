package com.zhisu.hotel.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BasePriceUpdateRequest {

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Base price must keep up to 2 decimal places")
    private BigDecimal basePrice;

    @NotBlank(message = "Change reason is required")
    @Size(max = 200, message = "Change reason must be within 200 characters")
    private String reason;
}