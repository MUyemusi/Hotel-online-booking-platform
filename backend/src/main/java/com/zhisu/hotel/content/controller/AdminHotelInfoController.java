package com.zhisu.hotel.content.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.content.dto.HotelInfoUpdateRequest;
import com.zhisu.hotel.content.entity.HotelInfo;
import com.zhisu.hotel.content.service.HotelInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/hotel-info")
@RequiredArgsConstructor
public class AdminHotelInfoController {

    private final HotelInfoService hotelInfoService;

    @GetMapping
    public ApiResponse<HotelInfo> getHotelInfo() {
        return ApiResponse.success(hotelInfoService.getHotelInfo());
    }

    @PutMapping
    public ApiResponse<HotelInfo> updateHotelInfo(
            @Valid @RequestBody HotelInfoUpdateRequest request) {

        return ApiResponse.success(
                hotelInfoService.updateHotelInfo(request)
        );
    }
}
