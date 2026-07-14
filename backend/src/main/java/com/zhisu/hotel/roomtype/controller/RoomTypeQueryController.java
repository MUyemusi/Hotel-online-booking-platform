package com.zhisu.hotel.roomtype.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.service.RoomTypeQueryService;
import com.zhisu.hotel.roomtype.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeQueryController {

    private final RoomTypeService roomTypeService;
    private final RoomTypeQueryService roomTypeQueryService;

    @GetMapping("/{id}")
    public ApiResponse<RoomType> getDetail(@PathVariable Long id) {
        return ApiResponse.success(roomTypeService.getRoomTypeDetail(id));
    }

    @GetMapping("/{id}/images")
    public ApiResponse<?> getImages(@PathVariable Long id) {
        return ApiResponse.success(roomTypeService.getImagesByRoomType(id));
    }

    @GetMapping("/{id}/facilities")
    public ApiResponse<?> getFacilities(@PathVariable Long id) {
        return ApiResponse.success(roomTypeService.getFacilitiesByRoomType(id));
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<RoomTypeSummary> getSummary(@PathVariable Long id) {
        return ApiResponse.success(roomTypeQueryService.getRoomTypeById(id));
    }

    @GetMapping("/{id}/available")
    public ApiResponse<Boolean> isAvailable(@PathVariable Long id) {
        return ApiResponse.success(roomTypeQueryService.isRoomTypeAvailable(id));
    }
}
