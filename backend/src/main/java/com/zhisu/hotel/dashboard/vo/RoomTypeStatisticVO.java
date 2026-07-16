package com.zhisu.hotel.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeStatisticVO {

    private Long roomTypeId;

    private String roomTypeName;

    private long bookingCount;

    private BigDecimal revenue;
}