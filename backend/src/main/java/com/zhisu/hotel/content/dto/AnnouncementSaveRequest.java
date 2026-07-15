package com.zhisu.hotel.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementSaveRequest {

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 150, message = "公告标题不能超过150个字符")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    @NotNull(message = "是否置顶不能为空")
    private Boolean top;
}
