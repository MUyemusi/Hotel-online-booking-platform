
package com.zhisu.hotel.dashboard.service.impl;

import com.zhisu.hotel.dashboard.service.DashboardService;
import com.zhisu.hotel.dashboard.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        long todayOrderCount = 0, todayPaidOrderCount = 0, pendingCheckInCount = 0, todayCheckOutCount = 0, userCancellationCount = 0;
        BigDecimal todayRevenue = money(BigDecimal.ZERO);
        if (tableExists("booking_order")) {
            todayOrderCount = queryForLong("SELECT COUNT(*) FROM booking_order WHERE created_at >= ? AND created_at < ?", Timestamp.valueOf(start), Timestamp.valueOf(end));
            pendingCheckInCount = queryForLong("SELECT COUNT(*) FROM booking_order WHERE check_in_date = ? AND order_status = 'PENDING_CHECK_IN'", Date.valueOf(today));
            todayCheckOutCount = queryForLong("SELECT COUNT(*) FROM booking_order WHERE updated_at >= ? AND updated_at < ? AND order_status = 'COMPLETED'", Timestamp.valueOf(start), Timestamp.valueOf(end));
            userCancellationCount = queryForLong("SELECT COUNT(*) FROM booking_order WHERE cancelled_at >= ? AND cancelled_at < ? AND cancel_type = 'USER_CANCEL'", Timestamp.valueOf(start), Timestamp.valueOf(end));
        }
        if (tableExists("payment_record")) {
            todayPaidOrderCount = queryForLong("SELECT COUNT(DISTINCT order_id) FROM payment_record WHERE paid_at >= ? AND paid_at < ? AND payment_status = 'PAID'", Timestamp.valueOf(start), Timestamp.valueOf(end));
            todayRevenue = queryForDecimal("SELECT COALESCE(SUM(amount), 0) FROM payment_record WHERE paid_at >= ? AND paid_at < ? AND payment_status = 'PAID'", Timestamp.valueOf(start), Timestamp.valueOf(end));
        }

        long occupied = 0, dirty = 0, usableRooms = 0;
        if (tableExists("room")) {
            occupied = queryForLong("SELECT COUNT(*) FROM room WHERE is_deleted = 0 AND room_status = 'OCCUPIED'");
            dirty = queryForLong("SELECT COUNT(*) FROM room WHERE is_deleted = 0 AND room_status = 'DIRTY'");
            usableRooms = queryForLong("SELECT COUNT(*) FROM room WHERE is_deleted = 0 AND room_status NOT IN ('DISABLED','MAINTENANCE')");
        }
        BigDecimal occupancyRate = usableRooms == 0 ? money(BigDecimal.ZERO) : BigDecimal.valueOf(occupied * 100.0 / usableRooms).setScale(2, RoundingMode.HALF_UP);
        int availableRoomCount = tableExists("daily_inventory") ? queryForInt("SELECT COALESCE(SUM(available_rooms), 0) FROM daily_inventory WHERE inventory_date = ? AND status = 'ACTIVE'", Date.valueOf(today)) : 0;
        int warningThreshold = systemInteger("INVENTORY_WARNING_VALUE", 2);
        long inventoryWarningCount = tableExists("daily_inventory") ? queryForLong("SELECT COUNT(DISTINCT room_type_id) FROM daily_inventory WHERE inventory_date = ? AND status = 'ACTIVE' AND available_rooms <= ?", Date.valueOf(today), warningThreshold) : 0;

        return DashboardOverviewVO.builder()
                .todayOrderCount(todayOrderCount).todayPaidOrderCount(todayPaidOrderCount).todayRevenue(todayRevenue)
                .todayCheckInCount(pendingCheckInCount).todayCheckOutCount(todayCheckOutCount).todayUserCancellationCount(userCancellationCount)
                .availableRoomCount(availableRoomCount).occupiedRoomCount(occupied).dirtyRoomCount(dirty).occupancyRate(occupancyRate)
                .inventoryWarningCount(inventoryWarningCount)
                .publishedAnnouncementCount(tableExists("announcement") ? queryForLong("SELECT COUNT(*) FROM announcement WHERE status = 'PUBLISHED'") : 0)
                .onlineBannerCount(tableExists("hotel_banner") ? queryForLong("SELECT COUNT(*) FROM hotel_banner WHERE status = 'PUBLISHED'") : 0)
                .enabledSystemConfigCount(tableExists("system_config") ? queryForLong("SELECT COUNT(*) FROM system_config WHERE status = 'ENABLED'") : 0)
                .build();
    }

    @Override
    public List<DashboardTrendPointVO> getTrends(int days) {
        int normalizedDays = Math.max(1, Math.min(days, 30));
        LocalDate today = LocalDate.now(HOTEL_ZONE);
        List<DashboardTrendPointVO> points = new ArrayList<>();
        for (int i = normalizedDays - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay(), end = date.plusDays(1).atStartOfDay();
            long orders = tableExists("booking_order") ? queryForLong("SELECT COUNT(*) FROM booking_order WHERE created_at >= ? AND created_at < ?", Timestamp.valueOf(start), Timestamp.valueOf(end)) : 0;
            BigDecimal revenue = tableExists("payment_record") ? queryForDecimal("SELECT COALESCE(SUM(amount),0) FROM payment_record WHERE paid_at >= ? AND paid_at < ? AND payment_status='PAID'", Timestamp.valueOf(start), Timestamp.valueOf(end)) : money(BigDecimal.ZERO);
            points.add(DashboardTrendPointVO.builder().date(date).orderCount(orders).revenue(revenue).build());
        }
        return points;
    }

    @Override
    public DashboardAnalyticsVO getAnalytics(int days) {
        int normalizedDays = Math.max(1, Math.min(days, 365));
        LocalDate today = LocalDate.now(HOTEL_ZONE);
        LocalDateTime currentStart = today.minusDays(normalizedDays - 1L).atStartOfDay();
        LocalDateTime currentEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime previousStart = currentStart.minusDays(normalizedDays);
        BigDecimal currentRevenue = paidRevenue(currentStart, currentEnd);
        BigDecimal previousRevenue = paidRevenue(previousStart, currentStart);
        BigDecimal growth = previousRevenue.compareTo(BigDecimal.ZERO) == 0
                ? (currentRevenue.compareTo(BigDecimal.ZERO) == 0 ? money(BigDecimal.ZERO) : money(BigDecimal.valueOf(100)))
                : currentRevenue.subtract(previousRevenue)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previousRevenue, 2, RoundingMode.HALF_UP);

        return DashboardAnalyticsVO.builder()
                .periodDays(normalizedDays)
                .currentPeriodRevenue(currentRevenue)
                .previousPeriodRevenue(previousRevenue)
                .revenueGrowthRate(growth)
                .trends(getTrends(normalizedDays))
                .roomTypeStatistics(periodRoomTypeStatistics(currentStart, currentEnd))
                .paymentMethods(categoryStatistics(
                        "SELECT COALESCE(payment_method,'UNKNOWN') code, COUNT(*) item_count, COALESCE(SUM(amount),0) amount "
                                + "FROM payment_record WHERE payment_status IN ('PAID','SUCCESS') AND paid_at>=? AND paid_at<? GROUP BY payment_method ORDER BY amount DESC",
                        currentStart, currentEnd, true))
                .orderStatuses(categoryStatistics(
                        "SELECT COALESCE(order_status,'UNKNOWN') code, COUNT(*) item_count, 0 amount "
                                + "FROM booking_order WHERE created_at>=? AND created_at<? GROUP BY order_status ORDER BY item_count DESC",
                        currentStart, currentEnd, false))
                .build();
    }

    @Override
    public List<DashboardPaymentRecordVO> getRecentPayments(int days, int limit) {
        int normalizedDays = Math.max(1, Math.min(days, 365));
        int normalizedLimit = Math.max(1, Math.min(limit, 200));
        if (!tableExists("payment_record")) return List.of();
        LocalDate today = LocalDate.now(HOTEL_ZONE);
        LocalDateTime start = today.minusDays(normalizedDays - 1L).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        String sql = "SELECT pr.id payment_id, pr.payment_no, pr.order_id, bo.order_no, pr.payment_method, pr.amount, "
                + "COALESCE(pr.paid_at, pr.created_at) paid_at "
                + "FROM payment_record pr "
                + "LEFT JOIN booking_order bo ON bo.id = pr.order_id "
                + "WHERE pr.payment_status IN ('PAID','SUCCESS') "
                + "AND COALESCE(pr.paid_at, pr.created_at) >=? AND COALESCE(pr.paid_at, pr.created_at) <? "
                + "ORDER BY paid_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> DashboardPaymentRecordVO.builder()
                        .paymentId(rs.getLong("payment_id"))
                        .paymentNo(rs.getString("payment_no"))
                        .orderId(rs.getLong("order_id"))
                        .orderNo(rs.getString("order_no"))
                        .paymentMethod(rs.getString("payment_method"))
                        .amount(money(rs.getBigDecimal("amount")))
                        .paidAt(toLocalDateTime(rs.getTimestamp("paid_at")))
                        .build(),
                Timestamp.valueOf(start), Timestamp.valueOf(end), normalizedLimit);
    }

    private BigDecimal paidRevenue(LocalDateTime start, LocalDateTime end) {
        if (!tableExists("payment_record")) return money(BigDecimal.ZERO);
        return queryForDecimal("SELECT COALESCE(SUM(amount),0) FROM payment_record "
                + "WHERE payment_status IN ('PAID','SUCCESS') AND paid_at>=? AND paid_at<?", Timestamp.valueOf(start), Timestamp.valueOf(end));
    }

    private List<RoomTypeStatisticVO> periodRoomTypeStatistics(LocalDateTime start, LocalDateTime end) {
        if (!tableExists("payment_record") || !tableExists("booking_order")) return List.of();
        return jdbcTemplate.query("SELECT bo.room_type_id, COALESCE(rt.name,bo.room_type_name,CONCAT('房型',bo.room_type_id)) room_type_name, "
                        + "COUNT(DISTINCT pr.order_id) booking_count, COALESCE(SUM(pr.amount),0) revenue "
                        + "FROM payment_record pr JOIN booking_order bo ON bo.id=pr.order_id "
                        + "LEFT JOIN room_type rt ON rt.id=bo.room_type_id "
                        + "WHERE pr.payment_status IN ('PAID','SUCCESS') AND pr.paid_at>=? AND pr.paid_at<? "
                        + "GROUP BY bo.room_type_id,rt.name,bo.room_type_name ORDER BY revenue DESC,booking_count DESC",
                this::mapRoomTypeStatistic, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }

    private List<DashboardCategoryStatisticVO> categoryStatistics(
            String sql, LocalDateTime start, LocalDateTime end, boolean paymentCategory) {
        String requiredTable = paymentCategory ? "payment_record" : "booking_order";
        if (!tableExists(requiredTable)) return List.of();
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String code = rs.getString("code");
            return DashboardCategoryStatisticVO.builder()
                    .code(code)
                    .label(paymentCategory ? paymentMethodLabel(code) : orderStatusLabel(code))
                    .count(rs.getLong("item_count"))
                    .amount(money(rs.getBigDecimal("amount")))
                    .build();
        }, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }

    private String paymentMethodLabel(String code) {
        if (code == null) return "未知方式";
        return switch (code) {
            case "MOCK" -> "模拟支付";
            case "WECHAT" -> "微信支付";
            case "ALIPAY" -> "支付宝";
            case "CASH" -> "现金";
            case "CARD" -> "银行卡";
            default -> code;
        };
    }

    private String orderStatusLabel(String code) {
        if (code == null) return "未知状态";
        return switch (code) {
            case "PENDING_PAYMENT" -> "待支付";
            case "PENDING_CHECK_IN" -> "待入住";
            case "CHECKED_IN" -> "已入住";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            case "CLOSED" -> "已关闭";
            default -> code;
        };
    }

    @Override
    public List<RoomTypeStatisticVO> getRoomTypeStatistics() {
        if (tableExists("booking_order")) {
            return jdbcTemplate.query("SELECT bo.room_type_id, COALESCE(rt.name, bo.room_type_name, CONCAT('房型', bo.room_type_id)) room_type_name, COUNT(*) booking_count, COALESCE(SUM(bo.total_amount),0) revenue FROM booking_order bo LEFT JOIN room_type rt ON rt.id=bo.room_type_id WHERE bo.payment_status='PAID' AND bo.order_status NOT IN ('CANCELLED','CLOSED') GROUP BY bo.room_type_id, rt.name, bo.room_type_name ORDER BY revenue DESC, booking_count DESC", this::mapRoomTypeStatistic);
        }
        return List.of();
    }

    @Override
    public List<InventoryWarningVO> getInventoryWarnings() {
        if (!tableExists("daily_inventory")) return List.of();
        LocalDate today = LocalDate.now(HOTEL_ZONE);
        int threshold = systemInteger("INVENTORY_WARNING_VALUE", 2);
        return jdbcTemplate.query("SELECT di.room_type_id, rt.name room_type_name, di.inventory_date, di.available_rooms, di.total_rooms, di.status FROM daily_inventory di LEFT JOIN room_type rt ON rt.id=di.room_type_id WHERE di.inventory_date>=? AND di.inventory_date<? AND di.status='ACTIVE' AND di.available_rooms<=? ORDER BY di.inventory_date, di.available_rooms LIMIT 100",
                (rs, n) -> InventoryWarningVO.builder().roomTypeId(rs.getLong("room_type_id")).roomTypeName(rs.getString("room_type_name")).inventoryDate(rs.getDate("inventory_date").toLocalDate()).availableRooms(rs.getInt("available_rooms")).totalRooms(rs.getInt("total_rooms")).status(rs.getString("status")).build(),
                Date.valueOf(today), Date.valueOf(today.plusDays(7)), threshold);
    }

    private RoomTypeStatisticVO mapRoomTypeStatistic(ResultSet rs, int rowNum) throws SQLException {
        return RoomTypeStatisticVO.builder().roomTypeId(rs.getLong("room_type_id")).roomTypeName(rs.getString("room_type_name")).bookingCount(rs.getLong("booking_count")).revenue(money(rs.getBigDecimal("revenue"))).build();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
    protected int systemInteger(String key, int fallback) {
        if (!tableExists("system_config")) return fallback;
        Integer value = jdbcTemplate.query("SELECT CAST(config_value AS SIGNED) FROM system_config WHERE config_key=? AND status='ENABLED' LIMIT 1", rs -> rs.next() ? rs.getInt(1) : null, key);
        return value == null ? fallback : value;
    }
    protected long queryForLong(String sql, Object... args) { Long v=jdbcTemplate.query(sql, rs->rs.next()?rs.getLong(1):0L,args); return v==null?0:v; }
    protected int queryForInt(String sql, Object... args) { Integer v=jdbcTemplate.query(sql, rs->rs.next()?rs.getInt(1):0,args); return v==null?0:v; }
    protected BigDecimal queryForDecimal(String sql, Object... args) { BigDecimal v=jdbcTemplate.query(sql, rs->rs.next()?rs.getBigDecimal(1):BigDecimal.ZERO,args); return money(v); }
    protected BigDecimal money(BigDecimal value) { return (value==null?BigDecimal.ZERO:value).setScale(2,RoundingMode.HALF_UP); }
    protected boolean tableExists(String name) { Integer c=jdbcTemplate.query("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?",rs->rs.next()?rs.getInt(1):0,name); return c!=null&&c>0; }
}
