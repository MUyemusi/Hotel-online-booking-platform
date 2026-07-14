package com.zhisu.hotel.inventory.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.inventory.entity.DailyInventory;
import com.zhisu.hotel.inventory.entity.InventoryLog;
import com.zhisu.hotel.inventory.mapper.DailyInventoryMapper;
import com.zhisu.hotel.inventory.mapper.InventoryLogMapper;
import com.zhisu.hotel.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final DailyInventoryMapper dailyInventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final InventoryService inventoryService;

    @GetMapping
    public ApiResponse<List<DailyInventory>> getByDateRange(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyInventory> list = dailyInventoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DailyInventory>()
                        .eq(DailyInventory::getRoomTypeId, roomTypeId)
                        .between(DailyInventory::getInventoryDate, startDate, endDate)
                        .orderByAsc(DailyInventory::getInventoryDate));
        return ApiResponse.success(list);
    }

    @PutMapping("/{id}")
    public ApiResponse<DailyInventory> update(@PathVariable Long id, @RequestBody DailyInventory inventory) {
        inventory.setId(id);
        dailyInventoryMapper.updateById(inventory);
        return ApiResponse.success(dailyInventoryMapper.selectById(id));
    }

    @PutMapping("/{id}/pause")
    public ApiResponse<Void> pause(@PathVariable Long id) {
        DailyInventory inv = dailyInventoryMapper.selectById(id);
        if (inv != null) {
            inv.setStatus("PAUSED");
            dailyInventoryMapper.updateById(inv);
        }
        return ApiResponse.success();
    }

    @PutMapping("/{id}/resume")
    public ApiResponse<Void> resume(@PathVariable Long id) {
        DailyInventory inv = dailyInventoryMapper.selectById(id);
        if (inv != null) {
            inv.setStatus("ACTIVE");
            dailyInventoryMapper.updateById(inv);
        }
        return ApiResponse.success();
    }

    @GetMapping("/logs")
    public ApiResponse<List<InventoryLog>> getLogs(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<InventoryLog> logs = inventoryLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InventoryLog>()
                        .eq(InventoryLog::getRoomTypeId, roomTypeId)
                        .between(InventoryLog::getInventoryDate, startDate, endDate)
                        .orderByDesc(InventoryLog::getCreatedAt));
        return ApiResponse.success(logs);
    }
}
