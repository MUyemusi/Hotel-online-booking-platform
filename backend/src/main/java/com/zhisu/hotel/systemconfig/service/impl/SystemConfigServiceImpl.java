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
                .orderByAsc(SystemConfig::getConfigGroup)
                .orderByAsc(SystemConfig::getId);

        if (StringUtils.hasText(configGroup)) {
            wrapper.eq(SystemConfig::getConfigGroup, configGroup.toUpperCase());
        }

        return systemConfigMapper.selectList(wrapper);
    }

    @Override
    public SystemConfig getConfig(String configKey) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, configKey)
                        .eq(SystemConfig::getStatus, "ENABLED")
        );

        if (config == null) {
            throw new BusinessException(
                    "SYSTEM_CONFIG_NOT_FOUND",
                    "系统参数不存在：" + configKey
            );
        }

        return config;
    }

    @Override
    @Transactional
    public SystemConfig updateConfig(String configKey, SystemConfigUpdateRequest request) {
        SystemConfig config = getConfig(configKey);
        String newValue = request.getConfigValue().trim();

        validateValue(config.getValueType(), newValue);

        config.setConfigValue(newValue);
        systemConfigMapper.updateById(config);

        return getConfig(configKey);
    }

    @Override
    public String getString(String configKey) {
        return getConfig(configKey).getConfigValue();
    }

    @Override
    public Integer getInteger(String configKey) {
        return Integer.valueOf(getString(configKey));
    }

    @Override
    public BigDecimal getDecimal(String configKey) {
        return new BigDecimal(getString(configKey));
    }

    @Override
    public LocalTime getTime(String configKey) {
        return LocalTime.parse(getString(configKey));
    }

    private void validateValue(String valueType, String value) {
        try {
            switch (valueType) {
                case "INTEGER" -> {
                    int number = Integer.parseInt(value);
                    if (number < 0) {
                        throw new IllegalArgumentException();
                    }
                }
                case "DECIMAL" -> {
                    BigDecimal number = new BigDecimal(value);
                    if (number.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException();
                    }
                }
                case "TIME" -> LocalTime.parse(value);
                case "STRING" -> {
                    if (!StringUtils.hasText(value)) {
                        throw new IllegalArgumentException();
                    }
                }
                default -> throw new IllegalArgumentException();
            }
        } catch (Exception exception) {
            throw new BusinessException(
                    "SYSTEM_CONFIG_VALUE_INVALID",
                    "参数值格式与参数类型不匹配"
            );
        }
    }
}
