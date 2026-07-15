package com.zhisu.hotel.content.service.impl;

import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.content.dto.HotelInfoUpdateRequest;
import com.zhisu.hotel.content.entity.HotelInfo;
import com.zhisu.hotel.content.mapper.HotelInfoMapper;
import com.zhisu.hotel.content.service.HotelInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelInfoServiceImpl implements HotelInfoService {

    /**
     * 本项目为单酒店系统，统一维护 ID=1 的酒店信息。
     */
    private static final Long HOTEL_ID = 1L;

    private final HotelInfoMapper hotelInfoMapper;

    @Override
    public HotelInfo getHotelInfo() {
        HotelInfo hotelInfo = hotelInfoMapper.selectById(HOTEL_ID);

        if (hotelInfo == null) {
            throw new BusinessException(
                    "HOTEL_INFO_NOT_FOUND",
                    "酒店信息不存在"
            );
        }

        return hotelInfo;
    }

    @Override
    @Transactional
    public HotelInfo updateHotelInfo(HotelInfoUpdateRequest request) {
        HotelInfo hotelInfo = hotelInfoMapper.selectById(HOTEL_ID);

        if (hotelInfo == null) {
            throw new BusinessException(
                    "HOTEL_INFO_NOT_FOUND",
                    "酒店信息不存在"
            );
        }

        BeanUtils.copyProperties(request, hotelInfo);
        hotelInfoMapper.updateById(hotelInfo);

        return hotelInfoMapper.selectById(HOTEL_ID);
    }
}
