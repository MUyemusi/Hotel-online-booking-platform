package com.zhisu.hotel.auth.service;

import com.zhisu.hotel.auth.dto.LoginRequest;
import com.zhisu.hotel.auth.dto.RegisterRequest;
import com.zhisu.hotel.auth.security.AuthenticatedUser;
import com.zhisu.hotel.auth.security.JwtService;
import com.zhisu.hotel.auth.vo.AuthTokenVO;
import com.zhisu.hotel.auth.vo.CurrentUserVO;
import com.zhisu.hotel.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.refresh-token-days:7}")
    private long refreshTokenDays;

    @Transactional
    public CurrentUserVO register(RegisterRequest request) {
        if (exists("username", request.getUsername())) throw new BusinessException("USERNAME_EXISTS", "账号已存在");
        if (exists("phone", request.getPhone())) throw new BusinessException("PHONE_EXISTS", "手机号已注册");
        if (request.getEmail() != null && !request.getEmail().isBlank() && exists("email", request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "邮箱已注册");
        }
        try {
            jdbcTemplate.update(
                    "INSERT INTO sys_user(username,password_hash,nickname,phone,email,status,consecutive_cancel_count) VALUES(?,?,?,?,?,'ENABLED',0)",
                    request.getUsername(), passwordEncoder.encode(request.getPassword()), request.getNickname(),
                    request.getPhone(), blankToNull(request.getEmail())
            );
        } catch (DuplicateKeyException e) {
            throw new BusinessException("USER_EXISTS", "账号、手机号或邮箱已存在");
        }
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username=?", Long.class, request.getUsername());
        Long roleId = jdbcTemplate.queryForObject("SELECT id FROM sys_role WHERE role_code='USER'", Long.class);
        jdbcTemplate.update("INSERT INTO sys_user_role(user_id,role_id) VALUES(?,?)", userId, roleId);
        return profile(userId);
    }

    @Transactional
    public AuthSession login(LoginRequest request) {
        UserRow user = findByUsername(request.getUsername());
        if (user == null || !"ENABLED".equalsIgnoreCase(user.status()) || !passwordEncoder.matches(request.getPassword(), user.passwordHash())) {
            throw new BusinessException("BAD_CREDENTIALS", "账号或密码错误");
        }
        jdbcTemplate.update("UPDATE sys_user SET last_login_at=NOW() WHERE id=?", user.id());
        return issueSession(user.id(), user.username());
    }

    @Transactional
    public AuthSession refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) throw new BusinessException("REFRESH_TOKEN_INVALID", "登录状态已失效");
        RefreshRow row = jdbcTemplate.query(
                "SELECT t.id,t.user_id,u.username,u.status,t.expires_at,t.revoked_at FROM auth_refresh_token t JOIN sys_user u ON u.id=t.user_id WHERE t.token_hash=? LIMIT 1",
                rs -> rs.next() ? new RefreshRow(rs.getLong("id"), rs.getLong("user_id"), rs.getString("username"),
                        rs.getString("status"), rs.getTimestamp("expires_at").toLocalDateTime(),
                        rs.getTimestamp("revoked_at") == null ? null : rs.getTimestamp("revoked_at").toLocalDateTime()) : null,
                hash(rawToken)
        );
        if (row == null || row.revokedAt() != null || row.expiresAt().isBefore(LocalDateTime.now())
                || !"ENABLED".equalsIgnoreCase(row.status())) {
            throw new BusinessException("REFRESH_TOKEN_INVALID", "登录状态已失效");
        }
        jdbcTemplate.update("UPDATE auth_refresh_token SET revoked_at=NOW() WHERE id=?", row.id());
        return issueSession(row.userId(), row.username());
    }

    public void logout(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            jdbcTemplate.update("UPDATE auth_refresh_token SET revoked_at=NOW() WHERE token_hash=? AND revoked_at IS NULL", hash(rawToken));
        }
    }

    public CurrentUserVO current(AuthenticatedUser user) {
        return profile(user.id());
    }

    private AuthSession issueSession(Long userId, String username) {
        List<String> roles = roles(userId);
        String accessToken = jwtService.createAccessToken(userId, username, roles);
        String refreshToken = randomToken();
        jdbcTemplate.update("INSERT INTO auth_refresh_token(user_id,token_hash,expires_at) VALUES(?,?,?)",
                userId, hash(refreshToken), Timestamp.valueOf(LocalDateTime.now().plusDays(refreshTokenDays)));
        return new AuthSession(
                AuthTokenVO.builder().accessToken(accessToken).expiresIn(jwtService.expiresInSeconds()).user(profile(userId)).build(),
                refreshToken,
                refreshTokenDays * 24 * 60 * 60
        );
    }

    private CurrentUserVO profile(Long userId) {
        CurrentUserVO profile = jdbcTemplate.query(
                "SELECT id,username,nickname,phone,email FROM sys_user WHERE id=? AND status='ENABLED'",
                rs -> rs.next() ? CurrentUserVO.builder().id(rs.getLong("id")).username(rs.getString("username"))
                        .nickname(rs.getString("nickname")).phone(rs.getString("phone")).email(rs.getString("email")).build() : null,
                userId
        );
        if (profile == null) throw new BusinessException("USER_NOT_FOUND", "用户不存在或已停用");
        profile.setRoles(roles(userId));
        return profile;
    }

    private List<String> roles(Long userId) {
        return jdbcTemplate.query(
                "SELECT r.role_code FROM sys_role r JOIN sys_user_role ur ON ur.role_id=r.id WHERE ur.user_id=? ORDER BY r.id",
                (rs, rowNum) -> rs.getString(1), userId
        );
    }

    private UserRow findByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT id,username,password_hash,status FROM sys_user WHERE username=? LIMIT 1",
                rs -> rs.next() ? new UserRow(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("status")) : null,
                username
        );
    }

    private boolean exists(String column, String value) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE " + column + "=?", Integer.class, value);
        return count != null && count > 0;
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
    private record UserRow(Long id, String username, String passwordHash, String status) {}
    private record RefreshRow(Long id, Long userId, String username, String status, LocalDateTime expiresAt, LocalDateTime revokedAt) {}
    public record AuthSession(AuthTokenVO response, String refreshToken, long refreshMaxAgeSeconds) {}
}