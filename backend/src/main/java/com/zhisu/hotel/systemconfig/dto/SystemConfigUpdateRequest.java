package com.zhisu.hotel.systemconfig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemConfigUpdateRequest {

    @NotBlank(message = "参数值不能为空")
    @Size(max = 500, message = "参数值不能超过500个字符")
    private String configValue;
}
