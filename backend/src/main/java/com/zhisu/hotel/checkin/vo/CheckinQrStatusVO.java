package com.zhisu.hotel.checkin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinQrStatusVO {

    private Boolean hasActiveToken;
    private Long tokenId;
    private String tokenStatus;
    private LocalDateTime expiresAt;
    private Boolean expired;
    private String verificationCode;
}
