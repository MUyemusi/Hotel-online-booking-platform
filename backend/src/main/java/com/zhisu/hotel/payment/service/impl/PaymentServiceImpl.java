package com.zhisu.hotel.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.common.enums.OrderStatus;
import com.zhisu.hotel.common.enums.PaymentStatus;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.payment.dto.PaymentRequest;
import com.zhisu.hotel.payment.entity.PaymentRecord;
import com.zhisu.hotel.payment.mapper.PaymentRecordMapper;
import com.zhisu.hotel.payment.service.PaymentService;
import com.zhisu.hotel.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final BookingOrderMapper bookingOrderMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO processPayment(PaymentRequest request, Long userId) {
        BookingOrder order = bookingOrderMapper.selectById(request.getOrderId());
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("FORBIDDEN", "无权操作该订单");
        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getOrderStatus())) {
            throw new BusinessException("INVALID_ORDER_STATUS", "订单当前状态不支持支付");
        }
        PaymentRecord existingPaid = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getOrderId, order.getId())
                        .eq(PaymentRecord::getPaymentStatus, "SUCCESS"));
        if (existingPaid != null) throw new BusinessException("ALREADY_PAID", "该订单已支付成功，请勿重复支付");
        if (LocalDateTime.now().isAfter(order.getPaymentTimeout())) {
            throw new BusinessException("PAYMENT_TIMEOUT", "订单支付已超时，请重新下单");
        }
        String paymentNo = "PY" + java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss").format(LocalDateTime.now()) + randomDigits(4);
        String transactionId = getTransactionPrefix(request.getPaymentMethod()) + System.currentTimeMillis();
        boolean paySuccess = Math.random() < 0.9;
        LocalDateTime now = LocalDateTime.now();
        if (paySuccess) {
            PaymentRecord record = PaymentRecord.builder().paymentNo(paymentNo).orderId(order.getId())
                    .userId(userId).amount(order.getTotalAmount()).amountFen(order.getTotalAmountFen())
                    .paymentMethod(request.getPaymentMethod()).transactionId(transactionId)
                    .paymentStatus("SUCCESS").paidAt(now).build();
            paymentRecordMapper.insert(record);
            order.setOrderStatus(OrderStatus.PENDING_CHECK_IN.name());
            order.setPaymentStatus(PaymentStatus.PAID.name());
            order.setPaidAt(now);
            bookingOrderMapper.updateById(order);
            inventoryService.confirmInventory(order.getId());
            log.info("支付成功: {} orderNo={} method={} amount={}", paymentNo, order.getOrderNo(), request.getPaymentMethod(), order.getTotalAmount());
            return PaymentVO.builder().id(record.getId()).paymentNo(record.getPaymentNo())
                    .orderId(record.getOrderId()).amount(record.getAmount())
                    .paymentMethod(record.getPaymentMethod()).transactionId(record.getTransactionId())
                    .paymentStatus(record.getPaymentStatus()).paidAt(record.getPaidAt())
                    .createdAt(record.getCreatedAt()).build();
        } else {
            PaymentRecord record = PaymentRecord.builder().paymentNo(paymentNo).orderId(order.getId())
                    .userId(userId).amount(order.getTotalAmount()).amountFen(order.getTotalAmountFen())
                    .paymentMethod(request.getPaymentMethod()).transactionId(transactionId)
                    .paymentStatus("FAILED").build();
            paymentRecordMapper.insert(record);
            log.info("支付失败: {} orderNo={} method={}", paymentNo, order.getOrderNo(), request.getPaymentMethod());
            throw new BusinessException("PAYMENT_FAILED", "支付失败，请重试");
        }
    }

    @Override
    public List<PaymentVO> getPaymentRecords(Long orderId, Long userId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("FORBIDDEN", "无权查看该订单的支付记录");
        }
        return getPaymentRecordsInternal(orderId);
    }

    @Override
    public List<PaymentVO> getPaymentRecordsByOrderId(Long orderId) {
        return getPaymentRecordsInternal(orderId);
    }

    private List<PaymentVO> getPaymentRecordsInternal(Long orderId) {
        List<PaymentRecord> records = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getOrderId, orderId)
                        .orderByDesc(PaymentRecord::getCreatedAt));
        return records.stream().map(r -> PaymentVO.builder().id(r.getId()).paymentNo(r.getPaymentNo())
                .orderId(r.getOrderId()).amount(r.getAmount()).paymentMethod(r.getPaymentMethod())
                .transactionId(r.getTransactionId()).paymentStatus(r.getPaymentStatus())
                .paidAt(r.getPaidAt()).refundAmount(r.getRefundAmount())
                .refundTransactionId(r.getRefundTransactionId()).refundedAt(r.getRefundedAt())
                .createdAt(r.getCreatedAt()).build()).collect(Collectors.toList());
    }

    private String getTransactionPrefix(String method) {
        return switch (method) { case "WECHAT" -> "WX"; case "ALIPAY" -> "ALI"; case "BANK_CARD" -> "BC"; default -> "TX"; } + System.currentTimeMillis();
    }

    private String randomDigits(int len) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < len; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }
}
