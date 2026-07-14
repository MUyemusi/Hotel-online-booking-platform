package com.zhisu.hotel.content.service;

import com.zhisu.hotel.content.dto.HotelInfoUpdateRequest;
import com.zhisu.hotel.content.entity.HotelInfo;

public interface HotelInfoService {

    /**
     * 查询当前酒店信息。
     */
    HotelInfo getHotelInfo();

    /**
     * 修改当前酒店信息。
     */
    HotelInfo updateHotelInfo(HotelInfoUpdateRequest request);
}
