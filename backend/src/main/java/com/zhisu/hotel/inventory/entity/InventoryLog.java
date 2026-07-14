package com.zhisu.hotel.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("inventory_log")
public class InventoryLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomTypeId;

    private LocalDate inventoryDate;

    private String changeType;

    private Integer beforeValue;

    private Integer afterValue;

    private Integer changeAmount;

    private String operator;

    private String changeReason;

    private Long orderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
