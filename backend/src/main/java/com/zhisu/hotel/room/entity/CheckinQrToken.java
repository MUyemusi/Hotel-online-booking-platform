package com.zhisu.hotel.room.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhisu.hotel.common.enums.QrTokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("checkin_qr_token")
public class CheckinQrToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String tokenHash;

    @TableField("token_status")
    private QrTokenStatus tokenStatus;

    private LocalDateTime expireAt;

    private LocalDateTime usedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
