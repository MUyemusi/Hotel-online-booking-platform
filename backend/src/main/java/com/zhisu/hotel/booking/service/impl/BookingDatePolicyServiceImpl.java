package com.zhisu.hotel.booking.service.impl;

import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingDatePolicyServiceImpl implements BookingDatePolicyService {

    private static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Shanghai");

    private final SystemConfigService systemConfigService;

    @Override
    public LocalDate getHotelToday() {
        return LocalDate.now(HOTEL_ZONE);
    }

    @Override
    public LocalDate getLatestCheckInDate() {
        int bookingWindowMonths = systemConfigService.getInteger("BOOKING_WINDOW_MONTHS");
        return getHotelToday().plusMonths(bookingWindowMonths);
    }

    @Override
    public void validateBookingDates(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            throw new BusinessException("BOOKING_DATE_EMPTY", "Check-in and check-out dates are required");
        }

        LocalDate today = getHotelToday();
        LocalDate latestCheckInDate = getLatestCheckInDate();

        if (checkInDate.isBefore(today)) {
            throw new BusinessException("CHECK_IN_DATE_EXPIRED", "Check-in date cannot be earlier than hotel today");
        }

        if (checkInDate.isAfter(latestCheckInDate)) {
            throw new BusinessException("CHECK_IN_DATE_TOO_LATE", "Latest available check-in date is " + latestCheckInDate);
        }

        if (!checkOutDate.isAfter(checkInDate)) {
            throw new BusinessException("CHECK_OUT_DATE_INVALID", "Check-out date must be later than check-in date");
        }

        long stayNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        int maxStayNights = systemConfigService.getInteger("MAX_STAY_NIGHTS");
        if (stayNights > maxStayNights) {
            throw new BusinessException("STAY_NIGHTS_TOO_LONG", "Maximum stay nights is " + maxStayNights);
        }
    }
}