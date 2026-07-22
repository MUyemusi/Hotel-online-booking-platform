package com.zhisu.hotel.payment.service.impl;

import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.common.enums.OrderStatus;
import com.zhisu.hotel.common.enums.PaymentStatus;
import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.payment.dto.PaymentRequest;
import com.zhisu.hotel.payment.entity.PaymentRecord;
import com.zhisu.hotel.payment.mapper.PaymentRecordMapper;
import com.zhisu.hotel.payment.vo.PaymentVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {
    @Test
    void paymentIsDeterministicAndConfirmsInventory() {
        PaymentRecordMapper records = mock(PaymentRecordMapper.class);
        BookingOrderMapper orders = mock(BookingOrderMapper.class);
        InventoryService inventory = mock(InventoryService.class);
        PaymentServiceImpl service = new PaymentServiceImpl(records, orders, inventory);
        BookingOrder order = BookingOrder.builder().id(9L).userId(3L).orderNo("O9")
                .orderStatus(OrderStatus.PENDING_PAYMENT.name()).paymentStatus(PaymentStatus.UNPAID.name())
                .totalAmount(new BigDecimal("128.00")).totalAmountFen(12800)
                .paymentTimeout(LocalDateTime.now().plusMinutes(10)).build();
        when(orders.selectById(9L)).thenReturn(order);
        when(records.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> { invocation.<PaymentRecord>getArgument(0).setId(88L); return 1; })
                .when(records).insert(any(PaymentRecord.class));
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(9L); request.setPaymentMethod("MOCK"); request.setIdempotencyKey("pay-9");

        PaymentVO result = service.processPayment(request, 3L);

        assertEquals(PaymentStatus.PAID.name(), result.getPaymentStatus());
        assertEquals(OrderStatus.PENDING_CHECK_IN.name(), order.getOrderStatus());
        verify(inventory).confirmInventory(9L);
    }

    @Test
    void repeatedIdempotencyKeyReturnsExistingPayment() {
        PaymentRecordMapper records = mock(PaymentRecordMapper.class);
        BookingOrderMapper orders = mock(BookingOrderMapper.class);
        PaymentServiceImpl service = new PaymentServiceImpl(records, orders, mock(InventoryService.class));
        PaymentRecord existing = PaymentRecord.builder().id(5L).orderId(9L).userId(3L)
                .paymentNo("P5").paymentStatus("PAID").amount(new BigDecimal("20.00")).build();
        when(records.selectOne(any())).thenReturn(existing);
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(9L); request.setPaymentMethod("MOCK"); request.setIdempotencyKey("same");

        assertEquals(5L, service.processPayment(request, 3L).getId());
        verifyNoInteractions(orders);
        verify(records, never()).insert(any(PaymentRecord.class));
    }
}