package com.zhisu.hotel.frontdesk.service;

public interface FrontdeskService {
    
    boolean checkIn(Long orderId, Long roomId);
    
    boolean checkOut(Long orderId);
    
    boolean markRoomCleaned(Long roomId);
}
