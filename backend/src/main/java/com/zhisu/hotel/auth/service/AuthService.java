package com.zhisu.hotel.auth.service;

import com.zhisu.hotel.auth.dto.LoginRequest;
import com.zhisu.hotel.auth.dto.LoginResponse;
import com.zhisu.hotel.auth.dto.RegisterRequest;

public interface AuthService {

    Long register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}