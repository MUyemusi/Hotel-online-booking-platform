package com.zhisu.hotel.roomtype.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @GetMapping
    public ApiResponse<Page<RoomTypeSummary>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ApiResponse.success(
                roomTypeService.pageRoomTypes(page, size, keyword, null, "ONLINE", guests, checkIn, checkOut));
    }

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
