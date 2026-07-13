package com.zhisu.hotel.user.service.impl;

import com.zhisu.hotel.user.service.CreditService;
import org.springframework.stereotype.Service;

@Service
public class CreditServiceImpl implements CreditService {

    @Override
    public boolean checkBookingPermission(Long userId) {
        return true;
    }

    @Override
    public void recordUserCancellation(Long userId) {
    }

    @Override
    public void resetContinuousCancellation(Long userId) {
    }
}
