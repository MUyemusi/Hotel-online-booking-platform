package com.zhisu.hotel.roomtype.entity;

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
@TableName("room_type_facility")
public class RoomTypeFacility {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomTypeId;

    private String facilityName;

    private String facilityIcon;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
