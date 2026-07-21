package com.zhisu.hotel.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.audit.entity.OperationLog;
import com.zhisu.hotel.audit.mapper.OperationLogMapper;
import com.zhisu.hotel.audit.service.OperationLogService;
import com.zhisu.hotel.audit.vo.OperationLogVO;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.common.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(OperationLog operationLog) {
        if (operationLog == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "操作日志不能为空");
        }
        if (operationLogMapper.insert(operationLog) != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "操作日志写入失败");
        }
    }

    @Override
    public PageResponse<OperationLogVO> getOperationLogPage(
            long current,
            long size,
            Long operatorId,
            String module,
            String action,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "开始时间不能晚于结束时间");
        }

        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(operatorId != null, OperationLog::getOperatorId, operatorId)
                .eq(StringUtils.hasText(module), OperationLog::getModule, normalize(module))
                .eq(StringUtils.hasText(action), OperationLog::getAction, normalize(action))
                .eq(StringUtils.hasText(result), OperationLog::getResult, normalize(result))
                .ge(startTime != null, OperationLog::getCreatedAt, startTime)
                .le(endTime != null, OperationLog::getCreatedAt, endTime)
                .orderByDesc(OperationLog::getCreatedAt)
                .orderByDesc(OperationLog::getId);

        Page<OperationLog> page = operationLogMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                wrapper
        );
        List<OperationLogVO> records = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResponse.<OperationLogVO>builder()
                .records(records)
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    public OperationLogVO getOperationLogById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "日志ID不能为空");
        }
        OperationLog operationLog = operationLogMapper.selectById(id);
        if (operationLog == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "操作日志不存在");
        }
        return toVO(operationLog);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private OperationLogVO toVO(OperationLog operationLog) {
        return OperationLogVO.builder()
                .id(operationLog.getId())
                .operatorId(operationLog.getOperatorId())
                .module(operationLog.getModule())
                .action(operationLog.getAction())
                .requestMethod(operationLog.getRequestMethod())
                .requestUri(operationLog.getRequestUri())
                .result(operationLog.getResult())
                .ipAddress(operationLog.getIpAddress())
                .detail(operationLog.getDetail())
                .createdAt(operationLog.getCreatedAt())
                .build();
    }
}
