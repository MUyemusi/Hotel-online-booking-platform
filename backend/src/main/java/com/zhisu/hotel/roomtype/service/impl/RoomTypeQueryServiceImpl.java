package com.zhisu.hotel.roomtype.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.inventory.entity.DailyInventory;
import com.zhisu.hotel.inventory.mapper.DailyInventoryMapper;
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
    private final DailyInventoryMapper dailyInventoryMapper;

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
        LocalDate date = checkIn;
        while (date.isBefore(checkOut)) {
            LambdaQueryWrapper<DailyInventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DailyInventory::getRoomTypeId, roomTypeId)
                    .eq(DailyInventory::getInventoryDate, date)
                    .eq(DailyInventory::getStatus, "ACTIVE");
            DailyInventory inventory = dailyInventoryMapper.selectOne(wrapper);
            if (inventory == null || inventory.getAvailableRooms() < count) {
                return false;
            }
            date = date.plusDays(1);
        }
        return true;
    }
}
