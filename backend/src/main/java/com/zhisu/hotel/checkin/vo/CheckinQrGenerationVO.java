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
public class CheckinQrGenerationVO {

    private Long tokenId;
    private Long orderId;
    private String orderNo;
    private String tokenStatus;
    private LocalDateTime expiresAt;
    private LocalDateTime generatedAt;
    private String verificationCode;
    private String qrContent;
    private String qrImageDataUrl;
}
