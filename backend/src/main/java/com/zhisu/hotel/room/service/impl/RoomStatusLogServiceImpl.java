package com.zhisu.hotel.room.service.impl;

import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.room.entity.RoomStatusLog;
import com.zhisu.hotel.room.mapper.RoomStatusLogMapper;
import com.zhisu.hotel.room.service.RoomStatusLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomStatusLogServiceImpl implements RoomStatusLogService {

    private final RoomStatusLogMapper roomStatusLogMapper;

    @Override
    public RoomStatusLog createLog(Long roomId, RoomStatus beforeStatus, RoomStatus afterStatus,
                                   Long operatorId, String operatorType, String reason, Long relatedOrderId) {
        RoomStatusLog log = RoomStatusLog.builder()
                .roomId(roomId)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .operatorId(operatorId)
                .operatorType(operatorType)
                .reason(reason)
                .relatedOrderId(relatedOrderId)
                .build();
        roomStatusLogMapper.insert(log);
        return log;
    }
}
