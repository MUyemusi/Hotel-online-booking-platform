package com.zhisu.hotel.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.content.dto.HotelBannerSaveRequest;
import com.zhisu.hotel.content.entity.HotelBanner;
import com.zhisu.hotel.content.mapper.HotelBannerMapper;
import com.zhisu.hotel.content.service.HotelBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelBannerServiceImpl implements HotelBannerService {

    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_OFFLINE = "OFFLINE";

    private final HotelBannerMapper hotelBannerMapper;

    @Override
    public List<HotelBanner> getAdminBanners() {
        LambdaQueryWrapper<HotelBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(HotelBanner::getSortNo)
                .orderByDesc(HotelBanner::getId);
        return hotelBannerMapper.selectList(wrapper);
    }

    @Override
    public List<HotelBanner> getPublicBanners() {
        LambdaQueryWrapper<HotelBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelBanner::getStatus, STATUS_PUBLISHED)
                .orderByAsc(HotelBanner::getSortNo)
                .orderByDesc(HotelBanner::getId);
        return hotelBannerMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public HotelBanner createBanner(HotelBannerSaveRequest request) {
        validateRequest(request);

        HotelBanner banner = new HotelBanner();
        BeanUtils.copyProperties(request, banner);
        banner.setLinkUrl(normalizeLinkUrl(request.getLinkUrl()));
        banner.setStatus(STATUS_OFFLINE);
        hotelBannerMapper.insert(banner);

        return hotelBannerMapper.selectById(banner.getId());
    }

    @Override
    @Transactional
    public HotelBanner updateBanner(Long id, HotelBannerSaveRequest request) {
        validateRequest(request);

        HotelBanner banner = requireBanner(id);
        BeanUtils.copyProperties(request, banner);
        banner.setLinkUrl(normalizeLinkUrl(request.getLinkUrl()));
        hotelBannerMapper.updateById(banner);

        return hotelBannerMapper.selectById(id);
    }

    @Override
    @Transactional
    public HotelBanner publishBanner(Long id) {
        HotelBanner banner = requireBanner(id);
        if (STATUS_PUBLISHED.equals(banner.getStatus())) {
            return banner;
        }

        banner.setStatus(STATUS_PUBLISHED);
        hotelBannerMapper.updateById(banner);
        return hotelBannerMapper.selectById(id);
    }

    @Override
    @Transactional
    public HotelBanner offlineBanner(Long id) {
        HotelBanner banner = requireBanner(id);
        if (STATUS_OFFLINE.equals(banner.getStatus())) {
            return banner;
        }

        banner.setStatus(STATUS_OFFLINE);
        hotelBannerMapper.updateById(banner);
        return hotelBannerMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        HotelBanner banner = requireBanner(id);
        if (STATUS_PUBLISHED.equals(banner.getStatus())) {
            throw new BusinessException("BANNER_DELETE_FORBIDDEN", "请先下架轮播图后再删除");
        }
        hotelBannerMapper.deleteById(id);
    }

    private void validateRequest(HotelBannerSaveRequest request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException("BANNER_TITLE_REQUIRED", "轮播图标题不能为空");
        }
        if (!StringUtils.hasText(request.getImageUrl())) {
            throw new BusinessException("BANNER_IMAGE_REQUIRED", "轮播图图片地址不能为空");
        }
        if (request.getSortNo() == null || request.getSortNo() < 0) {
            throw new BusinessException("BANNER_SORT_INVALID", "排序序号不能小于0");
        }
    }

    private String normalizeLinkUrl(String linkUrl) {
        return StringUtils.hasText(linkUrl) ? linkUrl.trim() : "";
    }

    private HotelBanner requireBanner(Long id) {
        HotelBanner banner = hotelBannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException("BANNER_NOT_FOUND", "轮播图不存在");
        }
        return banner;
    }
}
