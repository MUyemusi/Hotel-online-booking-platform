package com.zhisu.hotel.content.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.content.dto.AnnouncementSaveRequest;
import com.zhisu.hotel.content.entity.Announcement;
import com.zhisu.hotel.content.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ApiResponse<List<Announcement>> getAnnouncements() {
        return ApiResponse.success(announcementService.getAdminAnnouncements());
    }

    @GetMapping("/{id}")
    public ApiResponse<Announcement> getAnnouncementDetail(@PathVariable Long id) {
        return ApiResponse.success(announcementService.getAdminAnnouncementDetail(id));
    }

    @PostMapping
    public ApiResponse<Announcement> createAnnouncement(@Valid @RequestBody AnnouncementSaveRequest request) {
        return ApiResponse.success(announcementService.createAnnouncement(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementSaveRequest request) {
        return ApiResponse.success(announcementService.updateAnnouncement(id, request));
    }

    @PatchMapping("/{id}/publish")
    public ApiResponse<Announcement> publishAnnouncement(@PathVariable Long id) {
        return ApiResponse.success(announcementService.publishAnnouncement(id));
    }

    @PatchMapping("/{id}/offline")
    public ApiResponse<Announcement> offlineAnnouncement(@PathVariable Long id) {
        return ApiResponse.success(announcementService.offlineAnnouncement(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ApiResponse.success();
    }
}
