package com.zhisu.hotel.pricing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("holiday_calendar")
public class HolidayCalendar {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate businessDate;
    private String holidayName;
    private String holidayCode;
    private String dayType;
    private BigDecimal coefficient;
    private Integer holidayYear;
    private String sourceStatus;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}