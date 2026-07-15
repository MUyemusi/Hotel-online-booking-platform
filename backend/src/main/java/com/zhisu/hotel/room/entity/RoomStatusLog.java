package com.zhisu.hotel.room.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhisu.hotel.common.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("room_status_log")
public class RoomStatusLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    @TableField("before_status")
    private RoomStatus beforeStatus;

    @TableField("after_status")
    private RoomStatus afterStatus;

    private Long operatorId;

    private String operatorType;

    private String reason;

    private Long relatedOrderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
