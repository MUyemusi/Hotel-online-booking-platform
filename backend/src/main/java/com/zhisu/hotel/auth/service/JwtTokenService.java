package com.zhisu.hotel.auth.service;

import com.zhisu.hotel.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtTokenService(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public String generateToken(Long userId, String username, List<String> roles) {
        Instant now = clock.instant();
        Instant expirationTime = now.plusMillis(jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        Object userId = parseClaims(token).get("userId");
        return userId instanceof Number number ? number.longValue() : null;
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(Date.from(clock.instant()));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return jwtProperties.getExpiration() / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        String secret = jwtProperties.getSecret();

        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT密钥长度不能少于32个字符");
        }

        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}