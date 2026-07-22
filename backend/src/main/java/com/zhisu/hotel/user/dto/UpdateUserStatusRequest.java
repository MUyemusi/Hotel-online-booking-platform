package com.zhisu.hotel.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotBlank(message = "用户状态不能为空")
    private String status;
}
