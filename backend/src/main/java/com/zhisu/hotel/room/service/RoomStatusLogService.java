package com.zhisu.hotel.room.service;

import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.room.entity.RoomStatusLog;

public interface RoomStatusLogService {

    RoomStatusLog createLog(Long roomId, RoomStatus beforeStatus, RoomStatus afterStatus,
                           Long operatorId, String operatorType, String reason, Long relatedOrderId);
}
