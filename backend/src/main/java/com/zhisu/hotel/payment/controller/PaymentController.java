package com.zhisu.hotel.payment.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.auth.security.CurrentUserProvider;
import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.payment.dto.PaymentRequest;
import com.zhisu.hotel.payment.service.PaymentService;
import com.zhisu.hotel.payment.vo.PaymentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ApiResponse<PaymentVO> pay(@Valid @RequestBody PaymentRequest request) {
        Long userId = CurrentUserProvider.requireUserId();
        PaymentVO vo = paymentService.processPayment(request, userId);
        return ApiResponse.success(vo);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<List<PaymentVO>> getPayments(@PathVariable Long orderId) {
        Long userId = CurrentUserProvider.requireUserId();
        List<PaymentVO> records = paymentService.getPaymentRecords(orderId, userId);
        return ApiResponse.success(records);
    }

    @GetMapping("/admin/{orderId}")
    public ApiResponse<List<PaymentVO>> getAdminPayments(@PathVariable Long orderId) {
        List<PaymentVO> records = paymentService.getPaymentRecordsByOrderId(orderId);
        return ApiResponse.success(records);
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("UNAUTHORIZED", "请先登录");
        }
        return userId;
    }
}
