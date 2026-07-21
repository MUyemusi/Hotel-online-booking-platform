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
public class CreditSummaryVO {
    private Long userId;
    private Integer consecutiveCancelCount;
    private Integer cancellationLimit;
    private LocalDateTime banUntil;
    private Boolean bookingAllowed;
}
