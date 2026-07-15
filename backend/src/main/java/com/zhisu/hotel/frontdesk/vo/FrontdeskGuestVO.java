package com.zhisu.hotel.frontdesk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontdeskGuestVO {

    private Long guestId;
    private String guestName;
    private String maskedGuestPhone;
    private String idType;
    private String maskedIdNumber;
    private Boolean main;
}
