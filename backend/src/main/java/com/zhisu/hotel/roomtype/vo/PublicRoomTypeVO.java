package com.zhisu.hotel.roomtype.vo;

import com.zhisu.hotel.pricing.vo.DailyPriceItemVO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PublicRoomTypeVO {
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
    private String description;
    private String mainImageUrl;
    private List<String> facilities;
    private Integer availableRooms;
    private BigDecimal totalAmount;
    private List<DailyPriceItemVO> dailyPrices;
}