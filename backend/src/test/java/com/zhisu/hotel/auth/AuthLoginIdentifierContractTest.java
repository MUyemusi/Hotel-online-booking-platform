package com.zhisu.hotel.auth;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AuthLoginIdentifierContractTest {

    @Test
    void loginAcceptsUsernameOrPhoneWithoutChangingTheJsonField() throws IOException {
        String service = Files.readString(Path.of("src/main/java/com/zhisu/hotel/auth/service/AuthService.java"));
        String request = Files.readString(Path.of("src/main/java/com/zhisu/hotel/auth/dto/LoginRequest.java"));
        String loginView = Files.readString(Path.of("..", "frontend", "src", "modules", "auth", "Login.vue"));

        assertThat(service).contains("WHERE username=? OR phone=?");
        assertThat(service).contains("ORDER BY CASE WHEN username=? THEN 0 ELSE 1 END");
        assertThat(request).contains("private String username;");
        assertThat(loginView).contains("用户名或手机号", "v-model.trim=\"form.username\"");
    }
}
