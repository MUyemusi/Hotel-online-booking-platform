package com.zhisu.hotel.auth.controller;

import com.zhisu.hotel.auth.dto.LoginRequest;
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