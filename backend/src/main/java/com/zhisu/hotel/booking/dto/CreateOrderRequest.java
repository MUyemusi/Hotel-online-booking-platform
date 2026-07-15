package com.zhisu.hotel.booking.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "房型ID不能为空")
    private Long roomTypeId;

    @NotNull(message = "入住日期不能为空")
    @FutureOrPresent(message = "入住日期不能早于今天")
    private LocalDate checkInDate;

    @NotNull(message = "离店日期不能为空")
    @Future(message = "离店日期必须在未来")
    private LocalDate checkOutDate;

    @NotNull(message = "房间数量不能为空")
    @Min(value = 1, message = "至少预订1间")
    @Max(value = 5, message = "最多预订5间")
    private Integer roomCount;

    @NotNull(message = "入住人数不能为空")
    @Min(value = 1, message = "至少1位入住人")
    private Integer guestCount;

    @NotEmpty(message = "入住人信息不能为空")
    private List<GuestInfo> guests;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;

    private String remark;
}
