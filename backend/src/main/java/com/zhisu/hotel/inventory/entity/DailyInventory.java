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
@TableName("daily_inventory")
public class DailyInventory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomTypeId;

    private LocalDate inventoryDate;

    private Integer totalRooms;

    private Integer lockedRooms;

    private Integer bookedRooms;

    private Integer availableRooms;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
