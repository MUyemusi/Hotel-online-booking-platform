package com.zhisu.hotel.pricing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.pricing.dto.HolidayCalendarSaveRequest;
import com.zhisu.hotel.pricing.entity.HolidayCalendar;
import com.zhisu.hotel.pricing.vo.DatePriceRule;

import java.time.LocalDate;

public interface HolidayCalendarService {

    Page<HolidayCalendar> pageHolidayCalendar(int page, int size, Integer holidayYear, String dayType, String sourceStatus, String status);

    HolidayCalendar getHolidayCalendar(Long id);

    HolidayCalendar createHolidayCalendar(HolidayCalendarSaveRequest request);

    HolidayCalendar updateHolidayCalendar(Long id, HolidayCalendarSaveRequest request);

    HolidayCalendar updateHolidayCalendarStatus(Long id, String status);

    void deleteHolidayCalendar(Long id);

    DatePriceRule resolveDateRule(LocalDate date);

    DatePriceRule resolveDateRule(LocalDate date, Long roomTypeId);
}