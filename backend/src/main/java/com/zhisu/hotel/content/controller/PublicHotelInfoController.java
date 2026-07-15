package com.zhisu.hotel.content.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.content.entity.HotelInfo;
import com.zhisu.hotel.content.service.HotelInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/hotel-info")
@RequiredArgsConstructor
public class PublicHotelInfoController {

    private final HotelInfoService hotelInfoService;

    @GetMapping
    public ApiResponse<HotelInfo> getHotelInfo() {
        return ApiResponse.success(hotelInfoService.getHotelInfo());
    }
}
