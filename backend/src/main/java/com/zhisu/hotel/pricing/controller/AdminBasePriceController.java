package com.zhisu.hotel.pricing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.pricing.dto.BasePriceUpdateRequest;
import com.zhisu.hotel.pricing.entity.PriceOperationLog;
import com.zhisu.hotel.pricing.service.BasePriceService;
import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;
import com.zhisu.hotel.roomtype.entity.RoomType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pricing")
@RequiredArgsConstructor
public class AdminBasePriceController {

    private final BasePriceService basePriceService;

    @GetMapping("/base-prices")
    public ApiResponse<Page<RoomTypeSummary>> pageBasePrices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(basePriceService.pageBasePrices(page, size, keyword, status));
    }

    @PutMapping("/base-prices/{roomTypeId}")
    public ApiResponse<RoomType> updateBasePrice(
            @PathVariable Long roomTypeId,
            @Valid @RequestBody BasePriceUpdateRequest request) {
        return ApiResponse.success(basePriceService.updateBasePrice(roomTypeId, request));
    }

    @GetMapping("/base-price-logs")
    public ApiResponse<Page<PriceOperationLog>> pagePriceLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long roomTypeId) {
        return ApiResponse.success(basePriceService.pagePriceLogs(page, size, roomTypeId));
    }
}
