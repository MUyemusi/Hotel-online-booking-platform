package com.zhisu.hotel.content.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.content.dto.HotelBannerSaveRequest;
import com.zhisu.hotel.content.entity.HotelBanner;
import com.zhisu.hotel.content.service.HotelBannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminHotelBannerController {

    private final HotelBannerService hotelBannerService;

    @GetMapping
    public ApiResponse<List<HotelBanner>> getBanners() {
        return ApiResponse.success(hotelBannerService.getAdminBanners());
    }

    @PostMapping
    public ApiResponse<HotelBanner> createBanner(@Valid @RequestBody HotelBannerSaveRequest request) {
        return ApiResponse.success(hotelBannerService.createBanner(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HotelBanner> updateBanner(
            @PathVariable Long id,
            @Valid @RequestBody HotelBannerSaveRequest request) {
        return ApiResponse.success(hotelBannerService.updateBanner(id, request));
    }

    @PatchMapping("/{id}/publish")
    public ApiResponse<HotelBanner> publishBanner(@PathVariable Long id) {
        return ApiResponse.success(hotelBannerService.publishBanner(id));
    }

    @PatchMapping("/{id}/offline")
    public ApiResponse<HotelBanner> offlineBanner(@PathVariable Long id) {
        return ApiResponse.success(hotelBannerService.offlineBanner(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
        hotelBannerService.deleteBanner(id);
        return ApiResponse.success();
    }
}
