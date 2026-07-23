package com.zhisu.hotel.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull(message = "房型ID不能为空")
    private Long roomTypeId;

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 500, message = "评价内容不能超过500个字符")
    private String content;
}
