package com.zhisu.hotel.roomtype.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.entity.RoomTypeFacility;
import com.zhisu.hotel.roomtype.entity.RoomTypeImage;
import com.zhisu.hotel.roomtype.mapper.RoomTypeFacilityMapper;
import com.zhisu.hotel.roomtype.mapper.RoomTypeImageMapper;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import com.zhisu.hotel.roomtype.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeMapper roomTypeMapper;
    private final RoomTypeImageMapper roomTypeImageMapper;
    private final RoomTypeFacilityMapper roomTypeFacilityMapper;

    @Override
    @Transactional
    public RoomType createRoomType(RoomType roomType) {
        roomTypeMapper.insert(roomType);
        return roomType;
    }

    @Override
    @Transactional
    public RoomType updateRoomType(RoomType roomType) {
        roomTypeMapper.updateById(roomType);
        return roomType;
    }

    @Override
    @Transactional
    public void deleteRoomType(Long id) {
        // 逻辑删除，MyBatis-Plus @TableLogic 自动处理
        RoomType roomType = roomTypeMapper.selectById(id);
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }
        roomTypeMapper.deleteById(id);
    }

    @Override
    public RoomType getRoomTypeDetail(Long id) {
        return roomTypeMapper.selectById(id);
    }

    @Override
    public Page<RoomTypeSummary> pageRoomTypes(int page, int size, String keyword, String category, String status) {
        LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(RoomType::getName, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(RoomType::getCategory, category);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(RoomType::getStatus, status);
        }
        wrapper.orderByAsc(RoomType::getId);

        Page<RoomType> entityPage = roomTypeMapper.selectPage(new Page<>(page, size), wrapper);

        Page<RoomTypeSummary> result = new Page<>(page, size, entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(this::toSummary).toList());
        return result;
    }

    @Override
    @Transactional
    public void onlineRoomType(Long id) {
        RoomType roomType = roomTypeMapper.selectById(id);
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }
        roomType.setStatus("ONLINE");
        roomTypeMapper.updateById(roomType);
    }

    @Override
    @Transactional
    public void offlineRoomType(Long id) {
        RoomType roomType = roomTypeMapper.selectById(id);
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }
        roomType.setStatus("OFFLINE");
        roomTypeMapper.updateById(roomType);
    }

    @Override
    @Transactional
    public RoomTypeImage addImage(Long roomTypeId, String imageUrl, String imageType, int sortOrder) {
        RoomTypeImage image = RoomTypeImage.builder()
                .roomTypeId(roomTypeId)
                .imageUrl(imageUrl)
                .imageType(imageType)
                .sortOrder(sortOrder)
                .build();
        roomTypeImageMapper.insert(image);
        return image;
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        roomTypeImageMapper.deleteById(imageId);
    }

    @Override
    public List<RoomTypeImage> getImagesByRoomType(Long roomTypeId) {
        LambdaQueryWrapper<RoomTypeImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomTypeImage::getRoomTypeId, roomTypeId)
                .orderByAsc(RoomTypeImage::getSortOrder);
        return roomTypeImageMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void setMainImage(Long imageId) {
        RoomTypeImage image = roomTypeImageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException("图片不存在");
        }
        // 将同一房型的其他主图改为详情图
        LambdaQueryWrapper<RoomTypeImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomTypeImage::getRoomTypeId, image.getRoomTypeId())
                .eq(RoomTypeImage::getImageType, "MAIN");
        List<RoomTypeImage> mainImages = roomTypeImageMapper.selectList(wrapper);
        for (RoomTypeImage img : mainImages) {
            img.setImageType("DETAIL");
            roomTypeImageMapper.updateById(img);
        }
        // 设置新主图
        image.setImageType("MAIN");
        roomTypeImageMapper.updateById(image);
    }

    @Override
    @Transactional
    public RoomTypeFacility addFacility(Long roomTypeId, String facilityName, String facilityIcon) {
        RoomTypeFacility facility = RoomTypeFacility.builder()
                .roomTypeId(roomTypeId)
                .facilityName(facilityName)
                .facilityIcon(facilityIcon)
                .build();
        roomTypeFacilityMapper.insert(facility);
        return facility;
    }

    @Override
    @Transactional
    public void deleteFacility(Long facilityId) {
        roomTypeFacilityMapper.deleteById(facilityId);
    }

    @Override
    public List<RoomTypeFacility> getFacilitiesByRoomType(Long roomTypeId) {
        LambdaQueryWrapper<RoomTypeFacility> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomTypeFacility::getRoomTypeId, roomTypeId);
        return roomTypeFacilityMapper.selectList(wrapper);
    }

    private RoomTypeSummary toSummary(RoomType roomType) {
        return RoomTypeSummary.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .category(roomType.getCategory())
                .basePrice(roomType.getBasePrice())
                .area(roomType.getArea())
                .bedType(roomType.getBedType())
                .bedCount(roomType.getBedCount())
                .maxGuests(roomType.getMaxGuests())
                .hasBreakfast(roomType.getHasBreakfast())
                .hasWindow(roomType.getHasWindow())
                .hasWifi(roomType.getHasWifi())
                .hasAc(roomType.getHasAc())
                .allowPets(roomType.getAllowPets())
                .description(roomType.getDescription())
                .status(roomType.getStatus())
                .build();
    }
}
