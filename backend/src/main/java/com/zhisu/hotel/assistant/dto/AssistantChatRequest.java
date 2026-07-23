package com.zhisu.hotel.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssistantChatRequest {

    @NotBlank(message = "提问内容不能为空")
    private String message;

    private List<AssistantMessage> history = new ArrayList<>();
}
