package com.zhisu.hotel.content.service;

import com.zhisu.hotel.content.dto.HotelBannerSaveRequest;
import com.zhisu.hotel.content.entity.HotelBanner;

import java.util.List;

public interface HotelBannerService {

    List<HotelBanner> getAdminBanners();

    List<HotelBanner> getPublicBanners();

    HotelBanner createBanner(HotelBannerSaveRequest request);

    HotelBanner updateBanner(Long id, HotelBannerSaveRequest request);

    HotelBanner publishBanner(Long id);

    HotelBanner offlineBanner(Long id);

    void deleteBanner(Long id);
}
