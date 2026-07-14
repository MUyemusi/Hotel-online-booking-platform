package com.zhisu.hotel.inventory.service;

import com.zhisu.hotel.inventory.entity.DailyInventory;

import java.time.LocalDate;
import java.util.List;

public interface InventoryService {

    /**
     * 检查指定日期范围内房型是否有足够可售库存
     */
    boolean checkInventory(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count);

    /**
     * 锁定库存（创建订单时调用）
     * 将可售库存转为锁定库存
     */
    boolean lockInventory(Long orderId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut, int count);

    /**
     * 确认预订（支付成功后调用）
     * 将锁定库存转为已预订库存
     */
    boolean confirmInventory(Long orderId);

    /**
     * 释放库存（取消订单或超时关闭时调用）
     * 将锁定或已预订库存释放回可售库存
     */
    boolean releaseInventory(Long orderId);

    /**
     * 为指定房型生成未来N天的初始库存
     */
    void generateDailyInventory(Long roomTypeId, int totalRooms, int days);

    /**
     * 批量生成所有房型未来N天库存
     */
    void generateAllDailyInventory(int days);

    /**
     * 查询指定日期范围的可售库存
     */
    List<DailyInventory> getAvailableInventory(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取日期范围内最低可售数量
     */
    int getMinAvailableCount(Long roomTypeId, LocalDate startDate, LocalDate endDate);
}
