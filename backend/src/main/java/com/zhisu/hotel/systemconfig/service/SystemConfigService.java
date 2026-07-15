package com.zhisu.hotel.systemconfig.service;

import com.zhisu.hotel.systemconfig.dto.SystemConfigUpdateRequest;
import com.zhisu.hotel.systemconfig.entity.SystemConfig;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public interface SystemConfigService {

    List<SystemConfig> listConfigs(String configGroup);

    SystemConfig getConfig(String configKey);

    SystemConfig updateConfig(String configKey, SystemConfigUpdateRequest request);

    String getString(String configKey);

    Integer getInteger(String configKey);

    BigDecimal getDecimal(String configKey);

    LocalTime getTime(String configKey);
}
