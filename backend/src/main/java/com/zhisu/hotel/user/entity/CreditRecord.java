package com.zhisu.hotel.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("credit_record")
public class CreditRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId;
    private String changeType;
    private Integer beforeCount;
    private Integer afterCount;
    private LocalDateTime banUntil;
    private String reason;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
