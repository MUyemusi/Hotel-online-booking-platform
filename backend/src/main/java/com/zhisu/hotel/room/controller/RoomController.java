package com.zhisu.hotel.room.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.room.dto.RoomCreateRequest;
import com.zhisu.hotel.room.dto.RoomQueryRequest;
import com.zhisu.hotel.room.dto.RoomStatusChangeRequest;
import com.zhisu.hotel.room.dto.RoomUpdateRequest;
import com.zhisu.hotel.room.entity.Room;
import com.zhisu.hotel.room.service.RoomService;
import com.zhisu.hotel.room.vo.FloorRoomStatisticsVO;
import com.zhisu.hotel.room.vo.RoomDetailVO;
import com.zhisu.hotel.room.vo.RoomStatusLogVO;
import com.zhisu.hotel.room.vo.RoomVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * 创建房间
     */
    @PostMapping
    public ApiResponse<Room> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        return ApiResponse.success(roomService.createRoom(request));
    }

    /**
     * 更新房间信息
     */
    @PutMapping("/{id}")
    public ApiResponse<Room> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomUpdateRequest request) {
        return ApiResponse.success(roomService.updateRoom(id, request));
    }

    /**
     * 获取房间详情
     */
    @GetMapping("/{id}")
    public ApiResponse<RoomDetailVO> getRoomDetail(@PathVariable Long id) {
        return ApiResponse.success(roomService.getRoomDetail(id));
    }

    /**
     * 分页查询房间列表
     */
    @GetMapping
    public ApiResponse<Page<RoomVO>> pageRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) RoomStatus roomStatus,
            @RequestParam(required = false) String roomNo) {

        RoomQueryRequest request = RoomQueryRequest.builder()
                .floor(floor)
                .roomTypeId(roomTypeId)
                .roomStatus(roomStatus)
                .roomNo(roomNo)
                .build();

        return ApiResponse.success(roomService.pageRooms(page, size, request));
    }

    /**
     * 变更房间状态
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> changeRoomStatus(@PathVariable Long id, @Valid @RequestBody RoomStatusChangeRequest request) {
        roomService.changeRoomStatus(id, request);
        return ApiResponse.success();
    }

    /**
     * 获取指定楼层的房间列表
     */
    @GetMapping("/floors/{floor}")
    public ApiResponse<List<RoomVO>> getRoomsByFloor(@PathVariable Integer floor) {
        return ApiResponse.success(roomService.getRoomsByFloor(floor));
    }

    /**
     * 获取楼层统计信息
     */
    @GetMapping("/floor-statistics")
    public ApiResponse<List<FloorRoomStatisticsVO>> getFloorStatistics() {
        return ApiResponse.success(roomService.getFloorStatistics());
    }

    /**
     * 获取房间状态变更日志
     */
    @GetMapping("/{id}/status-logs")
    public ApiResponse<List<RoomStatusLogVO>> getRoomStatusLogs(@PathVariable Long id) {
        return ApiResponse.success(roomService.getRoomStatusLogs(id));
    }
}
