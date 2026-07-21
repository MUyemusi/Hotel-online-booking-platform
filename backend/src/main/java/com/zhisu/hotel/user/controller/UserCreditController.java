package com.zhisu.hotel.user.controller;

import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.user.service.CreditService;
import com.zhisu.hotel.user.vo.CreditRecordVO;
import com.zhisu.hotel.user.vo.CreditSummaryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/credit")
public class UserCreditController {

    private final CreditService creditService;

    public UserCreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @GetMapping
    public ApiResponse<CreditSummaryVO> getCreditSummary() {
        return ApiResponse.success(creditService.getCreditSummary(requireCurrentUserId()));
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<CreditRecordVO>> getCreditRecords(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(
                creditService.getCreditRecords(requireCurrentUserId(), current, size)
        );
    }

    private Long requireCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return userId;
    }
}
