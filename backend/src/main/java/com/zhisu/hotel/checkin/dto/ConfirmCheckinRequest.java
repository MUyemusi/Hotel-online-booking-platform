package com.zhisu.hotel.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ConfirmCheckinRequest {

    @NotBlank(message = "QR content is required")
    private String qrContent;

    @NotEmpty(message = "Room ids are required")
    private List<Long> roomIds;

    @Size(max = 128, message = "Request token must not exceed 128 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "Request token format is invalid")
    private String requestToken;

    @Size(max = 200, message = "Remark must not exceed 200 characters")
    private String remark;
}
