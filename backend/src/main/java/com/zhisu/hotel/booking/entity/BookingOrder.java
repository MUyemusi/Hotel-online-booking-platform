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
@TableName("booking_order")
public class BookingOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long roomTypeId;
    private String roomTypeName;
    private String roomTypeSnapshot;
    private String facilitiesSnapshot;
    private Integer roomCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer totalNights;
    private Integer guestCount;
    private BigDecimal totalAmount;
    private Integer totalAmountFen;
    private String idempotencyKey;
    private String phone;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime paymentTimeout;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private String cancelType;
    private String cancelReason;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
