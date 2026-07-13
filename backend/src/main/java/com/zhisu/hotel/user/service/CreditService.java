package com.zhisu.hotel.user.service;

public interface CreditService {
    
    boolean checkBookingPermission(Long userId);
    
    void recordUserCancellation(Long userId);
    
    void resetContinuousCancellation(Long userId);
}
