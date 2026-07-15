package com.zhisu.hotel.room.dto;

import com.zhisu.hotel.common.enums.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusChangeRequest {

    @NotNull(message = "Target status must not be null")
    private RoomStatus targetStatus;

    private String reason;
}
