

package com.zhisu.hotel.dashboard.service;

import com.zhisu.hotel.dashboard.vo.DashboardOverviewVO;
import com.zhisu.hotel.dashboard.vo.DashboardAnalyticsVO;
import com.zhisu.hotel.dashboard.vo.DashboardTrendPointVO;
import com.zhisu.hotel.dashboard.vo.InventoryWarningVO;
import com.zhisu.hotel.dashboard.vo.RoomTypeStatisticVO;

import java.util.List;

public interface DashboardService {

    DashboardOverviewVO getOverview();

    List<DashboardTrendPointVO> getTrends(int days);

    DashboardAnalyticsVO getAnalytics(int days);

    List<RoomTypeStatisticVO> getRoomTypeStatistics();

    List<InventoryWarningVO> getInventoryWarnings();
}
