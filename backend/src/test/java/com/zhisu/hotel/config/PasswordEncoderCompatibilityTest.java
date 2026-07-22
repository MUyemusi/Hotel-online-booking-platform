package com.zhisu.hotel.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderCompatibilityTest {

    private final PasswordEncoder passwordEncoder =
            new SecurityConfig().passwordEncoder();

    @Test
    void shouldMatchLegacyBcryptHashWithoutPrefix() {
        String encoded = new BCryptPasswordEncoder().encode("admin123");

        assertTrue(passwordEncoder.matches("admin123", encoded));
    }

    @Test
    void shouldMatchNewDelegatingPasswordHash() {
        String encoded = passwordEncoder.encode("Test123456");

        assertTrue(passwordEncoder.matches("Test123456", encoded));
    }
}
