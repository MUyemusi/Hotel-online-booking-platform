package com.zhisu.hotel.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HolidayCalendarSaveRequest {

    @NotNull(message = "Business date is required")
    private LocalDate businessDate;

    @NotBlank(message = "Holiday name is required")
    @Size(max = 100, message = "Holiday name must be within 100 characters")
    private String holidayName;

    @NotBlank(message = "Holiday code is required")
    @Size(max = 50, message = "Holiday code must be within 50 characters")
    private String holidayCode;

    @NotBlank(message = "Day type is required")
    @Size(max = 30, message = "Day type must be within 30 characters")
    private String dayType;

    @NotNull(message = "Coefficient is required")
    @DecimalMin(value = "1.00", message = "Coefficient must be greater than or equal to 1.00")
    @Digits(integer = 3, fraction = 2, message = "Coefficient must keep up to 2 decimal places")
    private BigDecimal coefficient;

    @NotNull(message = "Holiday year is required")
    private Integer holidayYear;

    @NotBlank(message = "Source status is required")
    @Size(max = 20, message = "Source status must be within 20 characters")
    private String sourceStatus;

    @NotBlank(message = "Status is required")
    @Size(max = 20, message = "Status must be within 20 characters")
    private String status;

    @Size(max = 255, message = "Remark must be within 255 characters")
    private String remark;
}