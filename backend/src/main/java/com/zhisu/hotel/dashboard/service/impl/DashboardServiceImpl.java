package com.zhisu.hotel.dashboard.service.impl;

import com.zhisu.hotel.dashboard.service.DashboardService;
import com.zhisu.hotel.dashboard.vo.DashboardOverviewVO;
import com.zhisu.hotel.dashboard.vo.DashboardTrendPointVO;
import com.zhisu.hotel.dashboard.vo.InventoryWarningVO;
import com.zhisu.hotel.dashboard.vo.RoomTypeStatisticVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Shanghai");

    private final JdbcTemplate jdbcTemplate;

    @Override
    public DashboardOverviewVO getOverview() {
        LocalDate today = LocalDate.now(HOTEL_ZONE);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long todayOrderCount = 0L;
        BigDecimal todayRevenue = BigDecimal.ZERO.setScale(2);
        long todayCheckInCount = 0L;
        long todayCheckOutCount = 0L;

        if (tableExists("booking_order")) {
            todayOrderCount = queryForLong(
                    "SELECT COUNT(*) FROM booking_order WHERE created_at >= ? AND created_at < ?",
                    Timestamp.valueOf(start), Timestamp.valueOf(end)
            );
            todayRevenue = queryForDecimal(
                    "SELECT COALESCE(SUM(total_amount), 0) FROM booking_order WHERE created_at >= ? AND created_at < ?",
                    Timestamp.valueOf(start), Timestamp.valueOf(end)
            );
            if (columnExists("booking_order", "check_in_date")) {
                todayCheckInCount = queryForLong(
                        "SELECT COUNT(*) FROM booking_order WHERE check_in_date = ?",
                        Date.valueOf(today)
                );
            }
            if (columnExists("booking_order", "check_out_date")) {
                todayCheckOutCount = queryForLong(
                        "SELECT COUNT(*) FROM booking_order WHERE check_out_date = ?",
                        Date.valueOf(today)
                );
            }
        }

        int availableRoomCount = queryForInt(
                "SELECT COALESCE(SUM(available_rooms), 0) FROM daily_inventory WHERE inventory_date = ? AND status = 'ACTIVE'",
                Date.valueOf(today)
        );

        int warningThreshold = 2;
        if (tableExists("system_config")) {
            Integer value = jdbcTemplate.query(
                    "SELECT CAST(config_value AS SIGNED) FROM system_config WHERE config_key = 'INVENTORY_WARNING_VALUE' LIMIT 1",
                    rs -> rs.next() ? rs.getInt(1) : null
            );
            if (value != null) {
                warningThreshold = value;
            }
        }

        long inventoryWarningCount = queryForLong(
                "SELECT COUNT(DISTINCT room_type_id) FROM daily_inventory WHERE inventory_date = ? AND status = 'ACTIVE' AND available_rooms <= ?",
                Date.valueOf(today), warningThreshold
        );

        long publishedAnnouncementCount = queryForLong(
                "SELECT COUNT(*) FROM announcement WHERE status = 'PUBLISHED'"
        );
        long onlineBannerCount = queryForLong(
                "SELECT COUNT(*) FROM hotel_banner WHERE status = 'PUBLISHED'"
        );
        long enabledSystemConfigCount = queryForLong(
                "SELECT COUNT(*) FROM system_config WHERE status = 'ENABLED'"
        );

        return DashboardOverviewVO.builder()
                .todayOrderCount(todayOrderCount)
                .todayRevenue(todayRevenue)
                .todayCheckInCount(todayCheckInCount)
                .todayCheckOutCount(todayCheckOutCount)
                .availableRoomCount(availableRoomCount)
                .inventoryWarningCount(inventoryWarningCount)
                .publishedAnnouncementCount(publishedAnnouncementCount)
                .onlineBannerCount(onlineBannerCount)
                .enabledSystemConfigCount(enabledSystemConfigCount)
                .build();
    }

    @Override
    public List<DashboardTrendPointVO> getTrends(int days) {
        int normalizedDays = Math.max(1, Math.min(days, 30));
        LocalDate today = LocalDate.now(HOTEL_ZONE);
        List<DashboardTrendPointVO> points = new ArrayList<>();
        for (int i = normalizedDays - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long orderCount = 0L;
            BigDecimal revenue = BigDecimal.ZERO.setScale(2);
            if (tableExists("booking_order")) {
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.plusDays(1).atStartOfDay();
                orderCount = queryForLong(
                        "SELECT COUNT(*) FROM booking_order WHERE created_at >= ? AND created_at < ?",
                        Timestamp.valueOf(start), Timestamp.valueOf(end)
                );
                revenue = queryForDecimal(
                        "SELECT COALESCE(SUM(total_amount), 0) FROM booking_order WHERE created_at >= ? AND created_at < ?",
                        Timestamp.valueOf(start), Timestamp.valueOf(end)
                );
            }
            points.add(DashboardTrendPointVO.builder()
                    .date(date)
                    .orderCount(orderCount)
                    .revenue(revenue)
                    .build());
        }
        return points;
    }

    @Override
    public List<RoomTypeStatisticVO> getRoomTypeStatistics() {
        if (tableExists("order_daily_price")) {
            return jdbcTemplate.query(
                    "SELECT odp.room_type_id, COALESCE(rt.name, CONCAT('房型', odp.room_type_id)) AS room_type_name, COUNT(*) AS booking_count, COALESCE(SUM(odp.daily_amount), 0) AS revenue " +
                            "FROM order_daily_price odp " +
                            "LEFT JOIN room_type rt ON rt.id = odp.room_type_id " +
                            "GROUP BY odp.room_type_id, rt.name " +
                            "ORDER BY revenue DESC, booking_count DESC",
                    (rs, rowNum) -> mapRoomTypeStatistic(rs)
            );
        }

        return jdbcTemplate.query(
                "SELECT id, name FROM room_type WHERE is_deleted = 0 ORDER BY id",
                (rs, rowNum) -> RoomTypeStatisticVO.builder()
                        .roomTypeId(rs.getLong("id"))
                        .roomTypeName(rs.getString("name"))
                        .bookingCount(0L)
                        .revenue(BigDecimal.ZERO.setScale(2))
                        .build()
        );
    }

    @Override
    public List<InventoryWarningVO> getInventoryWarnings() {
        LocalDate today = LocalDate.now(HOTEL_ZONE);
        int warningThreshold = 2;
        if (tableExists("system_config")) {
            Integer value = jdbcTemplate.query(
                    "SELECT CAST(config_value AS SIGNED) FROM system_config WHERE config_key = 'INVENTORY_WARNING_VALUE' LIMIT 1",
                    rs -> rs.next() ? rs.getInt(1) : null
            );
            if (value != null) {
                warningThreshold = value;
            }
        }

        return jdbcTemplate.query(
                "SELECT di.room_type_id, rt.name AS room_type_name, di.inventory_date, di.available_rooms, di.total_rooms, di.status " +
                        "FROM daily_inventory di " +
                        "LEFT JOIN room_type rt ON rt.id = di.room_type_id " +
                        "WHERE di.inventory_date >= ? AND di.status = 'ACTIVE' AND di.available_rooms <= ? " +
                        "ORDER BY di.inventory_date ASC, di.available_rooms ASC, di.room_type_id ASC LIMIT 20",
                (rs, rowNum) -> InventoryWarningVO.builder()
                        .roomTypeId(rs.getLong("room_type_id"))
                        .roomTypeName(rs.getString("room_type_name"))
                        .inventoryDate(rs.getDate("inventory_date").toLocalDate())
                        .availableRooms(rs.getInt("available_rooms"))
                        .totalRooms(rs.getInt("total_rooms"))
                        .status(rs.getString("status"))
                        .build(),
                Date.valueOf(today), warningThreshold
        );
    }

    private RoomTypeStatisticVO mapRoomTypeStatistic(ResultSet rs) throws SQLException {
        return RoomTypeStatisticVO.builder()
                .roomTypeId(rs.getLong("room_type_id"))
                .roomTypeName(rs.getString("room_type_name"))
                .bookingCount(rs.getLong("booking_count"))
                .revenue(scale(rs.getBigDecimal("revenue")))
                .build();
    }

    private long queryForLong(String sql, Object... args) {
        Long value = jdbcTemplate.query(sql, rs -> rs.next() ? rs.getLong(1) : 0L, args);
        return value == null ? 0L : value;
    }

    private int queryForInt(String sql, Object... args) {
        Integer value = jdbcTemplate.query(sql, rs -> rs.next() ? rs.getInt(1) : 0, args);
        return value == null ? 0 : value;
    }

    private BigDecimal queryForDecimal(String sql, Object... args) {
        BigDecimal value = jdbcTemplate.query(sql, rs -> rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO, args);
        return scale(value);
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.query(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                rs -> rs.next() ? rs.getInt(1) : 0,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.query(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                rs -> rs.next() ? rs.getInt(1) : 0,
                tableName, columnName
        );
        return count != null && count > 0;
    }
}