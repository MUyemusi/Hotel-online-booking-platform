package com.zhisu.hotel.payment.controller;

import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.payment.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PaymentControllerTest {

    private final PaymentService paymentService = mock(PaymentService.class);
    private final PaymentController controller = new PaymentController(paymentService);

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void paymentRecordsUseAuthenticatedUserInsteadOfFixedUser() {
        UserContext.setUserId(42L);

        controller.getPayments(9L);

        verify(paymentService).getPaymentRecords(9L, 42L);
    }

    @Test
    void unauthenticatedRequestIsRejected() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.getPayments(9L));

        assertEquals("UNAUTHORIZED", exception.getCode());
    }
}
