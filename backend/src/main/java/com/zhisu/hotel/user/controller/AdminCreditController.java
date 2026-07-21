package com.zhisu.hotel.user.controller;

import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.user.dto.UnbanUserRequest;
import com.zhisu.hotel.user.service.CreditService;
import com.zhisu.hotel.user.vo.CreditRecordVO;
import com.zhisu.hotel.user.vo.CreditSummaryVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users/{userId}/credit")
public class AdminCreditController {

    private final CreditService creditService;

    public AdminCreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @GetMapping
    public ApiResponse<CreditSummaryVO> getCreditSummary(@PathVariable Long userId) {
        return ApiResponse.success(creditService.getCreditSummary(userId));
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<CreditRecordVO>> getCreditRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(creditService.getCreditRecords(userId, current, size));
    }

    @PutMapping("/unban")
    public ApiResponse<Void> unbanUser(
            @PathVariable Long userId,
            @Valid @RequestBody UnbanUserRequest request) {
        Long operatorId = UserContext.getUserId();
        if (operatorId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        creditService.unbanUser(userId, operatorId, request.getReason());
        return ApiResponse.success();
    }
}
