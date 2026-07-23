package com.zhisu.hotel.checkin.controller;

import com.zhisu.hotel.checkin.dto.CheckinQrVerifyRequest;
import com.zhisu.hotel.checkin.service.CheckinQrService;
import com.zhisu.hotel.checkin.vo.CheckinQrVerificationVO;
import com.zhisu.hotel.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/frontdesk/checkin/qr")
@RequiredArgsConstructor
public class FrontdeskCheckinQrController {

    private final CheckinQrService checkinQrService;

    @PostMapping("/verify")
    public ApiResponse<CheckinQrVerificationVO> verify(@Valid @RequestBody CheckinQrVerifyRequest request) {
        return ApiResponse.success(checkinQrService.verify(request.getCredential()));
    }
}
