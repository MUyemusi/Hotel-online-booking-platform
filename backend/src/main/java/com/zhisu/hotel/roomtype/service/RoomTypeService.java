package com.zhisu.hotel.roomtype.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.entity.RoomTypeFacility;
import com.zhisu.hotel.roomtype.entity.RoomTypeImage;

import java.util.List;

public interface RoomTypeService {

    // 房型CRUD
    RoomType createRoomType(RoomType roomType);
    RoomType updateRoomType(RoomType roomType);
    void deleteRoomType(Long id);
    RoomType getRoomTypeDetail(Long id);
    Page<RoomTypeSummary> pageRoomTypes(int page, int size, String keyword, String category, String status);

    // 上下架
    void onlineRoomType(Long id);
    void offlineRoomType(Long id);

    // 图片管理
    RoomTypeImage addImage(Long roomTypeId, String imageUrl, String imageType, int sortOrder);
    void deleteImage(Long imageId);
    List<RoomTypeImage> getImagesByRoomType(Long roomTypeId);
    void setMainImage(Long imageId);

    // 设施管理
    RoomTypeFacility addFacility(Long roomTypeId, String facilityName, String facilityIcon);
    void deleteFacility(Long facilityId);
    List<RoomTypeFacility> getFacilitiesByRoomType(Long roomTypeId);
}
