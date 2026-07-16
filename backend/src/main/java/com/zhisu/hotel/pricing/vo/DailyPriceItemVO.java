package com.zhisu.hotel.pricing.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailyPriceItemVO {

    private LocalDate stayDate;

    private BigDecimal basePrice;

    private BigDecimal coefficient;

    private BigDecimal unitPrice;

    private Integer roomCount;

    private BigDecimal dailyAmount;

    private String priceSource;

    private String priceDescription;
}