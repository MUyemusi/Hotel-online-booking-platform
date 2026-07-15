package com.zhisu.hotel.room.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhisu.hotel.common.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("room_assignment")
public class RoomAssignment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long roomId;

    @TableField("assignment_status")
    private AssignmentStatus assignmentStatus;

    private Long assignedBy;

    private LocalDateTime assignedAt;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private String requestToken;

    @Version
    private Integer version;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
