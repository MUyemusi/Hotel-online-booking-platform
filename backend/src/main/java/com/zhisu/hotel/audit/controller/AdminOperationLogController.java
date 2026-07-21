package com.zhisu.hotel.audit.controller;

import com.zhisu.hotel.audit.service.OperationLogService;
import com.zhisu.hotel.audit.vo.OperationLogVO;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.common.response.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/operation-logs")
public class AdminOperationLogController {

    private final OperationLogService operationLogService;

    public AdminOperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<OperationLogVO>> getOperationLogs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String result,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ApiResponse.success(operationLogService.getOperationLogPage(
                current,
                size,
                operatorId,
                module,
                action,
                result,
                startTime,
                endTime
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<OperationLogVO> getOperationLog(@PathVariable Long id) {
        return ApiResponse.success(operationLogService.getOperationLogById(id));
    }
}
