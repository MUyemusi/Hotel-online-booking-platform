package com.zhisu.hotel.content.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.content.entity.Announcement;
import com.zhisu.hotel.content.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/announcements")
@RequiredArgsConstructor
public class PublicAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ApiResponse<List<Announcement>> getAnnouncements() {
        return ApiResponse.success(announcementService.getPublicAnnouncements());
    }

    @GetMapping("/{id}")
    public ApiResponse<Announcement> getAnnouncementDetail(@PathVariable Long id) {
        return ApiResponse.success(announcementService.getPublicAnnouncementDetail(id));
    }
}
