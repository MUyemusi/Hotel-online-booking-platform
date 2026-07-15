package com.zhisu.hotel.pricing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.pricing.dto.BasePriceUpdateRequest;
import com.zhisu.hotel.pricing.entity.PriceOperationLog;
import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;
import com.zhisu.hotel.roomtype.entity.RoomType;

public interface BasePriceService {

    Page<RoomTypeSummary> pageBasePrices(int page, int size, String keyword, String status);

    RoomType updateBasePrice(Long roomTypeId, BasePriceUpdateRequest request);

    Page<PriceOperationLog> pagePriceLogs(int page, int size, Long roomTypeId);
}
