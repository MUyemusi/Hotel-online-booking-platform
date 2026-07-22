package com.zhisu.hotel.systemconfig.service.impl;

import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.systemconfig.dto.SystemConfigUpdateRequest;
import com.zhisu.hotel.systemconfig.entity.SystemConfig;
import com.zhisu.hotel.systemconfig.mapper.SystemConfigMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SystemConfigServiceImplTest {
    @Test
    void rejectsOutOfRangeUploadLimit() {
        SystemConfigMapper mapper = mock(SystemConfigMapper.class);
        SystemConfig config = new SystemConfig();
        config.setId(1L); config.setConfigKey("IMAGE_UPLOAD_MAX_MB"); config.setConfigValue("5");
        config.setValueType("INTEGER"); config.setStatus("ENABLED");
        when(mapper.selectOne(any())).thenReturn(config);
        SystemConfigUpdateRequest request = new SystemConfigUpdateRequest();
        request.setConfigValue("21");

        assertThrows(BusinessException.class,
                () -> new SystemConfigServiceImpl(mapper).updateConfig("IMAGE_UPLOAD_MAX_MB", request));
        verify(mapper, never()).updateById(any(SystemConfig.class));
    }
}