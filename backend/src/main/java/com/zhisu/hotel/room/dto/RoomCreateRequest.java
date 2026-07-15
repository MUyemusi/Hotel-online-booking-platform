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

    @NotBlank(message = "房间号不能为空")
    private String roomNo;

    @NotNull(message = "楼层不能为空")
    private Integer floor;

    @NotNull(message = "房型ID不能为空")
    private Long roomTypeId;

    private String remark;
}
