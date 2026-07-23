
package com.zhisu.hotel.pricing.service.impl;

import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.booking.service.StayDateRange;
import com.zhisu.hotel.common.enums.StayType;
import com.zhisu.hotel.inventory.service.InventoryService;
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
    private final InventoryService inventoryService;

    @Override
    public PriceQuoteVO quotePrice(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, Integer roomCount, String stayType) {
        if (roomTypeId == null) {
            throw new BusinessException("ROOM_TYPE_ID_EMPTY", "房型ID不能为空");
        }
        if (roomCount == null || roomCount <= 0) {
            throw new BusinessException("ROOM_COUNT_INVALID", "房间数必须大于0");
        }

        StayDateRange stay = bookingDatePolicyService.resolveStay(checkInDate, checkOutDate, stayType);

        RoomType roomType = roomTypeService.getRoomTypeDetail(roomTypeId);
        if (roomType == null) {
            throw new BusinessException("ROOM_TYPE_NOT_FOUND", "房型不存在");
        }
        if (!roomTypeQueryService.isRoomTypeAvailable(roomTypeId)) {
            throw new BusinessException("ROOM_TYPE_OFFLINE", "房型已下架或不可售");
        }
        if (stay.stayType() == StayType.EARLY_MORNING
                && inventoryService.getAvailableInventory(roomTypeId, stay.inventoryStartDate(), stay.lastInventoryDate()).isEmpty()) {
            throw new BusinessException("ROOM_INVENTORY_INSUFFICIENT", "前一晚没有可用库存，无法预订凌晨入住");
        }
        if (!inventoryService.checkInventory(roomTypeId, stay.inventoryStartDate(), stay.inventoryEndExclusive(), roomCount)) {
            throw new BusinessException("ROOM_INVENTORY_INSUFFICIENT", "所选住宿日期库存不足");
        }

        dailyPriceService.ensurePricesExist(roomTypeId, stay.inventoryStartDate(), stay.lastInventoryDate());
        List<DailyPrice> dailyPrices = dailyPriceService.listDailyPrices(roomTypeId, stay.inventoryStartDate(), stay.lastInventoryDate());
        int expectedNights = stay.stayNights();
        if (dailyPrices.size() != expectedNights) {
            throw new BusinessException("DAILY_PRICE_INCOMPLETE", "所选住宿日期的每日价格数据不完整");
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
                .stayType(stay.stayType().name())
                .stayNights(expectedNights)
                .roomCount(roomCount)
                .oneRoomAmount(oneRoomAmount)
                .totalAmount(totalAmount)
                .dailyPrices(items)
                .build();
    }
}
