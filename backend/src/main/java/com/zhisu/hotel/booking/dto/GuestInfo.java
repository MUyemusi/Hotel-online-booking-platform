package com.zhisu.hotel.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestInfo {

    @NotBlank(message = "入住人姓名不能为空")
    private String guestName;

    private String guestPhone;
    private String idType;
    private String idNumber;
    private Boolean isMain = false;
}
