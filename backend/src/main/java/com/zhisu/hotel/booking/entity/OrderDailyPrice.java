package com.zhisu.hotel.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_daily_price")
public class OrderDailyPrice {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private LocalDate priceDate;
    private BigDecimal dailyPrice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
