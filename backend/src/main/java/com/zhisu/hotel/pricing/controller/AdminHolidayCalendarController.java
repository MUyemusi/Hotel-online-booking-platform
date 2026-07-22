package com.zhisu.hotel.pricing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.pricing.dto.HolidayCalendarSaveRequest;
import com.zhisu.hotel.pricing.entity.HolidayCalendar;
import com.zhisu.hotel.pricing.service.HolidayCalendarService;
import com.zhisu.hotel.pricing.vo.DatePriceRule;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/holiday-calendar")
@RequiredArgsConstructor
public class AdminHolidayCalendarController {

    private final HolidayCalendarService holidayCalendarService;

    @GetMapping
    public ApiResponse<Page<HolidayCalendar>> pageHolidayCalendar(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer holidayYear,
            @RequestParam(required = false) String dayType,
            @RequestParam(required = false) String sourceStatus,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(holidayCalendarService.pageHolidayCalendar(page, size, holidayYear, dayType, sourceStatus, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<HolidayCalendar> getHolidayCalendar(@PathVariable Long id) {
        return ApiResponse.success(holidayCalendarService.getHolidayCalendar(id));
    }

    @PostMapping
    public ApiResponse<HolidayCalendar> createHolidayCalendar(@Valid @RequestBody HolidayCalendarSaveRequest request) {
        return ApiResponse.success(holidayCalendarService.createHolidayCalendar(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HolidayCalendar> updateHolidayCalendar(@PathVariable Long id, @Valid @RequestBody HolidayCalendarSaveRequest request) {
        return ApiResponse.success(holidayCalendarService.updateHolidayCalendar(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<HolidayCalendar> updateHolidayCalendarStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return ApiResponse.success(holidayCalendarService.updateHolidayCalendarStatus(id, request.get("status")));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteHolidayCalendar(@PathVariable Long id) {
        holidayCalendarService.deleteHolidayCalendar(id);
        return ApiResponse.success();
    }

    @GetMapping("/preview")
    public ApiResponse<Map<String, Object>> previewDateRule(@RequestParam LocalDate date, @RequestParam(required = false) Long roomTypeId) {
        DatePriceRule rule = holidayCalendarService.resolveDateRule(date, roomTypeId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("date", date);
        payload.put("coefficient", rule.coefficient());
        payload.put("priceSource", rule.priceSource());
        payload.put("description", rule.description());
        payload.put("sourceStatus", rule.sourceStatus());
        payload.put("holidayCalendarId", rule.holidayCalendarId());
        return ApiResponse.success(payload);
    }
}