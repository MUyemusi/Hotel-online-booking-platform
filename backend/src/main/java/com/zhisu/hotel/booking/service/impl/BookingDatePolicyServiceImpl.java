
package com.zhisu.hotel.booking.service.impl;

import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.booking.service.StayDateRange;
import com.zhisu.hotel.common.enums.StayType;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingDatePolicyServiceImpl implements BookingDatePolicyService {

    private static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Shanghai");

    private final SystemConfigService systemConfigService;
    private final Clock clock;

    @Override
    public LocalDate getHotelToday() {
        return LocalDate.now(clock.withZone(HOTEL_ZONE));
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

    @Override
    public StayDateRange resolveStay(LocalDate checkInDate, LocalDate checkOutDate, String stayTypeValue) {
        StayType stayType = StayType.from(stayTypeValue);
        if (stayType == null) throw new BusinessException("STAY_TYPE_INVALID", "??????");
        if (stayType == StayType.STANDARD) {
            validateBookingDates(checkInDate, checkOutDate);
            return new StayDateRange(stayType, checkInDate, checkOutDate, checkInDate, checkOutDate,
                    Math.toIntExact(ChronoUnit.DAYS.between(checkInDate, checkOutDate)));
        }
        LocalDate today = getHotelToday();
        if (!isEarlyMorningWindow()) {
            throw new BusinessException("EARLY_MORNING_WINDOW_CLOSED", "????????00:00?06:00??");
        }
        if (checkInDate == null || checkOutDate == null || !today.equals(checkInDate) || !today.equals(checkOutDate)) {
            throw new BusinessException("EARLY_MORNING_DATE_INVALID", "??????????????????");
        }
        return new StayDateRange(stayType, today, today, today.minusDays(1), today, 1);
    }

    @Override
    public boolean isEarlyMorningWindow() {
        LocalTime end = LocalTime.of(6, 0);
        try {
            end = systemConfigService.getTime("EARLY_MORNING_END_TIME");
        } catch (BusinessException ignored) {
            // ?????????????????
        }
        return LocalTime.now(clock.withZone(HOTEL_ZONE)).isBefore(end);
    }
}
