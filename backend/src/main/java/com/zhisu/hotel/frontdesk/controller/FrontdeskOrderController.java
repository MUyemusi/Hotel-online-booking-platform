package com.zhisu.hotel.frontdesk.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.frontdesk.dto.FrontdeskOrderQueryRequest;
import com.zhisu.hotel.frontdesk.service.FrontdeskOrderQueryService;
import com.zhisu.hotel.frontdesk.vo.CheckinVerificationVO;
import com.zhisu.hotel.frontdesk.vo.FrontdeskOrderDetailVO;
import com.zhisu.hotel.frontdesk.vo.FrontdeskOrderListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/frontdesk/orders")
@RequiredArgsConstructor
public class FrontdeskOrderController {

    private final FrontdeskOrderQueryService frontdeskOrderQueryService;

    /**
     * Front desk permissions are pending member 1 authentication integration.
     */
    @GetMapping
    public ApiResponse<PageResponse<FrontdeskOrderListVO>> searchOrders(
            @ModelAttribute FrontdeskOrderQueryRequest request) {
        return ApiResponse.success(frontdeskOrderQueryService.searchOrders(request));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<FrontdeskOrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        return ApiResponse.success(frontdeskOrderQueryService.getOrderDetail(orderId));
    }

    /**
     * Candidate rooms are a query-time snapshot. The final assignment step must
     * re-check room status in a transaction and use conditional updates.
     */
    @GetMapping("/{orderId}/check-in-verification")
    public ApiResponse<CheckinVerificationVO> verifyCheckin(@PathVariable Long orderId) {
        return ApiResponse.success(frontdeskOrderQueryService.verifyCheckin(orderId));
    }

    @GetMapping("/verify")
    public ApiResponse<CheckinVerificationVO> verifyCheckinByOrderNo(@RequestParam String orderNo) {
        return ApiResponse.success(frontdeskOrderQueryService.verifyCheckinByOrderNo(orderNo));
    }
}
