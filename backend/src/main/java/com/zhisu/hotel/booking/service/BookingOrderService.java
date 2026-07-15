package com.zhisu.hotel.booking.service;

import com.zhisu.hotel.booking.dto.AdminOrderSearchRequest;
import com.zhisu.hotel.booking.dto.CancelOrderRequest;
import com.zhisu.hotel.booking.dto.CreateOrderRequest;
import com.zhisu.hotel.booking.vo.OrderDetailVO;
import com.zhisu.hotel.booking.vo.OrderVO;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.payment.vo.CancellationVO;

public interface BookingOrderService {

    OrderDetailVO createOrder(CreateOrderRequest request, Long userId);
    PageResponse<OrderVO> getMyOrders(Long userId, String orderStatus, int page, int size);
    OrderDetailVO getOrderDetail(Long orderId, Long userId);
    CancellationVO cancelOrder(Long orderId, CancelOrderRequest request, Long userId);
    PageResponse<OrderVO> adminOrderList(AdminOrderSearchRequest request);
    CancellationVO adminCancelOrder(Long orderId, String reason);
    void adminUpdateStatus(Long orderId, String targetStatus);
}
