
package com.zhisu.hotel.roomtype.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.booking.service.StayDateRange;
import com.zhisu.hotel.common.enums.StayType;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.pricing.service.PriceQuoteService;
import com.zhisu.hotel.pricing.vo.PriceQuoteVO;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.entity.RoomTypeFacility;
import com.zhisu.hotel.roomtype.entity.RoomTypeImage;
import com.zhisu.hotel.roomtype.mapper.RoomTypeFacilityMapper;
import com.zhisu.hotel.roomtype.mapper.RoomTypeImageMapper;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import com.zhisu.hotel.roomtype.vo.PublicRoomTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicRoomTypeService {
    private final RoomTypeMapper roomTypeMapper;
    private final RoomTypeImageMapper imageMapper;
    private final RoomTypeFacilityMapper facilityMapper;
    private final InventoryService inventoryService;
    private final PriceQuoteService priceQuoteService;
    private final BookingDatePolicyService bookingDatePolicyService;

    public List<PublicRoomTypeVO> search(LocalDate checkIn, LocalDate checkOut, int roomCount, int guests, String stayType) {
        StayDateRange stay = validateSearch(checkIn, checkOut, roomCount, guests, stayType);
        List<RoomType> roomTypes = roomTypeMapper.selectList(
                new LambdaQueryWrapper<RoomType>().eq(RoomType::getStatus, "ONLINE").orderByAsc(RoomType::getBasePrice));
        if (roomTypes.isEmpty()) return List.of();
        List<Long> ids = roomTypes.stream().map(RoomType::getId).toList();
        Map<Long, List<RoomTypeImage>> images = imageMapper.selectList(
                        new LambdaQueryWrapper<RoomTypeImage>().in(RoomTypeImage::getRoomTypeId, ids)
                                .orderByAsc(RoomTypeImage::getSortOrder))
                .stream().collect(Collectors.groupingBy(RoomTypeImage::getRoomTypeId));
        Map<Long, List<RoomTypeFacility>> facilities = facilityMapper.selectList(
                        new LambdaQueryWrapper<RoomTypeFacility>().in(RoomTypeFacility::getRoomTypeId, ids))
                .stream().collect(Collectors.groupingBy(RoomTypeFacility::getRoomTypeId));

        return roomTypes.stream()
                .filter(roomType -> roomType.getMaxGuests() != null
                        && roomType.getMaxGuests() * roomCount >= guests)
                .map(roomType -> {
            List<?> inventoryRows = inventoryService.getAvailableInventory(roomType.getId(), stay.inventoryStartDate(), stay.lastInventoryDate());
            if (inventoryRows.isEmpty() && stay.stayType() == StayType.EARLY_MORNING) return null;
            if (inventoryRows.isEmpty()) {
                inventoryService.ensureInventoryExists(roomType.getId(), stay.inventoryStartDate(), stay.lastInventoryDate());
            }
            int available = inventoryService.getMinAvailableCount(roomType.getId(), stay.inventoryStartDate(), stay.lastInventoryDate());
            if (available < roomCount) return null;
            PriceQuoteVO quote = priceQuoteService.quotePrice(roomType.getId(), checkIn, checkOut, roomCount, stay.stayType().name());
            String mainImage = images.getOrDefault(roomType.getId(), List.of()).stream()
                    .filter(image -> "MAIN".equals(image.getImageType())).findFirst()
                    .or(() -> images.getOrDefault(roomType.getId(), List.of()).stream().findFirst())
                    .map(RoomTypeImage::getImageUrl).orElse(null);
            return PublicRoomTypeVO.builder()
                    .id(roomType.getId()).name(roomType.getName()).category(roomType.getCategory())
                    .basePrice(roomType.getBasePrice()).area(roomType.getArea()).bedType(roomType.getBedType())
                    .bedCount(roomType.getBedCount()).maxGuests(roomType.getMaxGuests())
                    .hasBreakfast(roomType.getHasBreakfast()).hasWindow(roomType.getHasWindow())
                    .description(roomType.getDescription()).mainImageUrl(mainImage)
                    .facilities(facilities.getOrDefault(roomType.getId(), List.of()).stream()
                            .map(RoomTypeFacility::getFacilityName).toList())
                    .availableRooms(available).totalAmount(quote.getTotalAmount()).dailyPrices(quote.getDailyPrices())
                    .stayType(stay.stayType().name())
                    .build();
        }).filter(Objects::nonNull).filter(item -> item.getAvailableRooms() >= roomCount).toList();
    }

    private StayDateRange validateSearch(LocalDate checkIn, LocalDate checkOut, int roomCount, int guests, String stayType) {
        if (roomCount < 1 || roomCount > 5 || guests < 1 || guests > 20) {
            throw new BusinessException("INVALID_REQUEST", "房间数或入住人数超出允许范围");
        }
        return bookingDatePolicyService.resolveStay(checkIn, checkOut, stayType);
    }
}
