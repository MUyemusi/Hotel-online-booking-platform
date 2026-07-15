package com.zhisu.hotel.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckinQrVerifyRequest {

    @NotBlank(message = "QR content is required")
    private String qrContent;
}
