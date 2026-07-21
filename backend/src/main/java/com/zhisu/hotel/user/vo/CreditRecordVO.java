package com.zhisu.hotel.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRecordVO {
    private Long id;
    private Long userId;
    private Long orderId;
    private String changeType;
    private Integer beforeCount;
    private Integer afterCount;
    private LocalDateTime banUntil;
    private String reason;
    private Long operatorId;
    private LocalDateTime createdAt;
}
