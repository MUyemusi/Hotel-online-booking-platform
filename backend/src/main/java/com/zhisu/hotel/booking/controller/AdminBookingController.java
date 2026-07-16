package com.zhisu.hotel.booking.controller;

import com.zhisu.hotel.booking.dto.AdminOrderSearchRequest;
import com.zhisu.hotel.booking.service.BookingOrderService;
import com.zhisu.hotel.booking.vo.OrderDetailVO;
import com.zhisu.hotel.booking.vo.OrderVO;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.payment.vo.CancellationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingOrderService bookingOrderService;

    @GetMapping
    public ApiResponse<PageResponse<OrderVO>> adminOrderList(@ModelAttribute AdminOrderSearchRequest request) {
        PageResponse<OrderVO> result = bookingOrderService.adminOrderList(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailVO> adminOrderDetail(@PathVariable Long id) {
        OrderDetailVO vo = bookingOrderService.getOrderDetail(id, null);
        return ApiResponse.success(vo);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> adminUpdateStatus(@PathVariable Long id, @RequestParam String targetStatus) {
        bookingOrderService.adminUpdateStatus(id, targetStatus);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<CancellationVO> adminCancelOrder(@PathVariable Long id, @RequestParam String reason) {
        CancellationVO vo = bookingOrderService.adminCancelOrder(id, reason);
        return ApiResponse.success(vo);
    }
}
