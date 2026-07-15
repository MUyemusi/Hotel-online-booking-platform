package com.zhisu.hotel.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("scheduled_task_log")
public class ScheduledTaskLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private LocalDateTime executedAt;
    private Integer closedOrders;
    private Integer releasedRooms;
    private Integer success;
    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
