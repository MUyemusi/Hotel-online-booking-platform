package com.zhisu.hotel.auth.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    @Test
    void createsAndParsesAccessToken() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", "0123456789012345678901234567890123456789");
        ReflectionTestUtils.setField(service, "accessTokenMinutes", 15L);
        service.initialize();

        String token = service.createAccessToken(7L, "guest", List.of("USER"));
        Claims claims = service.parse(token);

        assertEquals("guest", claims.getSubject());
        assertEquals(7L, claims.get("uid", Long.class));
        assertEquals(List.of("USER"), claims.get("roles", List.class));
        assertEquals(900, service.expiresInSeconds());
    }
}