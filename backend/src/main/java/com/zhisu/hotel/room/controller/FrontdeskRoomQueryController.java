package com.zhisu.hotel.room.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.room.service.RoomService;
import com.zhisu.hotel.room.vo.FloorRoomStatisticsVO;
import com.zhisu.hotel.room.vo.RoomDetailVO;
import com.zhisu.hotel.room.vo.RoomStatusLogVO;
import com.zhisu.hotel.room.vo.RoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/frontdesk/rooms")
@RequiredArgsConstructor
public class FrontdeskRoomQueryController {

    private final RoomService roomService;

    @GetMapping("/floors/{floor}")
    public ApiResponse<List<RoomVO>> getRoomsByFloor(@PathVariable Integer floor) {
        return ApiResponse.success(roomService.getRoomsByFloor(floor));
    }

    @GetMapping("/floor-statistics")
    public ApiResponse<List<FloorRoomStatisticsVO>> getFloorStatistics() {
        return ApiResponse.success(roomService.getFloorStatistics());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomDetailVO> getRoomDetail(@PathVariable Long id) {
        return ApiResponse.success(roomService.getRoomDetail(id));
    }

    @GetMapping("/{id}/status-logs")
    public ApiResponse<List<RoomStatusLogVO>> getRoomStatusLogs(@PathVariable Long id) {
        return ApiResponse.success(roomService.getRoomStatusLogs(id));
    }
}
