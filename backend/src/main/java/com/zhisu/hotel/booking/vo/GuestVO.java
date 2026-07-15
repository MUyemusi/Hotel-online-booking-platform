package com.zhisu.hotel.booking.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestVO {

    private Long id;
    private String guestName;
    private String guestPhone;
    private String idType;
    private String idNumber;
    private Boolean isMain;
}
