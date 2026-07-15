package com.zhisu.hotel.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.inventory.entity.DailyInventory;
import com.zhisu.hotel.inventory.entity.InventoryLog;
import com.zhisu.hotel.inventory.mapper.DailyInventoryMapper;
import com.zhisu.hotel.inventory.mapper.InventoryLogMapper;
import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final int DEFAULT_TOTAL_ROOMS = 10;

    private final DailyInventoryMapper dailyInventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final BookingDatePolicyService bookingDatePolicyService;
    private final SystemConfigService systemConfigService;

    @Override
    public boolean checkInventory(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count) {
        ensureInventoryExists(roomTypeId, checkIn, checkOut.minusDays(1));
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

    @Override
    @Transactional
    public boolean lockInventory(Long orderId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count) {
        ensureInventoryExists(roomTypeId, checkIn, checkOut.minusDays(1));
        LocalDate date = checkIn;
        while (date.isBefore(checkOut)) {
            LambdaQueryWrapper<DailyInventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DailyInventory::getRoomTypeId, roomTypeId)
                    .eq(DailyInventory::getInventoryDate, date);
            DailyInventory inventory = dailyInventoryMapper.selectOne(wrapper);
            if (inventory == null || inventory.getAvailableRooms() < count) {
                throw new RuntimeException("Inventory is insufficient");
            }
            int before = inventory.getAvailableRooms();
            inventory.setLockedRooms(inventory.getLockedRooms() + count);
            inventory.setAvailableRooms(inventory.getAvailableRooms() - count);
            dailyInventoryMapper.updateById(inventory);
            saveLog(roomTypeId, date, "LOCK", before, inventory.getAvailableRooms(), -count, orderId, "Lock inventory for order");
            date = date.plusDays(1);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean confirmInventory(Long orderId) {
        LambdaQueryWrapper<InventoryLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(InventoryLog::getOrderId, orderId)
                .eq(InventoryLog::getChangeType, "LOCK");
        List<InventoryLog> logs = inventoryLogMapper.selectList(logWrapper);
        for (InventoryLog log : logs) {
            LambdaQueryWrapper<DailyInventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DailyInventory::getRoomTypeId, log.getRoomTypeId())
                    .eq(DailyInventory::getInventoryDate, log.getInventoryDate());
            DailyInventory inventory = dailyInventoryMapper.selectOne(wrapper);
            if (inventory != null) {
                int before = inventory.getAvailableRooms();
                inventory.setLockedRooms(inventory.getLockedRooms() - Math.abs(log.getChangeAmount()));
                inventory.setBookedRooms(inventory.getBookedRooms() + Math.abs(log.getChangeAmount()));
                dailyInventoryMapper.updateById(inventory);
                saveLog(log.getRoomTypeId(), log.getInventoryDate(), "CONFIRM", before, inventory.getAvailableRooms(), 0, orderId, "Confirm booking inventory");
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean releaseInventory(Long orderId) {
        LambdaQueryWrapper<InventoryLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(InventoryLog::getOrderId, orderId)
                .in(InventoryLog::getChangeType, "LOCK", "CONFIRM");
        List<InventoryLog> logs = inventoryLogMapper.selectList(logWrapper);
        for (InventoryLog log : logs) {
            LambdaQueryWrapper<DailyInventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DailyInventory::getRoomTypeId, log.getRoomTypeId())
                    .eq(DailyInventory::getInventoryDate, log.getInventoryDate());
            DailyInventory inventory = dailyInventoryMapper.selectOne(wrapper);
            if (inventory != null) {
                int before = inventory.getAvailableRooms();
                int amount = Math.abs(log.getChangeAmount());
                inventory.setLockedRooms(Math.max(0, inventory.getLockedRooms() - amount));
                inventory.setBookedRooms(Math.max(0, inventory.getBookedRooms() - amount));
                inventory.setAvailableRooms(inventory.getAvailableRooms() + amount);
                dailyInventoryMapper.updateById(inventory);
                saveLog(log.getRoomTypeId(), log.getInventoryDate(), "RELEASE", before, inventory.getAvailableRooms(), amount, orderId, "Release inventory");
            }
        }
        return true;
    }

    @Override
    @Transactional
    public void generateDailyInventory(Long roomTypeId, int totalRooms, int days) {
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            ensureOneInventoryRecord(roomTypeId, date, totalRooms);
        }
    }

    @Override
    @Transactional
    public void generateAllDailyInventory(int days) {
        List<RoomType> roomTypes = roomTypeMapper.selectList(null);
        for (RoomType roomType : roomTypes) {
            generateDailyInventory(roomType.getId(), DEFAULT_TOTAL_ROOMS, days);
        }
    }

    @Override
    @Transactional
    public void ensureInventoryExists(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        if (roomTypeId == null || startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return;
        }
        int totalRooms = resolveTotalRooms(roomTypeId);
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            ensureOneInventoryRecord(roomTypeId, current, totalRooms);
            current = current.plusDays(1);
        }
    }

    @Override
    @Transactional
    public int ensureRollingWindowInventory() {
        LocalDate today = bookingDatePolicyService.getHotelToday();
        LocalDate latestCheckInDate = bookingDatePolicyService.getLatestCheckInDate();
        int maxStayNights = systemConfigService.getInteger("MAX_STAY_NIGHTS");
        LocalDate requiredEndDate = latestCheckInDate.plusDays(maxStayNights - 1L);
        List<RoomType> roomTypes = roomTypeMapper.selectList(null);
        for (RoomType roomType : roomTypes) {
            ensureInventoryExists(roomType.getId(), today, requiredEndDate);
        }
        return roomTypes.size() * (int) (ChronoUnit.DAYS.between(today, requiredEndDate) + 1);
    }

    @Override
    public List<DailyInventory> getAvailableInventory(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DailyInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyInventory::getRoomTypeId, roomTypeId)
                .between(DailyInventory::getInventoryDate, startDate, endDate)
                .orderByAsc(DailyInventory::getInventoryDate);
        return dailyInventoryMapper.selectList(wrapper);
    }

    @Override
    public int getMinAvailableCount(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        List<DailyInventory> list = getAvailableInventory(roomTypeId, startDate, endDate);
        return list.stream()
                .mapToInt(DailyInventory::getAvailableRooms)
                .min()
                .orElse(0);
    }

    private void ensureOneInventoryRecord(Long roomTypeId, LocalDate date, int totalRooms) {
        LambdaQueryWrapper<DailyInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyInventory::getRoomTypeId, roomTypeId)
                .eq(DailyInventory::getInventoryDate, date);
        DailyInventory existing = dailyInventoryMapper.selectOne(wrapper);
        if (existing != null) {
            return;
        }

        DailyInventory inventory = DailyInventory.builder()
                .roomTypeId(roomTypeId)
                .inventoryDate(date)
                .totalRooms(totalRooms)
                .lockedRooms(0)
                .bookedRooms(0)
                .availableRooms(totalRooms)
                .status("ACTIVE")
                .build();
        try {
            dailyInventoryMapper.insert(inventory);
            saveLog(roomTypeId, date, "SYSTEM_INIT", 0, totalRooms, totalRooms, null, "Initialize rolling inventory");
        } catch (DuplicateKeyException ignored) {
        }
    }

    private int resolveTotalRooms(Long roomTypeId) {
        DailyInventory latestInventory = dailyInventoryMapper.selectOne(
                new LambdaQueryWrapper<DailyInventory>()
                        .eq(DailyInventory::getRoomTypeId, roomTypeId)
                        .orderByDesc(DailyInventory::getInventoryDate)
                        .last("LIMIT 1")
        );
        if (latestInventory != null && latestInventory.getTotalRooms() != null && latestInventory.getTotalRooms() > 0) {
            return latestInventory.getTotalRooms();
        }
        return DEFAULT_TOTAL_ROOMS;
    }

    private void saveLog(Long roomTypeId, LocalDate date, String changeType, int before, int after, int amount, Long orderId, String reason) {
        InventoryLog log = InventoryLog.builder()
                .roomTypeId(roomTypeId)
                .inventoryDate(date)
                .changeType(changeType)
                .beforeValue(before)
                .afterValue(after)
                .changeAmount(amount)
                .operator("system")
                .changeReason(reason)
                .orderId(orderId)
                .build();
        inventoryLogMapper.insert(log);
    }
}