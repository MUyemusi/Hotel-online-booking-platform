package com.zhisu.hotel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Order(Integer.MIN_VALUE + 1) // 在 AutoSchemaMigration 之后运行
public class DatabaseSchemaValidator implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Map<String, List<String>> required = new LinkedHashMap<>();
        required.put("sys_user", List.of("id", "username", "password_hash", "status", "consecutive_cancel_count"));
        required.put("booking_order", List.of("id", "user_id", "order_status", "payment_status", "payment_timeout"));
        required.put("payment_record", List.of("id", "user_id", "payment_status", "idempotency_key"));
        required.put("cancellation_record", List.of("id", "order_id", "cancel_type", "cancel_time"));
        required.put("scheduled_task_log", List.of("id", "task_name", "executed_at"));
        required.put("hotel_info", List.of("hotel_name", "logo_url", "contact_phone", "faq_content"));
        required.put("price_rule", List.of("start_date", "end_date", "room_type_id", "priority"));
        required.put("daily_price", List.of("room_type_id", "business_date", "final_price", "manual_override"));
        required.put("auth_refresh_token", List.of("user_id", "token_hash", "expires_at", "revoked_at"));
        required.put("room_type_review", List.of("room_type_id", "nickname", "content", "status"));

        StringBuilder missing = new StringBuilder();
        required.forEach((table, columns) -> {
            if (!tableExists(table)) {
                missing.append(" table:").append(table);
                return;
            }
            columns.stream().filter(column -> !columnExists(table, column))
                    .forEach(column -> missing.append(" column:").append(table).append('.').append(column));
        });
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Database schema is incomplete." + missing
                    + ". Run database/complete_init_fixed.sql or database/migrations/full_completion.sql.");
        }
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?",
                Integer.class, table);
        return count != null && count > 0;
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=? AND column_name=?",
                Integer.class, table, column);
        return count != null && count > 0;
    }
}