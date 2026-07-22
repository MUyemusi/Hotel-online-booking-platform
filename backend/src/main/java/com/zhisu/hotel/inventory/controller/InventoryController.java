package com.zhisu.hotel.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.common.exception.BusinessException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                new LambdaQueryWrapper<DailyInventory>()
                        .eq(DailyInventory::getRoomTypeId, roomTypeId)
                        .between(DailyInventory::getInventoryDate, startDate, endDate)
                        .orderByAsc(DailyInventory::getInventoryDate));
        return ApiResponse.success(list);
    }

    /** 修改单条库存 — 校验 totalRooms 不能低于已占用数量 */
    @PutMapping("/{id}")
    public ApiResponse<DailyInventory> update(@PathVariable Long id, @RequestBody DailyInventory inventory) {
        DailyInventory existing = dailyInventoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("库存记录不存在");
        }
        int occupied = existing.getLockedRooms() + existing.getBookedRooms();
        if (inventory.getTotalRooms() != null && inventory.getTotalRooms() < occupied) {
            throw new BusinessException("总库存不能低于已占用数量（锁定 " + existing.getLockedRooms()
                    + " + 已预订 " + existing.getBookedRooms() + " = " + occupied + "）");
        }
        inventory.setId(id);
        if (inventory.getTotalRooms() != null) {
            inventory.setAvailableRooms(inventory.getTotalRooms() - existing.getLockedRooms() - existing.getBookedRooms());
        }
        dailyInventoryMapper.updateById(inventory);
        return ApiResponse.success(dailyInventoryMapper.selectById(id));
    }

    /** 按日期范围批量修改库存 */
    @PutMapping("/batch")
    public ApiResponse<Map<String, Object>> batchUpdate(@RequestBody BatchInventoryRequest request) {
        if (request.getRoomTypeId() == null || request.getStartDate() == null || request.getEndDate() == null) {
            throw new BusinessException("请指定房型和日期范围");
        }
        if (request.getTotalRooms() != null && request.getTotalRooms() < 0) {
            throw new BusinessException("总库存不能为负数");
        }

        List<DailyInventory> list = dailyInventoryMapper.selectList(
                new LambdaQueryWrapper<DailyInventory>()
                        .eq(DailyInventory::getRoomTypeId, request.getRoomTypeId())
                        .between(DailyInventory::getInventoryDate, request.getStartDate(), request.getEndDate()));

        int updated = 0;
        int skipped = 0;
        List<String> skipReasons = new ArrayList<>();

        for (DailyInventory inv : list) {
            if (request.getStatus() != null) {
                inv.setStatus(request.getStatus());
            }
            if (request.getTotalRooms() != null) {
                int occupied = inv.getLockedRooms() + inv.getBookedRooms();
                if (request.getTotalRooms() < occupied) {
                    skipped++;
                    skipReasons.add(inv.getInventoryDate() + "：总库存" + request.getTotalRooms()
                            + " 低于已占用" + occupied + "，已跳过");
                    continue;
                }
                inv.setTotalRooms(request.getTotalRooms());
                inv.setAvailableRooms(request.getTotalRooms() - occupied);
            }
            dailyInventoryMapper.updateById(inv);
            updated++;
        }

        return ApiResponse.success(Map.of(
                "updated", updated,
                "skipped", skipped,
                "skipReasons", skipReasons,
                "total", list.size()
        ));
    }

    /** 自动生成库存（按房型或全部房型） */
    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generate(
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(defaultValue = "15") int days,
            @RequestParam(defaultValue = "10") int totalRooms) {
        if (days < 1 || days > 90) {
            throw new BusinessException("生成天数必须在 1–90 之间");
        }
        if (totalRooms < 1 || totalRooms > 9999) {
            throw new BusinessException("总库存数必须在 1–9999 之间");
        }

        if (roomTypeId != null) {
            inventoryService.generateDailyInventory(roomTypeId, totalRooms, days);
            return ApiResponse.success(Map.of("generated", days, "roomTypeId", roomTypeId));
        } else {
            inventoryService.generateAllDailyInventory(days);
            return ApiResponse.success(Map.of("generated", days, "scope", "all"));
        }
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
                new LambdaQueryWrapper<InventoryLog>()
                        .eq(InventoryLog::getRoomTypeId, roomTypeId)
                        .between(InventoryLog::getInventoryDate, startDate, endDate)
                        .orderByDesc(InventoryLog::getCreatedAt));
        return ApiResponse.success(logs);
    }
}
