package com.zhisu.hotel.audit.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogVO {
    private Long id;
    private Long operatorId;
    private String module;
    private String action;
    private String requestMethod;
    private String requestUri;
    private String result;
    private String ipAddress;
    private String detail;
    private LocalDateTime createdAt;
}
