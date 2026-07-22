package com.zhisu.hotel.roomtype.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.roomtype.dto.RoomTypeSummary;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.entity.RoomTypeFacility;
import com.zhisu.hotel.roomtype.entity.RoomTypeImage;
import com.zhisu.hotel.roomtype.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @PostMapping
    public ApiResponse<RoomType> create(@Valid @RequestBody RoomType roomType) {
        return ApiResponse.success(roomTypeService.createRoomType(roomType));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoomType> update(@PathVariable Long id, @Valid @RequestBody RoomType roomType) {
        roomType.setId(id);
        return ApiResponse.success(roomTypeService.updateRoomType(roomType));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomType> getDetail(@PathVariable Long id) {
        return ApiResponse.success(roomTypeService.getRoomTypeDetail(id));
    }

    @GetMapping
    public ApiResponse<Page<RoomTypeSummary>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(roomTypeService.pageRoomTypes(page, size, keyword, category, status, null, null, null));
    }

    @PutMapping("/{id}/online")
    public ApiResponse<Void> online(@PathVariable Long id) {
        roomTypeService.onlineRoomType(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/offline")
    public ApiResponse<Void> offline(@PathVariable Long id) {
        roomTypeService.offlineRoomType(id);
        return ApiResponse.success();
    }

    // 图片管理
    @GetMapping("/{id}/images")
    public ApiResponse<List<RoomTypeImage>> getImages(@PathVariable Long id) {
        return ApiResponse.success(roomTypeService.getImagesByRoomType(id));
    }

    @PostMapping("/{id}/images")
    public ApiResponse<RoomTypeImage> addImage(@PathVariable Long id, @RequestBody RoomTypeImage image) {
        return ApiResponse.success(roomTypeService.addImage(id, image.getImageUrl(), image.getImageType(), image.getSortOrder()));
    }

    @DeleteMapping("/images/{imageId}")
    public ApiResponse<Void> deleteImage(@PathVariable Long imageId) {
        roomTypeService.deleteImage(imageId);
        return ApiResponse.success();
    }

    @PutMapping("/images/{imageId}/main")
    public ApiResponse<Void> setMainImage(@PathVariable Long imageId) {
        roomTypeService.setMainImage(imageId);
        return ApiResponse.success();
    }

    // 设施管理
    @GetMapping("/{id}/facilities")
    public ApiResponse<List<RoomTypeFacility>> getFacilities(@PathVariable Long id) {
        return ApiResponse.success(roomTypeService.getFacilitiesByRoomType(id));
    }

    @PostMapping("/{id}/facilities")
    public ApiResponse<RoomTypeFacility> addFacility(@PathVariable Long id, @RequestBody RoomTypeFacility facility) {
        return ApiResponse.success(roomTypeService.addFacility(id, facility.getFacilityName(), facility.getFacilityIcon()));
    }

    @DeleteMapping("/facilities/{facilityId}")
    public ApiResponse<Void> deleteFacility(@PathVariable Long facilityId) {
        roomTypeService.deleteFacility(facilityId);
        return ApiResponse.success();
    }
}
