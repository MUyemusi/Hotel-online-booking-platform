package com.zhisu.hotel.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateRequest {

    @NotBlank(message = "Room number must not be blank")
    private String roomNo;

    @NotNull(message = "Floor must not be null")
    private Integer floor;

    @NotNull(message = "Room type ID must not be null")
    private Long roomTypeId;

    private Boolean nearElevator;

    private Boolean headRoom;

    private Boolean tailRoom;

    private String cadImageUrl;

    private String remark;
}
