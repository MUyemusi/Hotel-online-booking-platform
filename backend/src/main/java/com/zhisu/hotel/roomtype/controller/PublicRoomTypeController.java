package com.zhisu.hotel.roomtype.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.roomtype.service.PublicRoomTypeService;
import com.zhisu.hotel.roomtype.vo.PublicRoomTypeVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/public/room-types")
@RequiredArgsConstructor
@Validated
public class PublicRoomTypeController {
    private final PublicRoomTypeService service;

    @GetMapping
    public ApiResponse<List<PublicRoomTypeVO>> search(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(defaultValue = "1") @Min(1) @Max(5) int roomCount,
            @RequestParam(defaultValue = "1") @Min(1) @Max(20) int guests) {
        return ApiResponse.success(service.search(checkInDate, checkOutDate, roomCount, guests));
    }
}