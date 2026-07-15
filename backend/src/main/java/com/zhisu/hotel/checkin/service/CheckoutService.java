package com.zhisu.hotel.checkin.service;

import com.zhisu.hotel.checkin.dto.ConfirmCheckoutRequest;
import com.zhisu.hotel.checkin.vo.ConfirmCheckoutVO;

public interface CheckoutService {

    ConfirmCheckoutVO confirmCheckout(Long orderId, ConfirmCheckoutRequest request);
}
