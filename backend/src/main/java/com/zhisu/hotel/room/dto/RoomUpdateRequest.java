package com.zhisu.hotel.room.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateRequest {

    @NotNull(message = "Room type ID must not be null")
    private Long roomTypeId;

    private String remark;
}
