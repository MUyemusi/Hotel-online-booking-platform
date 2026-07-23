
package com.zhisu.hotel.pricing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.pricing.entity.DailyPrice;
import com.zhisu.hotel.pricing.mapper.DailyPriceMapper;
import com.zhisu.hotel.pricing.service.DailyPriceService;
import com.zhisu.hotel.pricing.service.HolidayCalendarService;
import com.zhisu.hotel.pricing.vo.DatePriceRule;
import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.service.RoomTypeService;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyPriceServiceImpl implements DailyPriceService {

    private static final int MAX_GENERATE_DAYS = 450;

    private final DailyPriceMapper dailyPriceMapper;
    private final RoomTypeService roomTypeService;
    private final HolidayCalendarService holidayCalendarService;
    private final BookingDatePolicyService bookingDatePolicyService;
    private final SystemConfigService systemConfigService;

    @Override
    @Transactional
    public int generateAllRoomTypePrices(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        Page<RoomTypeSummary> roomTypePage = roomTypeService.pageRoomTypes(1, 1000, null, null, null, null, null, null);
        if (roomTypePage.getRecords().isEmpty()) {
            throw new BusinessException("ROOM_TYPE_EMPTY", "没有可生成每日价格的房型");
        }

        int processedCount = 0;
        for (RoomTypeSummary roomType : roomTypePage.getRecords()) {
            validateBasePrice(roomType.getId(), roomType.getBasePrice());
            processedCount += generateOneRoomType(roomType.getId(), roomType.getBasePrice(), startDate, endDate);
        }
        return processedCount;
    }

    @Override
    @Transactional
    public int generateRoomTypePrices(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        if (roomTypeId == null) {
            throw new BusinessException("ROOM_TYPE_ID_EMPTY", "房型ID不能为空");
        }
        validateDateRange(startDate, endDate);
        RoomType roomType = roomTypeService.getRoomTypeDetail(roomTypeId);
        if (roomType == null) {
            throw new BusinessException("ROOM_TYPE_NOT_FOUND", "房型不存在");
        }
        validateBasePrice(roomType.getId(), roomType.getBasePrice());
        return generateOneRoomType(roomType.getId(), roomType.getBasePrice(), startDate, endDate);
    }

    @Override
    public List<DailyPrice> listDailyPrices(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        if (roomTypeId == null) {
            throw new BusinessException("ROOM_TYPE_ID_EMPTY", "房型ID不能为空");
        }
        validateDateRange(startDate, endDate);
        return dailyPriceMapper.selectList(
                new LambdaQueryWrapper<DailyPrice>()
                        .eq(DailyPrice::getRoomTypeId, roomTypeId)
                        .between(DailyPrice::getBusinessDate, startDate, endDate)
                        .orderByAsc(DailyPrice::getBusinessDate)
        );
    }

    @Override
    @Transactional
    public int ensureRollingWindowPrices() {
        LocalDate today = bookingDatePolicyService.getHotelToday();
        LocalDate latestCheckIn = bookingDatePolicyService.getLatestCheckInDate();
        int maxStayNights = systemConfigService.getInteger("MAX_STAY_NIGHTS");
        LocalDate requiredEndDate = latestCheckIn.plusDays(maxStayNights - 1L);
        return generateAllRoomTypePrices(today, requiredEndDate);
    }

    @Override
    @Transactional
    public void ensurePricesExist(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        if (roomTypeId == null) {
            throw new BusinessException("ROOM_TYPE_ID_EMPTY", "房型ID不能为空");
        }
        validateDateRange(startDate, endDate);
        long expectedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        Long actualCount = dailyPriceMapper.selectCount(
                new LambdaQueryWrapper<DailyPrice>()
                        .eq(DailyPrice::getRoomTypeId, roomTypeId)
                        .between(DailyPrice::getBusinessDate, startDate, endDate)
        );
        if (actualCount == null || actualCount != expectedDays) {
            generateRoomTypePrices(roomTypeId, startDate, endDate);
        }
    }

    @Override
    @Transactional
    public DailyPrice setManualOverride(Long roomTypeId, LocalDate businessDate, BigDecimal manualPrice, String overrideReason) {
        DailyPrice existing = getDailyPriceByRoomTypeAndDate(roomTypeId, businessDate, true);
        applyManualOverride(existing, manualPrice, overrideReason);
        return dailyPriceMapper.selectById(existing.getId());
    }

    @Override
    @Transactional
    public DailyPrice clearManualOverride(Long roomTypeId, LocalDate businessDate) {
        DailyPrice existing = getDailyPriceByRoomTypeAndDate(roomTypeId, businessDate, false);
        removeManualOverride(existing);
        return dailyPriceMapper.selectById(existing.getId());
    }

    @Override
    @Transactional
    public DailyPrice setManualOverrideById(Long dailyPriceId, BigDecimal manualPrice, String overrideReason) {
        DailyPrice existing = getDailyPriceById(dailyPriceId);
        applyManualOverride(existing, manualPrice, overrideReason);
        return dailyPriceMapper.selectById(existing.getId());
    }

    @Override
    @Transactional
    public DailyPrice clearManualOverrideById(Long dailyPriceId) {
        DailyPrice existing = getDailyPriceById(dailyPriceId);
        removeManualOverride(existing);
        return dailyPriceMapper.selectById(existing.getId());
    }

    private int generateOneRoomType(Long roomTypeId, BigDecimal basePrice, LocalDate startDate, LocalDate endDate) {
        int processedCount = 0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DatePriceRule rule = holidayCalendarService.resolveDateRule(currentDate, roomTypeId);
            BigDecimal finalPrice = basePrice.multiply(rule.coefficient()).setScale(2, RoundingMode.HALF_UP);
            DailyPrice existing = findExistingPrice(roomTypeId, currentDate);
            if (existing == null) {
                DailyPrice newPrice = new DailyPrice();
                newPrice.setRoomTypeId(roomTypeId);
                newPrice.setBusinessDate(currentDate);
                newPrice.setManualOverride(0);
                upsertDailyPrice(newPrice, basePrice, finalPrice, rule, false);
            } else {
                upsertDailyPrice(existing, basePrice, finalPrice, rule, true);
            }
            processedCount++;
            currentDate = currentDate.plusDays(1);
        }
        return processedCount;
    }

    private void upsertDailyPrice(DailyPrice dailyPrice, BigDecimal basePrice, BigDecimal calculatedFinalPrice, DatePriceRule rule, boolean existingRecord) {
        fillCalculatedFields(dailyPrice, basePrice, calculatedFinalPrice, rule);
        applyFinalPrice(dailyPrice);
        try {
            if (existingRecord) {
                dailyPriceMapper.updateById(dailyPrice);
            } else {
                dailyPriceMapper.insert(dailyPrice);
            }
        } catch (DuplicateKeyException ex) {
            DailyPrice latest = findExistingPrice(dailyPrice.getRoomTypeId(), dailyPrice.getBusinessDate());
            if (latest == null) {
                throw ex;
            }
            fillCalculatedFields(latest, basePrice, calculatedFinalPrice, rule);
            applyFinalPrice(latest);
            dailyPriceMapper.updateById(latest);
        }
    }

    private void applyManualOverride(DailyPrice existing, BigDecimal manualPrice, String overrideReason) {
        if (manualPrice == null || manualPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("MANUAL_PRICE_INVALID", "人工价格必须大于0");
        }
        existing.setManualPrice(manualPrice.setScale(2, RoundingMode.HALF_UP));
        existing.setManualOverride(1);
        existing.setOverrideReason(normalizeReason(overrideReason));
        existing.setFinalPrice(existing.getManualPrice());
        dailyPriceMapper.updateById(existing);
    }

    private void removeManualOverride(DailyPrice existing) {
        existing.setManualPrice(null);
        existing.setManualOverride(0);
        existing.setOverrideReason(null);
        DatePriceRule rule = holidayCalendarService.resolveDateRule(existing.getBusinessDate(), existing.getRoomTypeId());
        BigDecimal recalculatedFinalPrice = existing.getBasePrice()
                .multiply(rule.coefficient())
                .setScale(2, RoundingMode.HALF_UP);
        fillCalculatedFields(existing, existing.getBasePrice(), recalculatedFinalPrice, rule);
        dailyPriceMapper.updateById(existing);
    }

    private DailyPrice getDailyPriceById(Long dailyPriceId) {
        if (dailyPriceId == null) {
            throw new BusinessException("DAILY_PRICE_ID_EMPTY", "每日价格ID不能为空");
        }
        DailyPrice existing = dailyPriceMapper.selectById(dailyPriceId);
        if (existing == null) {
            throw new BusinessException("DAILY_PRICE_NOT_FOUND", "每日价格记录不存在");
        }
        return existing;
    }

    private DailyPrice getDailyPriceByRoomTypeAndDate(Long roomTypeId, LocalDate businessDate, boolean ensureExists) {
        if (roomTypeId == null || businessDate == null) {
            throw new BusinessException("MANUAL_OVERRIDE_PARAM_INVALID", "房型ID和营业日期不能为空");
        }
        if (ensureExists) {
            ensurePricesExist(roomTypeId, businessDate, businessDate);
        }
        DailyPrice existing = findExistingPrice(roomTypeId, businessDate);
        if (existing == null) {
            throw new BusinessException("DAILY_PRICE_NOT_FOUND", "每日价格记录不存在");
        }
        return existing;
    }

    private DailyPrice findExistingPrice(Long roomTypeId, LocalDate businessDate) {
        return dailyPriceMapper.selectOne(
                new LambdaQueryWrapper<DailyPrice>()
                        .eq(DailyPrice::getRoomTypeId, roomTypeId)
                        .eq(DailyPrice::getBusinessDate, businessDate)
        );
    }

    private void fillCalculatedFields(DailyPrice dailyPrice, BigDecimal basePrice, BigDecimal finalPrice, DatePriceRule rule) {
        dailyPrice.setBasePrice(basePrice.setScale(2, RoundingMode.HALF_UP));
        dailyPrice.setDateCoefficient(rule.coefficient().setScale(2, RoundingMode.HALF_UP));
        if (!Boolean.TRUE.equals(isManualOverride(dailyPrice))) {
            dailyPrice.setFinalPrice(finalPrice);
        }
        dailyPrice.setPriceSource(rule.priceSource());
        dailyPrice.setPriceDescription(rule.description());
        dailyPrice.setHolidayCalendarId(rule.holidayCalendarId());
        dailyPrice.setSourceStatus(rule.sourceStatus());
    }

    private void applyFinalPrice(DailyPrice dailyPrice) {
        if (Boolean.TRUE.equals(isManualOverride(dailyPrice)) && dailyPrice.getManualPrice() != null) {
            dailyPrice.setFinalPrice(dailyPrice.getManualPrice().setScale(2, RoundingMode.HALF_UP));
        }
    }

    private Boolean isManualOverride(DailyPrice dailyPrice) {
        return dailyPrice.getManualOverride() != null && dailyPrice.getManualOverride() == 1;
    }

    private String normalizeReason(String overrideReason) {
        if (overrideReason == null || overrideReason.isBlank()) {
            return "MANUAL_OVERRIDE";
        }
        return overrideReason.trim();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("DATE_EMPTY", "开始日期和结束日期不能为空");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("DATE_RANGE_INVALID", "结束日期不能早于开始日期");
        }
        long inclusiveDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (inclusiveDays > MAX_GENERATE_DAYS) {
            throw new BusinessException("DATE_RANGE_TOO_LONG", "单次最多生成450天的每日价格");
        }
    }

    private void validateBasePrice(Long roomTypeId, BigDecimal basePrice) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("BASE_PRICE_INVALID", "房型基础价格无效：" + roomTypeId);
        }
    }
}
