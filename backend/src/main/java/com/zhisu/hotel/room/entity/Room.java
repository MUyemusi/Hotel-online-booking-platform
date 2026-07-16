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
@TableName("room")
public class Room {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomNo;

    private Integer floor;

    private Long roomTypeId;

    @TableField("room_status")
    private RoomStatus roomStatus;

    private String maintenanceReason;

    private String remark;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}
