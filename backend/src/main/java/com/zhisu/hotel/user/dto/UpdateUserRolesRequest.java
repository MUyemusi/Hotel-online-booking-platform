package com.zhisu.hotel.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRolesRequest {

    @NotEmpty(message = "用户角色不能为空")
    private Set<String> roleCodes;
}
