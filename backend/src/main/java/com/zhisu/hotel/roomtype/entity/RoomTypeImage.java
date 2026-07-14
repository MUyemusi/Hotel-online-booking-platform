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
@TableName("room_type_image")
public class RoomTypeImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomTypeId;

    private String imageUrl;

    private String imageType;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
