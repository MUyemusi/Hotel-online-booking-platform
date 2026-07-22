package com.zhisu.hotel.auth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthTokenVO {
    private String accessToken;
    private long expiresIn;
    private CurrentUserVO user;
}