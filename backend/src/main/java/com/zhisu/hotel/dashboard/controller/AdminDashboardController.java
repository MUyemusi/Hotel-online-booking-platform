

package com.zhisu.hotel.dashboard.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.dashboard.service.DashboardService;
import com.zhisu.hotel.dashboard.vo.DashboardOverviewVO;
import com.zhisu.hotel.dashboard.vo.DashboardAnalyticsVO;
import com.zhisu.hotel.dashboard.vo.DashboardTrendPointVO;
import com.zhisu.hotel.dashboard.vo.DashboardPaymentRecordVO;
import com.zhisu.hotel.dashboard.vo.InventoryWarningVO;
import com.zhisu.hotel.dashboard.vo.RoomTypeStatisticVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewVO> getOverview() {
        return ApiResponse.success(dashboardService.getOverview());
    }

    @GetMapping("/trends")
    public ApiResponse<List<DashboardTrendPointVO>> getTrends(@RequestParam(defaultValue = "7") int days) {
        return ApiResponse.success(dashboardService.getTrends(days));
    }

    @GetMapping("/analytics")
    public ApiResponse<DashboardAnalyticsVO> getAnalytics(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.success(dashboardService.getAnalytics(days));
    }

    @GetMapping("/payment-records")
    public ApiResponse<List<DashboardPaymentRecordVO>> getPaymentRecords(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(dashboardService.getRecentPayments(days, limit));
    }

    @GetMapping("/room-type-statistics")
    public ApiResponse<List<RoomTypeStatisticVO>> getRoomTypeStatistics() {
        return ApiResponse.success(dashboardService.getRoomTypeStatistics());
    }

    @GetMapping("/inventory-warnings")
    public ApiResponse<List<InventoryWarningVO>> getInventoryWarnings() {
        return ApiResponse.success(dashboardService.getInventoryWarnings());
    }
}
