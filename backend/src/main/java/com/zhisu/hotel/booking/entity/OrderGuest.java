package com.zhisu.hotel.booking.entity;

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
@TableName("order_guest")
public class OrderGuest {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String guestName;
    private String guestPhone;
    private String idType;
    private String idNumber;
    private Integer isMain;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
