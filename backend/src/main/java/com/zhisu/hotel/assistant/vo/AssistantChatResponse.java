package com.zhisu.hotel.assistant.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatResponse {
    private String answer;
    private String provider;
    private boolean usedRealtimeWeather;
    private String weatherSummary;
}
