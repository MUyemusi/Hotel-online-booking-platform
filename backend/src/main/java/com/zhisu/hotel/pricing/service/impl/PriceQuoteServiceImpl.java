package com.zhisu.hotel.pricing.service.impl;

import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.pricing.entity.DailyPrice;
import com.zhisu.hotel.pricing.service.DailyPriceService;
import com.zhisu.hotel.pricing.service.PriceQuoteService;
import com.zhisu.hotel.pricing.vo.DailyPriceItemVO;
import com.zhisu.hotel.pricing.vo.PriceQuoteVO;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.service.RoomTypeQueryService;
import com.zhisu.hotel.roomtype.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceQuoteServiceImpl implements PriceQuoteService {

    private final BookingDatePolicyService bookingDatePolicyService;
    private final DailyPriceService dailyPriceService;
    private final RoomTypeService roomTypeService;
    private final RoomTypeQueryService roomTypeQueryService;

    @Override
    public PriceQuoteVO quotePrice(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, Integer roomCount) {
        if (roomTypeId == null) {
            throw new BusinessException("ROOM_TYPE_ID_EMPTY", "Room type id cannot be null");
        }
        if (roomCount == null || roomCount <= 0) {
            throw new BusinessException("ROOM_COUNT_INVALID", "Room count must be greater than 0");
        }

        bookingDatePolicyService.validateBookingDates(checkInDate, checkOutDate);

        RoomType roomType = roomTypeService.getRoomTypeDetail(roomTypeId);
        if (roomType == null) {
            throw new BusinessException("ROOM_TYPE_NOT_FOUND", "Room type not found");
        }
        if (!roomTypeQueryService.isRoomTypeAvailable(roomTypeId)) {
            throw new BusinessException("ROOM_TYPE_OFFLINE", "Room type is not available for sale");
        }
        if (!roomTypeQueryService.hasAvailableInventory(roomTypeId, checkInDate, checkOutDate, roomCount)) {
            throw new BusinessException("ROOM_INVENTORY_INSUFFICIENT", "Insufficient inventory for selected stay dates");
        }

        LocalDate lastStayDate = checkOutDate.minusDays(1);
        dailyPriceService.ensurePricesExist(roomTypeId, checkInDate, lastStayDate);

        List<DailyPrice> dailyPrices = dailyPriceService.listDailyPrices(roomTypeId, checkInDate, lastStayDate);
        int expectedNights = Math.toIntExact(ChronoUnit.DAYS.between(checkInDate, checkOutDate));
        if (dailyPrices.size() != expectedNights) {
            throw new BusinessException("DAILY_PRICE_INCOMPLETE", "Daily price data is incomplete for selected stay dates");
        }

        List<DailyPriceItemVO> items = dailyPrices.stream()
                .map(price -> {
                    BigDecimal dailyAmount = price.getFinalPrice()
                            .multiply(BigDecimal.valueOf(roomCount))
                            .setScale(2, RoundingMode.HALF_UP);
                    return DailyPriceItemVO.builder()
                            .stayDate(price.getBusinessDate())
                            .basePrice(price.getBasePrice())
                            .coefficient(price.getDateCoefficient())
                            .unitPrice(price.getFinalPrice())
                            .roomCount(roomCount)
                            .dailyAmount(dailyAmount)
                            .priceSource(price.getPriceSource())
                            .priceDescription(price.getPriceDescription())
                            .build();
                })
                .toList();

        BigDecimal oneRoomAmount = dailyPrices.stream()
                .map(DailyPrice::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = oneRoomAmount
                .multiply(BigDecimal.valueOf(roomCount))
                .setScale(2, RoundingMode.HALF_UP);

        return PriceQuoteVO.builder()
                .roomTypeId(roomTypeId)
                .roomTypeName(roomType.getName())
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .stayNights(expectedNights)
                .roomCount(roomCount)
                .oneRoomAmount(oneRoomAmount)
                .totalAmount(totalAmount)
                .dailyPrices(items)
                .build();
    }
}