package com.zhisu.hotel.room.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportCleaningIssueRequest {

    @Size(max = 128, message = "Request token must not exceed 128 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Request token format is invalid")
    private String requestToken;

    @Size(max = 500, message = "Issue reason must not exceed 500 characters")
    private String reason;

    @Size(max = 200, message = "Remark must not exceed 200 characters")
    private String remark;
}
