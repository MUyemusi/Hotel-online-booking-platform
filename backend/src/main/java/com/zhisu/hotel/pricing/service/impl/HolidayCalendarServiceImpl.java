package com.zhisu.hotel.pricing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.pricing.dto.HolidayCalendarSaveRequest;
import com.zhisu.hotel.pricing.entity.HolidayCalendar;
import com.zhisu.hotel.pricing.entity.PriceRule;
import com.zhisu.hotel.pricing.mapper.HolidayCalendarMapper;
import com.zhisu.hotel.pricing.service.HolidayCalendarService;
import com.zhisu.hotel.pricing.service.PriceRuleService;
import com.zhisu.hotel.pricing.vo.DatePriceRule;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HolidayCalendarServiceImpl implements HolidayCalendarService {

    private final HolidayCalendarMapper holidayCalendarMapper;
    private final SystemConfigService systemConfigService;
    private final PriceRuleService priceRuleService;

    @Override
    public Page<HolidayCalendar> pageHolidayCalendar(int page, int size, Integer holidayYear, String dayType, String sourceStatus, String status) {
        LambdaQueryWrapper<HolidayCalendar> wrapper = new LambdaQueryWrapper<>();
        if (holidayYear != null) {
            wrapper.eq(HolidayCalendar::getHolidayYear, holidayYear);
        }
        if (StringUtils.hasText(dayType)) {
            wrapper.eq(HolidayCalendar::getDayType, dayType.toUpperCase());
        }
        if (StringUtils.hasText(sourceStatus)) {
            wrapper.eq(HolidayCalendar::getSourceStatus, sourceStatus.toUpperCase());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(HolidayCalendar::getStatus, status.toUpperCase());
        }
        wrapper.orderByAsc(HolidayCalendar::getBusinessDate)
                .orderByAsc(HolidayCalendar::getId);
        return holidayCalendarMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public HolidayCalendar getHolidayCalendar(Long id) {
        HolidayCalendar entity = holidayCalendarMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("HOLIDAY_CALENDAR_NOT_FOUND", "Holiday calendar record not found");
        }
        return entity;
    }

    @Override
    @Transactional
    public HolidayCalendar createHolidayCalendar(HolidayCalendarSaveRequest request) {
        validateRequest(request, null);
        HolidayCalendar entity = toEntity(request, new HolidayCalendar());
        holidayCalendarMapper.insert(entity);
        return getHolidayCalendar(entity.getId());
    }

    @Override
    @Transactional
    public HolidayCalendar updateHolidayCalendar(Long id, HolidayCalendarSaveRequest request) {
        HolidayCalendar existing = getHolidayCalendar(id);
        validateRequest(request, id);
        holidayCalendarMapper.updateById(toEntity(request, existing));
        return getHolidayCalendar(id);
    }

    @Override
    @Transactional
    public HolidayCalendar updateHolidayCalendarStatus(Long id, String status) {
        HolidayCalendar entity = getHolidayCalendar(id);
        String normalized = normalizeEnum(status);
        if (!"ENABLED".equals(normalized) && !"DISABLED".equals(normalized)) {
            throw new BusinessException("HOLIDAY_CALENDAR_STATUS_INVALID", "Holiday calendar status is invalid");
        }
        entity.setStatus(normalized);
        holidayCalendarMapper.updateById(entity);
        return getHolidayCalendar(id);
    }

    @Override
    @Transactional
    public void deleteHolidayCalendar(Long id) {
        getHolidayCalendar(id);
        holidayCalendarMapper.deleteById(id);
    }

    @Override
    public DatePriceRule resolveDateRule(LocalDate date) {
        return resolveDateRule(date, null);
    }

    @Override
    public DatePriceRule resolveDateRule(LocalDate date, Long roomTypeId) {
        PriceRule priceRule = priceRuleService.resolveRule(date, roomTypeId);
        if (priceRule != null) {
            return new DatePriceRule(
                    priceRule.getCoefficient().setScale(2, RoundingMode.HALF_UP),
                    "PRICE_RULE",
                    priceRule.getRuleName(),
                    "CUSTOM",
                    null
            );
        }

        HolidayCalendar specialDate = holidayCalendarMapper.selectOne(
                new LambdaQueryWrapper<HolidayCalendar>()
                        .eq(HolidayCalendar::getBusinessDate, date)
                        .eq(HolidayCalendar::getStatus, "ENABLED")
        );

        if (specialDate != null) {
            if ("MAKEUP_WORKDAY".equals(specialDate.getDayType())) {
                return new DatePriceRule(
                        BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP),
                        "MAKEUP_WORKDAY",
                        specialDate.getHolidayName(),
                        specialDate.getSourceStatus(),
                        specialDate.getId()
                );
            }
            if ("HOLIDAY".equals(specialDate.getDayType())) {
                return new DatePriceRule(
                        specialDate.getCoefficient().setScale(2, RoundingMode.HALF_UP),
                        specialDate.getHolidayCode(),
                        specialDate.getHolidayName(),
                        specialDate.getSourceStatus(),
                        specialDate.getId()
                );
            }
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            return new DatePriceRule(systemConfigService.getDecimal("FRIDAY_PRICE_COEFFICIENT").setScale(2, RoundingMode.HALF_UP), "FRIDAY", "Friday", "SYSTEM", null);
        }
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return new DatePriceRule(systemConfigService.getDecimal("SATURDAY_PRICE_COEFFICIENT").setScale(2, RoundingMode.HALF_UP), "SATURDAY", "Saturday", "SYSTEM", null);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return new DatePriceRule(systemConfigService.getDecimal("SUNDAY_PRICE_COEFFICIENT").setScale(2, RoundingMode.HALF_UP), "SUNDAY", "Sunday", "SYSTEM", null);
        }

        return new DatePriceRule(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP), "NORMAL_DAY", "Normal day", "SYSTEM", null);
    }

    private HolidayCalendar toEntity(HolidayCalendarSaveRequest request, HolidayCalendar entity) {
        entity.setBusinessDate(request.getBusinessDate());
        entity.setHolidayName(request.getHolidayName().trim());
        entity.setHolidayCode(normalizeEnum(request.getHolidayCode()));
        entity.setDayType(normalizeEnum(request.getDayType()));
        entity.setCoefficient(request.getCoefficient().setScale(2, RoundingMode.HALF_UP));
        entity.setHolidayYear(request.getHolidayYear());
        entity.setSourceStatus(normalizeEnum(request.getSourceStatus()));
        entity.setStatus(normalizeEnum(request.getStatus()));
        entity.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        return entity;
    }

    private void validateRequest(HolidayCalendarSaveRequest request, Long currentId) {
        String dayType = normalizeEnum(request.getDayType());
        if (!"HOLIDAY".equals(dayType) && !"MAKEUP_WORKDAY".equals(dayType)) {
            throw new BusinessException("HOLIDAY_CALENDAR_DAY_TYPE_INVALID", "Holiday calendar day type is invalid");
        }

        String sourceStatus = normalizeEnum(request.getSourceStatus());
        if (!"OFFICIAL".equals(sourceStatus) && !"PROVISIONAL".equals(sourceStatus)) {
            throw new BusinessException("HOLIDAY_CALENDAR_SOURCE_STATUS_INVALID", "Holiday calendar source status is invalid");
        }

        String status = normalizeEnum(request.getStatus());
        if (!"ENABLED".equals(status) && !"DISABLED".equals(status)) {
            throw new BusinessException("HOLIDAY_CALENDAR_STATUS_INVALID", "Holiday calendar status is invalid");
        }

        if (request.getBusinessDate().getYear() != request.getHolidayYear()) {
            throw new BusinessException("HOLIDAY_CALENDAR_YEAR_MISMATCH", "Business date year and holiday year do not match");
        }

        HolidayCalendar duplicate = holidayCalendarMapper.selectOne(
                new LambdaQueryWrapper<HolidayCalendar>()
                        .eq(HolidayCalendar::getBusinessDate, request.getBusinessDate())
                        .ne(currentId != null, HolidayCalendar::getId, currentId)
        );
        if (duplicate != null) {
            throw new BusinessException("HOLIDAY_CALENDAR_DATE_DUPLICATE", "Business date already exists in holiday calendar");
        }
    }

    private String normalizeEnum(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}