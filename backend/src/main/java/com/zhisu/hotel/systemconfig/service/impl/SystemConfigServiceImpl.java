package com.zhisu.hotel.systemconfig.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.systemconfig.dto.SystemConfigUpdateRequest;
import com.zhisu.hotel.systemconfig.entity.SystemConfig;
import com.zhisu.hotel.systemconfig.mapper.SystemConfigMapper;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {
    private final SystemConfigMapper systemConfigMapper;

    @Override
    public List<SystemConfig> listConfigs(String configGroup) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getStatus, "ENABLED")
                .orderByAsc(SystemConfig::getConfigGroup).orderByAsc(SystemConfig::getId);
        if (StringUtils.hasText(configGroup)) wrapper.eq(SystemConfig::getConfigGroup, configGroup.toUpperCase());
        return systemConfigMapper.selectList(wrapper);
    }

    @Override
    public SystemConfig getConfig(String configKey) {
        SystemConfig config = systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, configKey).eq(SystemConfig::getStatus, "ENABLED"));
        if (config == null) throw new BusinessException("SYSTEM_CONFIG_NOT_FOUND", "系统参数不存在：" + configKey);
        return config;
    }

    @Override
    @Transactional
    public SystemConfig updateConfig(String configKey, SystemConfigUpdateRequest request) {
        SystemConfig config = getConfig(configKey);
        String value = request.getConfigValue().trim();
        validateValue(configKey, config.getValueType(), value);
        config.setConfigValue(value);
        systemConfigMapper.updateById(config);
        return getConfig(configKey);
    }

    @Override public String getString(String key) { return getConfig(key).getConfigValue(); }
    @Override public Integer getInteger(String key) { return Integer.valueOf(getString(key)); }
    @Override public BigDecimal getDecimal(String key) { return new BigDecimal(getString(key)); }
    @Override public LocalTime getTime(String key) { return LocalTime.parse(getString(key)); }

    private void validateValue(String key, String type, String value) {
        try {
            switch (type) {
                case "INTEGER" -> {
                    int number = Integer.parseInt(value);
                    if (number < 0) throw new IllegalArgumentException();
                }
                case "DECIMAL" -> {
                    if (new BigDecimal(value).compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException();
                }
                case "TIME" -> LocalTime.parse(value);
                case "STRING" -> { if (!StringUtils.hasText(value)) throw new IllegalArgumentException(); }
                default -> throw new IllegalArgumentException();
            }
            if ("INTEGER".equals(type) || "DECIMAL".equals(type)) validateRange(key, new BigDecimal(value));
        } catch (Exception e) {
            throw new BusinessException("SYSTEM_CONFIG_VALUE_INVALID", "参数值格式或取值范围不正确");
        }
    }

    private void validateRange(String key, BigDecimal value) {
        switch (key) {
            case "PAYMENT_TIMEOUT_MINUTES" -> requireRange(value, 1, 1440);
            case "INVENTORY_WARNING_VALUE" -> requireRange(value, 0, 9999);
            case "IMAGE_UPLOAD_MAX_MB" -> requireRange(value, 1, 20);
            case "DEFAULT_HOLIDAY_COEFFICIENT", "WEEKEND_COEFFICIENT" -> requireRange(value, 0.1, 10);
            case "MAX_STAY_NIGHTS" -> requireRange(value, 1, 60);
            case "MAX_BOOKING_DAYS" -> requireRange(value, 1, 730);
            default -> { }
        }
    }

    private void requireRange(BigDecimal value, double min, double max) {
        if (value.compareTo(BigDecimal.valueOf(min)) < 0 || value.compareTo(BigDecimal.valueOf(max)) > 0) {
            throw new IllegalArgumentException();
        }
    }
}