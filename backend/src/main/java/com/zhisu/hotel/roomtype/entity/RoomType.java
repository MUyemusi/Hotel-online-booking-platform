package com.zhisu.hotel.roomtype.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("room_type")
public class RoomType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;

    private BigDecimal basePrice;

    private Integer area;

    private String bedType;

    private Integer bedCount;

    private Integer maxGuests;

    private Integer hasBreakfast;

    private Integer hasWindow;

    private Integer hasWifi;

    private Integer hasAc;

    private Integer allowPets;

    private String description;

    private String status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
