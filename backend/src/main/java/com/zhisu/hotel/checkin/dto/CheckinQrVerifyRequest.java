package com.zhisu.hotel.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckinQrVerifyRequest {

    @NotBlank(message = "Verification credential is required")
    private String credential;
}
