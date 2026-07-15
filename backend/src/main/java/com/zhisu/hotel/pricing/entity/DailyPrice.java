package com.zhisu.hotel.pricing.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("daily_price")
public class DailyPrice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomTypeId;

    private LocalDate businessDate;

    private BigDecimal basePrice;

    private BigDecimal dateCoefficient;

    private BigDecimal finalPrice;

    private String priceSource;

    private String priceDescription;

    private Long holidayCalendarId;

    private String sourceStatus;

    private BigDecimal manualPrice;

    private Integer manualOverride;

    private String overrideReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}