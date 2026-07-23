package com.zhisu.hotel.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HotelInfoUpdateRequest {

    @NotBlank(message = "酒店名称不能为空")
    @Size(max = 100, message = "酒店名称不能超过100个字符")
    private String hotelName;

    @Size(max = 500, message = "Logo地址不能超过500个字符")
    private String logoUrl;

    private String introduction;

    @Size(max = 255, message = "酒店地址不能超过255个字符")
    private String address;

    @Size(max = 30, message = "联系电话不能超过30个字符")
    private String contactPhone;

    private String facilities;

    private String checkinNotice;

    private String cancellationPolicy;

    private String trafficInfo;

    private String faqContent;

    private Double latitude;

    private Double longitude;
}
