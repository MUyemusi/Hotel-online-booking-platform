package com.zhisu.hotel.booking.service;

import com.zhisu.hotel.booking.vo.FrontdeskOrderInfo;

public interface BookingFrontdeskService {

    FrontdeskOrderInfo findByOrderNo(String orderNo);
    FrontdeskOrderInfo findByPhone(String phone);
    FrontdeskOrderInfo getByOrderId(Long orderId);
    void confirmCheckedIn(Long orderId);
    void confirmCompleted(Long orderId);
}
