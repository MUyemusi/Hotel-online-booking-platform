package com.zhisu.hotel.booking.controller;

import com.zhisu.hotel.booking.dto.CancelOrderRequest;
import com.zhisu.hotel.booking.dto.CreateOrderRequest;
import com.zhisu.hotel.booking.service.BookingOrderService;
import com.zhisu.hotel.booking.vo.OrderDetailVO;
import com.zhisu.hotel.booking.vo.OrderVO;
import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.payment.vo.CancellationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class BookingController {

    private final BookingOrderService bookingOrderService;

    @PostMapping
    public ApiResponse<OrderDetailVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = getCurrentUserId();
        OrderDetailVO vo = bookingOrderService.createOrder(request, userId);
        return ApiResponse.success(vo);
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderVO>> getMyOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageResponse<OrderVO> result = bookingOrderService.getMyOrders(userId, status, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        OrderDetailVO vo = bookingOrderService.getOrderDetail(id, userId);
        return ApiResponse.success(vo);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<CancellationVO> cancelOrder(@PathVariable Long id,
                                                    @Valid @RequestBody CancelOrderRequest request) {
        Long userId = getCurrentUserId();
        CancellationVO vo = bookingOrderService.cancelOrder(id, request, userId);
        return ApiResponse.success(vo);
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("UNAUTHORIZED", "请先登录");
        }
        return userId;
    }
}
