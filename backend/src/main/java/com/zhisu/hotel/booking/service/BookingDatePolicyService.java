package com.zhisu.hotel.booking.service;

import java.time.LocalDate;

public interface BookingDatePolicyService {

    LocalDate getHotelToday();

    LocalDate getLatestCheckInDate();

    void validateBookingDates(LocalDate checkInDate, LocalDate checkOutDate);
}