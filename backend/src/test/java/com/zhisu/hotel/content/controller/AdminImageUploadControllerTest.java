package com.zhisu.hotel.content.controller;

import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdminImageUploadControllerTest {
    @Test
    void rejectsFileWhoseBytesDoNotMatchExtension() {
        SystemConfigService config = mock(SystemConfigService.class);
        when(config.getInteger("IMAGE_UPLOAD_MAX_MB")).thenReturn(5);
        AdminImageUploadController controller = new AdminImageUploadController(config);
        MockMultipartFile fake = new MockMultipartFile("file", "fake.jpg", "image/jpeg", "not-an-image".getBytes());

        assertThrows(BusinessException.class, () -> controller.upload(fake));
    }
}