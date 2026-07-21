package com.zhisu.hotel.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnbanUserRequest {

    @NotBlank(message = "解除限制原因不能为空")
    @Size(max = 255, message = "解除限制原因不能超过255个字符")
    private String reason;
}
