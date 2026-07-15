package com.zhisu.hotel.content.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.content.entity.HotelBanner;
import com.zhisu.hotel.content.service.HotelBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/banners")
@RequiredArgsConstructor
public class PublicHotelBannerController {

    private final HotelBannerService hotelBannerService;

    @GetMapping
    public ApiResponse<List<HotelBanner>> getBanners() {
        return ApiResponse.success(hotelBannerService.getPublicBanners());
    }
}
