package com.zhisu.hotel.payment.service;

import com.zhisu.hotel.payment.dto.PaymentRequest;
import com.zhisu.hotel.payment.vo.PaymentVO;

import java.util.List;

public interface PaymentService {

    PaymentVO processPayment(PaymentRequest request, Long userId);
    List<PaymentVO> getPaymentRecords(Long orderId, Long userId);
    List<PaymentVO> getPaymentRecordsByOrderId(Long orderId);
}
