
package com.zhisu.hotel.pricing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.pricing.dto.BasePriceUpdateRequest;
import com.zhisu.hotel.pricing.entity.PriceOperationLog;
import com.zhisu.hotel.pricing.mapper.PriceOperationLogMapper;
import com.zhisu.hotel.pricing.service.BasePriceService;
import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BasePriceServiceImpl implements BasePriceService {

    private final RoomTypeService roomTypeService;
    private final PriceOperationLogMapper priceOperationLogMapper;

    @Override
    public Page<RoomTypeSummary> pageBasePrices(int page, int size, String keyword, String status) {
        return roomTypeService.pageRoomTypes(page, size, keyword, null, status, null, null, null);
    }

    @Override
    @Transactional
    public RoomType updateBasePrice(Long roomTypeId, BasePriceUpdateRequest request) {
        RoomType roomType = roomTypeService.getRoomTypeDetail(roomTypeId);
        if (roomType == null) {
            throw new BusinessException("ROOM_TYPE_NOT_FOUND", "房型不存在");
        }

        BigDecimal oldPrice = roomType.getBasePrice();
        BigDecimal newPrice = request.getBasePrice().setScale(2, RoundingMode.HALF_UP);
        if (oldPrice != null && oldPrice.compareTo(newPrice) == 0) {
            return roomType;
        }

        roomType.setBasePrice(newPrice);
        RoomType updatedRoomType = roomTypeService.updateRoomType(roomType);

        PriceOperationLog log = new PriceOperationLog();
        log.setRoomTypeId(roomType.getId());
        log.setRoomTypeName(roomType.getName());
        log.setOldPrice(oldPrice == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : oldPrice.setScale(2, RoundingMode.HALF_UP));
        log.setNewPrice(newPrice);
        log.setChangeReason(request.getReason().trim());
        log.setOperatorId(null);
        log.setOperatorName(null);
        priceOperationLogMapper.insert(log);

        return updatedRoomType;
    }

    @Override
    public Page<PriceOperationLog> pagePriceLogs(int page, int size, Long roomTypeId) {
        LambdaQueryWrapper<PriceOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (roomTypeId != null) {
            wrapper.eq(PriceOperationLog::getRoomTypeId, roomTypeId);
        }
        wrapper.orderByDesc(PriceOperationLog::getCreatedAt)
                .orderByDesc(PriceOperationLog::getId);
        return priceOperationLogMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
