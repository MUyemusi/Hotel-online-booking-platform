package com.zhisu.hotel.room.dto;

import com.zhisu.hotel.common.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomQueryRequest {

    private Integer floor;

    private Long roomTypeId;

    private RoomStatus roomStatus;

    private String roomNo;
}
