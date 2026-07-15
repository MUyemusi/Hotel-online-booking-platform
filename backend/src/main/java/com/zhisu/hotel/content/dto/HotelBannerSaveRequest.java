package com.zhisu.hotel.content.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HotelBannerSaveRequest {

    @NotBlank(message = "轮播图标题不能为空")
    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;

    @NotBlank(message = "轮播图图片地址不能为空")
    @Size(max = 500, message = "图片地址不能超过500个字符")
    private String imageUrl;

    @Size(max = 500, message = "跳转地址不能超过500个字符")
    private String linkUrl;

    @NotNull(message = "排序序号不能为空")
    @Min(value = 0, message = "排序序号不能小于0")
    private Integer sortNo;
}
