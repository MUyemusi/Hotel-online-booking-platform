package com.zhisu.hotel.roomtype.service;

import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;

/**
 * 提供给其他模块调用的房型查询接口
 * 成员4的实体房间模块通过此接口获取房型信息
 */
public interface RoomTypeQueryService {

    /**
     * 根据房型ID查询房型摘要信息
     */
    RoomTypeSummary getRoomTypeById(Long roomTypeId);

    /**
     * 判断房型是否可售（上架且未删除）
     */
    boolean isRoomTypeAvailable(Long roomTypeId);

    /**
     * 判断指定日期范围内房型是否有足够可售库存
     */
    boolean hasAvailableInventory(Long roomTypeId, java.time.LocalDate checkIn, java.time.LocalDate checkOut, int count);
}
