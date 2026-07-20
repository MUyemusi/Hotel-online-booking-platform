package com.zhisu.hotel.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private String tokenType;

    private long expiresIn;

    private Long userId;

    private String username;

    private String nickname;

    private List<String> roles;
}