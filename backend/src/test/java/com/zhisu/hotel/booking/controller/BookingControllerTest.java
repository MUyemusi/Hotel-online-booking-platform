package com.zhisu.hotel.booking.controller;

import com.zhisu.hotel.booking.service.BookingOrderService;
import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BookingControllerTest {

    private final BookingOrderService bookingOrderService = mock(BookingOrderService.class);
    private final BookingController controller = new BookingController(bookingOrderService);

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void myOrdersUseAuthenticatedUserInsteadOfFixedUser() {
        UserContext.setUserId(42L);

        controller.getMyOrders(null, 1, 10);

        verify(bookingOrderService).getMyOrders(42L, null, 1, 10);
    }

    @Test
    void unauthenticatedRequestIsRejected() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.getMyOrders(null, 1, 10));

        assertEquals("UNAUTHORIZED", exception.getCode());
    }
}
