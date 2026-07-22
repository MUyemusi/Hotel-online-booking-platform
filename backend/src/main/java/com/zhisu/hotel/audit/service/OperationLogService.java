package com.zhisu.hotel.audit.service;

import com.zhisu.hotel.audit.entity.OperationLog;
import com.zhisu.hotel.audit.vo.OperationLogVO;
import com.zhisu.hotel.common.response.PageResponse;

import java.time.LocalDateTime;

public interface OperationLogService {

    void record(OperationLog operationLog);

    PageResponse<OperationLogVO> getOperationLogPage(
            long current,
            long size,
            Long operatorId,
            String module,
            String action,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    OperationLogVO getOperationLogById(Long id);
}
