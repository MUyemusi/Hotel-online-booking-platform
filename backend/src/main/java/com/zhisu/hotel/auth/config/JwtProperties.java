package com.zhisu.hotel.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签名密钥，至少32个字符。
     */
    private String secret;

    /**
     * Token有效期，单位为毫秒，默认2小时。
     */
    private long expiration = 7_200_000L;
}