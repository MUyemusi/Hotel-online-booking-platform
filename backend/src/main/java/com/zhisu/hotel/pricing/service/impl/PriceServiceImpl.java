package com.zhisu.hotel.pricing.service.impl;

import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.pricing.entity.DailyPrice;
import com.zhisu.hotel.pricing.service.DailyPriceService;
import com.zhisu.hotel.pricing.service.HolidayCalendarService;
import com.zhisu.hotel.pricing.service.PriceService;
import com.zhisu.hotel.pricing.vo.DatePriceRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final HolidayCalendarService holidayCalendarService;
    private final DailyPriceService dailyPriceService;
    private final BookingDatePolicyService bookingDatePolicyService;

    @Override
    public BigDecimal calculatePrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count) {
        if (roomTypeId == null) {
            throw new BusinessException("ROOM_TYPE_ID_EMPTY", "Room type id cannot be null");
        }
        if (count <= 0) {
            throw new BusinessException("ROOM_COUNT_INVALID", "Room count must be greater than 0");
        }

        bookingDatePolicyService.validateBookingDates(checkIn, checkOut);

        LocalDate lastStayDate = checkOut.minusDays(1);
        dailyPriceService.ensurePricesExist(roomTypeId, checkIn, lastStayDate);

        List<DailyPrice> prices = dailyPriceService.listDailyPrices(roomTypeId, checkIn, lastStayDate);
        long expectedNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (prices.size() != expectedNights) {
            throw new BusinessException("DAILY_PRICE_MISSING", "Daily prices are incomplete for the selected stay dates");
        }

        BigDecimal oneRoomAmount = prices.stream()
                .map(DailyPrice::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return oneRoomAmount.multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<DailyPrice> listDailyPrices(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        LocalDate lastStayDate = checkOut.minusDays(1);
        dailyPriceService.ensurePricesExist(roomTypeId, checkIn, lastStayDate);
        return dailyPriceService.listDailyPrices(roomTypeId, checkIn, lastStayDate);
    }
    @Override
    public BigDecimal calculateDailyPrice(BigDecimal basePrice, LocalDate date) {
        return basePrice.multiply(resolveDateRule(date).coefficient()).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public DatePriceRule resolveDateRule(LocalDate date) {
        return holidayCalendarService.resolveDateRule(date);
    }
}