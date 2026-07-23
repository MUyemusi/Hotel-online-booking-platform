
package com.zhisu.hotel.pricing.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.pricing.service.PriceQuoteService;
import com.zhisu.hotel.pricing.vo.PriceQuoteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/public/pricing")
@RequiredArgsConstructor
public class PublicPriceController {

    private final PriceQuoteService priceQuoteService;

    @GetMapping("/quote")
    public ApiResponse<PriceQuoteVO> quotePrice(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(defaultValue = "1") Integer roomCount,
            @RequestParam(defaultValue = "STANDARD") String stayType) {
        return ApiResponse.success(
                priceQuoteService.quotePrice(roomTypeId, checkInDate, checkOutDate, roomCount, stayType)
        );
    }
}
