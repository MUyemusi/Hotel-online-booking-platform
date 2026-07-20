package com.zhisu.hotel.auth.service;

import com.zhisu.hotel.auth.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {


    private JwtProperties jwtProperties;
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(
                "01234567890123456789012345678901"
        );
        jwtProperties.setExpiration(7_200_000L);

        Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        jwtTokenService = new JwtTokenService(jwtProperties, clock);
    }

    @Test
    void shouldGenerateAndParseValidToken() {
        String token = jwtTokenService.generateToken(
                4L,
                "student20260720",
                List.of("USER")
        );

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
        assertEquals(
                "student20260720",
                jwtTokenService.getUsername(token)
        );
        assertEquals(4L, jwtTokenService.getUserId(token));
        assertTrue(jwtTokenService.isTokenValid(token));
        assertEquals(
                7200L,
                jwtTokenService.getExpirationSeconds()
        );
    }

    @Test
    void shouldRejectMalformedToken() {
        assertFalse(
                jwtTokenService.isTokenValid("not-a-valid-token")
        );

        assertThrows(
                JwtException.class,
                () -> jwtTokenService.getUsername(
                        "not-a-valid-token"
                )
        );
    }

    @Test
    void shouldRejectSecretShorterThan32Characters() {
        jwtProperties.setSecret("too-short");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> jwtTokenService.generateToken(
                        1L,
                        "admin",
                        List.of("ADMIN")
                )
        );

        assertEquals(
                "JWT密钥长度不能少于32个字符",
                exception.getMessage()
        );
    }
}