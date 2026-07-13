package com.zhisu.hotel.frontdesk.service.impl;

import com.zhisu.hotel.frontdesk.service.FrontdeskService;
import org.springframework.stereotype.Service;

@Service
public class FrontdeskServiceImpl implements FrontdeskService {

    @Override
    public boolean checkIn(Long orderId, Long roomId) {
        return true;
    }

    @Override
    public boolean checkOut(Long orderId) {
        return true;
    }

    @Override
    public boolean markRoomCleaned(Long roomId) {
        return true;
    }
}
