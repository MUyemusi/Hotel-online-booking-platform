package com.zhisu.hotel.auth.controller;

import com.zhisu.hotel.auth.dto.LoginRequest;
import com.zhisu.hotel.auth.dto.RegisterRequest;
import com.zhisu.hotel.auth.security.CurrentUserProvider;
import com.zhisu.hotel.auth.service.AuthService;
import com.zhisu.hotel.auth.vo.AuthTokenVO;
import com.zhisu.hotel.auth.vo.CurrentUserVO;
import com.zhisu.hotel.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "zhisu_refresh_token";
    private final AuthService authService;

    @Value("${auth.cookie-secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ApiResponse<CurrentUserVO> register(@Valid @RequestBody RegisterRequest request) {
import com.zhisu.hotel.auth.dto.LoginResponse;
import com.zhisu.hotel.auth.dto.RegisterRequest;
import com.zhisu.hotel.auth.service.AuthService;
import com.zhisu.hotel.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> register(
            @Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenVO> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.AuthSession session = authService.login(request);
        writeRefreshCookie(response, session.refreshToken(), session.refreshMaxAgeSeconds());
        return ApiResponse.success(session.response());
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenVO> refresh(@CookieValue(value = REFRESH_COOKIE, required = false) String token,
                                             HttpServletResponse response) {
        AuthService.AuthSession session = authService.refresh(token);
        writeRefreshCookie(response, session.refreshToken(), session.refreshMaxAgeSeconds());
        return ApiResponse.success(session.response());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@CookieValue(value = REFRESH_COOKIE, required = false) String token,
                                    HttpServletResponse response) {
        authService.logout(token);
        writeRefreshCookie(response, "", 0);
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVO> me() {
        return ApiResponse.success(authService.current(CurrentUserProvider.require()));
    }

    private void writeRefreshCookie(HttpServletResponse response, String token, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        // JWT为无状态认证，前端删除本地Token即可退出。
        return ApiResponse.success();
    }
}