package com.zhisu.hotel.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


/**
 * 启动时自动补齐缺失的列，避免成员拉代码后需要手动跑 SQL。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Integer.MIN_VALUE) // 在 DatabaseSchemaValidator(HIGHEST_PRECEDENCE) 之前运行
public class AutoSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureColumn("hotel_info", "latitude", "DOUBLE DEFAULT NULL COMMENT '纬度'");
        ensureColumn("hotel_info", "longitude", "DOUBLE DEFAULT NULL COMMENT '经度'");

        // 如果有坐标但地址还是旧的，自动更新
        updateDefaultAddress();
    }

    private void ensureColumn(String table, String column, String definition) {
        if (!columnExists(table, column)) {
            String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", table, column, definition);
            jdbcTemplate.execute(sql);
            log.info("Auto-migration: added column {}.{}", table, column);
        }
    }

    private void updateDefaultAddress() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM hotel_info WHERE id = 1 AND latitude IS NOT NULL AND (address IS NULL OR address = '' OR address LIKE '%示范路%')",
                    Integer.class);
            if (count != null && count > 0) {
                jdbcTemplate.update(
                        "UPDATE hotel_info SET address = '海南省三亚市亚龙湾国家旅游度假区', latitude = COALESCE(latitude, 18.2296), longitude = COALESCE(longitude, 109.6253) WHERE id = 1");
                log.info("Auto-migration: updated hotel_info default address and coordinates");
            }
        } catch (Exception e) {
            // hotel_info 表可能还不存在，忽略
        }
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class, table, column);
        return count != null && count > 0;
    }
}
