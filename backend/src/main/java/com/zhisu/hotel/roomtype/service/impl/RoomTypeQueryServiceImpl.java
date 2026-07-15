package com.zhisu.hotel.roomtype.service.impl;

import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import com.zhisu.hotel.roomtype.service.RoomTypeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RoomTypeQueryServiceImpl implements RoomTypeQueryService {

    private final RoomTypeMapper roomTypeMapper;
    private final InventoryService inventoryService;

    @Override
    public RoomTypeSummary getRoomTypeById(Long roomTypeId) {
        RoomType roomType = roomTypeMapper.selectById(roomTypeId);
        if (roomType == null) {
            return null;
        }
        return RoomTypeSummary.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .category(roomType.getCategory())
                .basePrice(roomType.getBasePrice())
                .area(roomType.getArea())
                .bedType(roomType.getBedType())
                .bedCount(roomType.getBedCount())
                .maxGuests(roomType.getMaxGuests())
                .hasBreakfast(roomType.getHasBreakfast())
                .hasWindow(roomType.getHasWindow())
                .hasWifi(roomType.getHasWifi())
                .hasAc(roomType.getHasAc())
                .allowPets(roomType.getAllowPets())
                .description(roomType.getDescription())
                .status(roomType.getStatus())
                .build();
    }

    @Override
    public boolean isRoomTypeAvailable(Long roomTypeId) {
        RoomType roomType = roomTypeMapper.selectById(roomTypeId);
        return roomType != null
                && "ONLINE".equals(roomType.getStatus())
                && roomType.getIsDeleted() == 0;
    }

    @Override
    public boolean hasAvailableInventory(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count) {
        inventoryService.ensureInventoryExists(roomTypeId, checkIn, checkOut.minusDays(1));
        return inventoryService.checkInventory(roomTypeId, checkIn, checkOut, count);
    }
}