package com.zhisu.hotel.roomtype.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeSummary {

    private Long id;

    private String name;

    private String category;

    private BigDecimal basePrice;

    private Integer area;

    private String bedType;

    private Integer bedCount;

    private Integer maxGuests;

    private Integer hasBreakfast;

    private Integer hasWindow;

    private Integer hasWifi;

    private Integer hasAc;

    private Integer allowPets;

    private String description;

    private String status;
}
