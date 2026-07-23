package com.zhisu.hotel.assistant.controller;

import com.zhisu.hotel.assistant.dto.AssistantChatRequest;
import com.zhisu.hotel.assistant.service.HotelAssistantService;
import com.zhisu.hotel.assistant.vo.AssistantChatResponse;
import com.zhisu.hotel.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/assistant")
@RequiredArgsConstructor
public class PublicHotelAssistantController {

    private final HotelAssistantService hotelAssistantService;

    @PostMapping("/chat")
    public ApiResponse<AssistantChatResponse> chat(@Valid @RequestBody AssistantChatRequest request) {
        return ApiResponse.success(hotelAssistantService.chat(request));
    }
}
