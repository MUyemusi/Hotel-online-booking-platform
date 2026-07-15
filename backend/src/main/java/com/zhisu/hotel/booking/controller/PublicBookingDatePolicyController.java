package com.zhisu.hotel.booking.controller;

import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/booking-date-policy")
@RequiredArgsConstructor
public class PublicBookingDatePolicyController {

    private final BookingDatePolicyService bookingDatePolicyService;
    private final SystemConfigService systemConfigService;

    @GetMapping
    public ApiResponse<Map<String, Object>> getBookingDatePolicy() {
        LocalDate hotelToday = bookingDatePolicyService.getHotelToday();
        LocalDate latestCheckInDate = bookingDatePolicyService.getLatestCheckInDate();
        int maxStayNights = systemConfigService.getInteger("MAX_STAY_NIGHTS");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("hotelToday", hotelToday);
        payload.put("earliestCheckInDate", hotelToday);
        payload.put("latestCheckInDate", latestCheckInDate);
        payload.put("maxStayNights", maxStayNights);
        return ApiResponse.success(payload);
    }
}