package com.zhisu.hotel.audit.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhisu.hotel.audit.entity.OperationLog;
import com.zhisu.hotel.audit.service.OperationLogService;
import com.zhisu.hotel.common.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
public class OperationLogFilter extends OncePerRequestFilter {

    private static final Set<String> AUDITED_METHODS =
            Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final int URI_LIMIT = 255;
    private static final int IP_LIMIT = 45;
    private static final int DETAIL_LIMIT = 500;

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    public OperationLogFilter(
            OperationLogService operationLogService,
            ObjectMapper objectMapper) {
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!shouldAudit(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startNanos = System.nanoTime();
        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);
        String failure = null;
        try {
            filterChain.doFilter(request, responseWrapper);
        } catch (ServletException | IOException | RuntimeException exception) {
            failure = exception.getClass().getSimpleName();
            throw exception;
        } finally {
            try {
                recordOperation(request, responseWrapper, startNanos, failure);
            } catch (RuntimeException exception) {
                log.error("Failed to persist operation log", exception);
            } finally {
                responseWrapper.copyBodyToResponse();
            }
        }
    }

    private boolean shouldAudit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null
                && uri.startsWith("/api/")
                && AUDITED_METHODS.contains(request.getMethod().toUpperCase(Locale.ROOT));
    }

    private void recordOperation(
            HttpServletRequest request,
            ContentCachingResponseWrapper response,
            long startNanos,
            String exceptionName) {
        ResponseOutcome outcome = resolveOutcome(response, exceptionName);
        long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
        String detail = "httpStatus=" + response.getStatus()
                + "; responseCode=" + outcome.code()
                + "; durationMs=" + durationMillis;
        if (outcome.message() != null && !outcome.message().isBlank()) {
            detail += "; message=" + sanitize(outcome.message());
        }

        operationLogService.record(OperationLog.builder()
                .operatorId(UserContext.getUserId())
                .module(resolveModule(request.getRequestURI()))
                .action(resolveAction(request.getMethod(), request.getRequestURI()))
                .requestMethod(limit(request.getMethod(), 10))
                .requestUri(limit(request.getRequestURI(), URI_LIMIT))
                .result(outcome.success() ? "SUCCESS" : "FAILED")
                .ipAddress(limit(resolveClientIp(request), IP_LIMIT))
                .detail(limit(detail, DETAIL_LIMIT))
                .build());
    }

    private ResponseOutcome resolveOutcome(
            ContentCachingResponseWrapper response,
            String exceptionName) {
        if (exceptionName != null) {
            return new ResponseOutcome(false, exceptionName, exceptionName);
        }

        boolean httpSuccess = response.getStatus() < 400;
        String code = httpSuccess ? "SUCCESS" : "HTTP_" + response.getStatus();
        String message = null;
        byte[] body = response.getContentAsByteArray();
        String contentType = response.getContentType();
        if (body.length > 0 && contentType != null
                && contentType.toLowerCase(Locale.ROOT)
                .contains(MediaType.APPLICATION_JSON_VALUE)) {
            try {
                JsonNode root = objectMapper.readTree(
                        new String(body, StandardCharsets.UTF_8)
                );
                if (root.hasNonNull("code")) {
                    code = root.get("code").asText(code);
                }
                if (root.hasNonNull("message")) {
                    message = root.get("message").asText();
                }
            } catch (IOException exception) {
                log.debug("Unable to parse response code for audit", exception);
            }
        }
        return new ResponseOutcome(httpSuccess && "SUCCESS".equals(code), code, message);
    }

    static String resolveModule(String uri) {
        if (uri == null || uri.isBlank()) {
            return "UNKNOWN";
        }
        String[] segments = uri.split("/");
        int index = 2;
        if (segments.length > index
                && ("admin".equals(segments[index])
                || "public".equals(segments[index]))) {
            index++;
        }
        if (segments.length <= index || segments[index].isBlank()) {
            return "UNKNOWN";
        }
        return segments[index].replace('-', '_').toUpperCase(Locale.ROOT);
    }

    static String resolveAction(String method, String uri) {
        String path = uri == null ? "" : uri.toLowerCase(Locale.ROOT);
        if (path.endsWith("/login")) {
            return "LOGIN";
        }
        if (path.endsWith("/register")) {
            return "REGISTER";
        }
        if (path.contains("/unban")) {
            return "UNBAN";
        }
        if (path.contains("/cancel")) {
            return "CANCEL";
        }
        if (path.contains("/refund")) {
            return "REFUND";
        }
        if (path.contains("/check-out") || path.contains("/checkout")) {
            return "CHECK_OUT";
        }
        if (path.contains("/check-in") || path.contains("/checkin")) {
            return "CHECK_IN";
        }
        return switch (method.toUpperCase(Locale.ROOT)) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "ACCESS";
        };
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String sanitize(String value) {
        return value.replace('\r', ' ')
                .replace('\n', ' ')
                .replace(';', ',');
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record ResponseOutcome(
            boolean success,
            String code,
            String message) {
    }
}
