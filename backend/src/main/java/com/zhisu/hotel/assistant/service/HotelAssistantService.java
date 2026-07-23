package com.zhisu.hotel.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhisu.hotel.assistant.dto.AssistantChatRequest;
import com.zhisu.hotel.assistant.dto.AssistantMessage;
import com.zhisu.hotel.assistant.vo.AssistantChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HotelAssistantService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);

    private final ObjectMapper objectMapper;

    @Value("${assistant.deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;

    @Value("${assistant.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${assistant.deepseek.model:deepseek-chat}")
    private String deepseekModel;

    @Value("${assistant.deepseek.connect-timeout-seconds:10}")
    private long connectTimeoutSeconds;

    @Value("${assistant.deepseek.read-timeout-seconds:45}")
    private long readTimeoutSeconds;

    public AssistantChatResponse chat(AssistantChatRequest request) {
        String question = request.getMessage().trim();
        List<AssistantMessage> history = sanitizeHistory(request.getHistory(), question);
        String weatherSummary = tryFetchWeatherSummary(question);

        if (StringUtils.hasText(deepseekApiKey)) {
            try {
                String answer = callDeepSeek(question, history, weatherSummary);
                if (StringUtils.hasText(answer)) {
                    return new AssistantChatResponse(answer.trim(), "deepseek", StringUtils.hasText(weatherSummary), weatherSummary);
                }
            } catch (Exception ignored) {
                // DeepSeek 异常时自动降级到本地知识回复，避免小助手整体不可用
            }
        }

        return new AssistantChatResponse(
                buildLocalReply(question, weatherSummary),
                "local",
                StringUtils.hasText(weatherSummary),
                weatherSummary
        );
    }

    private List<AssistantMessage> sanitizeHistory(List<AssistantMessage> rawHistory, String question) {
        if (rawHistory == null || rawHistory.isEmpty()) {
            return List.of();
        }

        List<AssistantMessage> filtered = new ArrayList<>();
        for (AssistantMessage item : rawHistory) {
            if (item == null || !StringUtils.hasText(item.getContent()) || !StringUtils.hasText(item.getRole())) {
                continue;
            }
            String role = item.getRole().trim().toLowerCase(Locale.ROOT);
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }
            filtered.add(new AssistantMessage(role, item.getContent().trim()));
        }

        if (!filtered.isEmpty()) {
            AssistantMessage last = filtered.get(filtered.size() - 1);
            if ("user".equals(last.getRole()) && question.equals(last.getContent())) {
                filtered.remove(filtered.size() - 1);
            }
        }

        return filtered.size() > 10
                ? new ArrayList<>(filtered.subList(filtered.size() - 10, filtered.size()))
                : filtered;
    }

    private String callDeepSeek(String question, List<AssistantMessage> history, String weatherSummary) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(weatherSummary)));
        for (AssistantMessage item : history) {
            messages.add(Map.of("role", item.getRole(), "content", item.getContent()));
        }
        messages.add(Map.of("role", "user", "content", question));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", deepseekModel);
        payload.put("temperature", 0.55);
        payload.put("messages", messages);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(deepseekBaseUrl) + "/chat/completions"))
                .timeout(Duration.ofSeconds(readTimeoutSeconds))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + deepseekApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("DeepSeek API 调用失败: HTTP " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        return content.isMissingNode() ? "" : content.asText("");
    }

    private String buildSystemPrompt(String weatherSummary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是智宿酒店在线预订与运营管理系统的专属小助手，名字叫“小智”。")
                .append("你的回答必须满足以下要求：")
                .append("1. 始终使用简体中文；")
                .append("2. 始终以智宿酒店助手身份回答，不要自称其他模型；")
                .append("3. 语气专业、亲切、简洁，优先给出可执行建议；")
                .append("4. 当用户询问系统如何使用时，优先结合酒店预订、入住、退房、房态、信用中心、前台与后台功能作答；")
                .append("5. 当用户询问周边旅游时，默认以三亚亚龙湾及酒店周边为中心回答；")
                .append("6. 如果用户未说明地点而直接问今天天气，默认理解为酒店所在地“三亚亚龙湾”；")
                .append("7. 不编造不存在的后台权限和业务规则；")
                .append("8. 若涉及账号、支付、入住核验等敏感流程，提醒用户以系统页面和酒店前台确认为准。");

        prompt.append("已知酒店与系统知识如下：")
                .append("项目名称为“智宿酒店在线预订与运营管理系统”，技术栈为 Spring Boot 3、Vue 3、MySQL 8。")
                .append("酒店楼层为 3 至 8 层，每层 20 间，房号 301-320 到 801-820，共 120 间。")
                .append("住客端支持浏览房型、查询库存、在线预订、查看订单、支付、查看信用中心、修改资料与密码。")
                .append("前台角色负责订单核验、扫码核验、入住、退房、房态查看。")
                .append("管理员负责房态管理、清洁维修闭环、内容运营、价格与库存配置、用户与角色管理。")
                .append("入住时需要校验订单支付状态、房型匹配、房间可用性与有效二维码。")
                .append("房态流转遵循 AVAILABLE 到 OCCUPIED 到 DIRTY 到 AVAILABLE 的主链路，AVAILABLE 可与 MAINTENANCE 或 DISABLED 互转，DIRTY 可转 MAINTENANCE。")
                .append("酒店周边推荐景点包括：亚龙湾中心广场、亚龙湾沙滩、亚龙湾海底世界、亚龙湾热带天堂森林公园、博后村、太阳湾路。")
                .append("酒店地址位于海南省三亚市吉阳区亚龙湾国家旅游度假区。");

        if (StringUtils.hasText(weatherSummary)) {
            prompt.append("当前获取到的实时天气参考：").append(weatherSummary).append("。");
        } else {
            prompt.append("当前没有可用的实时天气数据时，不要伪造具体温度与天气现象，可明确说明暂未获取成功。");
        }

        return prompt.toString();
    }

    private String tryFetchWeatherSummary(String question) {
        if (!needsWeather(question)) {
            return "";
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(Math.min(connectTimeoutSeconds, 8)))
                    .build();

            String city = URLEncoder.encode("Sanya", StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://wttr.in/" + city + "?format=j1"))
                    .timeout(Duration.ofSeconds(Math.min(readTimeoutSeconds, 12)))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "";
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode current = root.path("current_condition").path(0);
            if (current.isMissingNode()) {
                return "";
            }

            String weatherText = translateWeatherDesc(current.path("weatherDesc").path(0).path("value").asText(""));
            String temp = current.path("temp_C").asText("");
            String feelsLike = current.path("FeelsLikeC").asText("");
            String humidity = current.path("humidity").asText("");
            String wind = current.path("windspeedKmph").asText("");

            return String.format(
                    Locale.CHINA,
                    "%s三亚亚龙湾天气：%s，当前气温%s℃，体感%s℃，湿度%s%%，风速%s公里/小时",
                    LocalDate.now().format(DATE_FORMATTER),
                    defaultText(weatherText, "天气情况未知"),
                    defaultText(temp, "-"),
                    defaultText(feelsLike, "-"),
                    defaultText(humidity, "-"),
                    defaultText(wind, "-")
            );
        } catch (Exception ignored) {
            return "";
        }
    }

    private boolean needsWeather(String question) {
        String text = question.toLowerCase(Locale.ROOT);
        return text.contains("天气")
                || text.contains("温度")
                || text.contains("下雨")
                || text.contains("气温")
                || text.contains("降温")
                || text.contains("weather");
    }

    private String buildLocalReply(String question, String weatherSummary) {
        String text = question.toLowerCase(Locale.ROOT);

        if (needsWeather(question)) {
            if (StringUtils.hasText(weatherSummary)) {
                return weatherSummary + "。如果您计划外出游玩，建议随身携带遮阳用品并根据实时天气调整海边行程。";
            }
            return "我暂时没有成功获取到三亚亚龙湾的实时天气数据。您可以稍后再问我一次，或先咨询前台获取当日出行建议。";
        }

        if (text.contains("预订") || text.contains("订房") || text.contains("房间")) {
            return "住客端预订流程一般为：进入首页或客房页面，选择入住与离店日期、人数后查询房型；进入房型详情确认信息；填写入住人资料并提交订单；完成支付后即可在订单页查看状态。";
        }

        if (text.contains("入住") || text.contains("退房") || text.contains("二维码") || text.contains("扫码")) {
            return "办理入住时，系统会校验订单支付状态、房型匹配情况、房间可用性以及二维码是否有效。前台可通过工作台进行订单检索、扫码核验、确认入住与退房处理。";
        }

        if (text.contains("前台") || text.contains("工作台") || text.contains("后台") || text.contains("管理员")) {
            return "系统按角色区分功能：住客端用于预订、订单与个人中心；前台工作台用于核验订单、入住退房和房态查看；管理员后台用于房态、清洁维修、内容、价格、库存、用户和角色管理。";
        }

        if (text.contains("景点") || text.contains("周边") || text.contains("游玩") || text.contains("三亚")) {
            return "酒店周边推荐您优先关注亚龙湾中心广场、亚龙湾沙滩、亚龙湾海底世界、亚龙湾热带天堂森林公园、博后村和太阳湾路。若您偏好海景漫步，可先去亚龙湾沙滩和太阳湾路；若偏好山海观景，可选择热带天堂森林公园。";
        }

        return "我是智宿酒店小助手小智，可以为您介绍酒店预订、入住退房流程、前台与后台功能、信用中心使用方式，以及三亚亚龙湾周边景点和天气信息。您可以再告诉我想了解的具体问题。";
    }

    private String trimTrailingSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String translateWeatherDesc(String value) {
        if (!StringUtils.hasText(value)) {
            return "天气情况未知";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        Map<String, String> mapping = Map.ofEntries(
                Map.entry("sunny", "晴"),
                Map.entry("clear", "晴朗"),
                Map.entry("partly cloudy", "局部多云"),
                Map.entry("cloudy", "多云"),
                Map.entry("overcast", "阴"),
                Map.entry("mist", "薄雾"),
                Map.entry("fog", "有雾"),
                Map.entry("light rain", "小雨"),
                Map.entry("moderate rain", "中雨"),
                Map.entry("heavy rain", "大雨"),
                Map.entry("patchy rain nearby", "附近有阵雨"),
                Map.entry("light drizzle", "毛毛雨"),
                Map.entry("thundery outbreaks nearby", "附近有雷阵雨"),
                Map.entry("light rain with thunderstorm", "雷阵雨"),
                Map.entry("moderate or heavy rain with thunderstorm", "中到大雷雨"),
                Map.entry("patchy light rain with thunder", "局部雷阵雨"),
                Map.entry("light rain shower", "短时小雨"),
                Map.entry("moderate or heavy rain shower", "阵雨"),
                Map.entry("patchy light drizzle", "零星毛毛雨")
        );

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (normalized.equals(entry.getKey())) {
                return entry.getValue();
            }
        }

        return value;
    }
}
