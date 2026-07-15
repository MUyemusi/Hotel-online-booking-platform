package com.zhisu.hotel.pricing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("price_operation_log")
public class PriceOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomTypeId;

    private String roomTypeName;

    private BigDecimal oldPrice;

    private BigDecimal newPrice;

    private String changeReason;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createdAt;
}
