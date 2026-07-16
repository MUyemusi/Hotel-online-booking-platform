package com.zhisu.hotel.checkin.controller;

import com.zhisu.hotel.checkin.service.CheckinQrService;
import com.zhisu.hotel.checkin.vo.CheckinQrGenerationVO;
import com.zhisu.hotel.checkin.vo.CheckinQrStatusVO;
import com.zhisu.hotel.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CheckinQrController {

    private final CheckinQrService checkinQrService;

    @PostMapping("/{orderId}/checkin-qr")
    public ApiResponse<CheckinQrGenerationVO> generate(@PathVariable Long orderId) {
        return ApiResponse.success(checkinQrService.generateForOrder(orderId, null));
    }

    @GetMapping("/{orderId}/checkin-qr/status")
    public ApiResponse<CheckinQrStatusVO> status(@PathVariable Long orderId) {
        return ApiResponse.success(checkinQrService.getActiveStatus(orderId, null));
    }
}
