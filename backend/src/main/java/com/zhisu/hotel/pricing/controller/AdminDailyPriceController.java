package com.zhisu.hotel.pricing.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.pricing.dto.DailyPriceGenerateRequest;
import com.zhisu.hotel.pricing.dto.DailyPriceManualPriceRequest;
import com.zhisu.hotel.pricing.entity.DailyPrice;
import com.zhisu.hotel.pricing.service.DailyPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing/daily-prices")
@RequiredArgsConstructor
public class AdminDailyPriceController {

    private final DailyPriceService dailyPriceService;

    @PostMapping("/generate")
    public ApiResponse<Integer> generatePrices(@Valid @RequestBody DailyPriceGenerateRequest request) {
        return ApiResponse.success(dailyPriceService.generateAllRoomTypePrices(request.getStartDate(), request.getEndDate()));
    }

    @PostMapping("/generate/{roomTypeId}")
    public ApiResponse<Integer> generateRoomTypePrices(@PathVariable Long roomTypeId, @Valid @RequestBody DailyPriceGenerateRequest request) {
        return ApiResponse.success(dailyPriceService.generateRoomTypePrices(roomTypeId, request.getStartDate(), request.getEndDate()));
    }

    @PostMapping("/regenerate-rolling-window")
    public ApiResponse<Integer> regenerateRollingWindow() {
        return ApiResponse.success(dailyPriceService.ensureRollingWindowPrices());
    }

    @GetMapping
    public ApiResponse<List<DailyPrice>> listPrices(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(dailyPriceService.listDailyPrices(roomTypeId, startDate, endDate));
    }

    @PutMapping("/{id}/manual-price")
    public ApiResponse<DailyPrice> setManualPrice(@PathVariable Long id, @Valid @RequestBody DailyPriceManualPriceRequest request) {
        return ApiResponse.success(dailyPriceService.setManualOverrideById(id, request.getManualPrice(), request.getOverrideReason()));
    }

    @DeleteMapping("/{id}/manual-price")
    public ApiResponse<DailyPrice> clearManualPrice(@PathVariable Long id) {
        return ApiResponse.success(dailyPriceService.clearManualOverrideById(id));
    }
}