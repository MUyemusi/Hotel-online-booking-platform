package com.zhisu.hotel.checkin.controller;

import com.zhisu.hotel.checkin.dto.ConfirmCheckinRequest;
import com.zhisu.hotel.checkin.dto.ConfirmCheckoutRequest;
import com.zhisu.hotel.checkin.service.CheckinConfirmationService;
import com.zhisu.hotel.checkin.service.CheckoutService;
import com.zhisu.hotel.checkin.vo.ConfirmCheckinVO;
import com.zhisu.hotel.checkin.vo.ConfirmCheckoutVO;
import com.zhisu.hotel.checkin.vo.RoomAssignmentVO;
import com.zhisu.hotel.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/frontdesk/orders")
@RequiredArgsConstructor
public class FrontdeskCheckinConfirmationController {

    private final CheckinConfirmationService checkinConfirmationService;
    private final CheckoutService checkoutService;

    /**
     * Front desk/admin authorization is pending member 1 integration.
     */
    @PostMapping("/{orderId}/check-in")
    public ApiResponse<ConfirmCheckinVO> confirmCheckin(@PathVariable Long orderId,
                                                        @Valid @RequestBody ConfirmCheckinRequest request) {
        return ApiResponse.success(checkinConfirmationService.confirm(orderId, request));
    }

    @PostMapping("/{orderId}/check-out")
    public ApiResponse<ConfirmCheckoutVO> confirmCheckout(@PathVariable Long orderId,
                                                          @Valid @RequestBody ConfirmCheckoutRequest request) {
        return ApiResponse.success(checkoutService.confirmCheckout(orderId, request));
    }

    @GetMapping("/{orderId}/assignments")
    public ApiResponse<List<RoomAssignmentVO>> listAssignments(@PathVariable Long orderId) {
        return ApiResponse.success(checkinConfirmationService.listAssignments(orderId));
    }
}
