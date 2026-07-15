package com.zhisu.hotel.booking.vo;

import com.zhisu.hotel.payment.vo.CancellationVO;
import com.zhisu.hotel.payment.vo.PaymentVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private Long roomTypeId;
    private String roomTypeName;
    private String roomTypeSnapshot;
    private String facilitiesSnapshot;
    private Integer roomCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer totalNights;
    private Integer guestCount;
    private BigDecimal totalAmount;
    private String phone;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime paymentTimeout;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private String cancelType;
    private String cancelReason;
    private LocalDateTime createdAt;
    private List<GuestVO> guests;
    private List<DailyPriceVO> dailyPrices;
    private PaymentVO latestPayment;
    private CancellationVO cancellation;
    private Long paymentRemainingSeconds;
}
