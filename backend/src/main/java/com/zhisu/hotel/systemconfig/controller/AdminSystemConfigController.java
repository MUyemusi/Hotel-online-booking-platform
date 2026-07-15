package com.zhisu.hotel.systemconfig.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.systemconfig.dto.SystemConfigUpdateRequest;
import com.zhisu.hotel.systemconfig.entity.SystemConfig;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/system-configs")
@RequiredArgsConstructor
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ApiResponse<List<SystemConfig>> listConfigs(@RequestParam(required = false) String group) {
        return ApiResponse.success(systemConfigService.listConfigs(group));
    }

    @GetMapping("/{configKey}")
    public ApiResponse<SystemConfig> getConfig(@PathVariable String configKey) {
        return ApiResponse.success(systemConfigService.getConfig(configKey));
    }

    @PutMapping("/{configKey}")
    public ApiResponse<SystemConfig> updateConfig(
            @PathVariable String configKey,
            @Valid @RequestBody SystemConfigUpdateRequest request) {
        return ApiResponse.success(systemConfigService.updateConfig(configKey, request));
    }
}
