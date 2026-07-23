
package com.zhisu.hotel.pricing.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PriceQuoteVO {

    private Long roomTypeId;

    private String roomTypeName;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private String stayType;

    private Integer stayNights;

    private Integer roomCount;

    private BigDecimal oneRoomAmount;

    private BigDecimal totalAmount;

    private List<DailyPriceItemVO> dailyPrices;
}
