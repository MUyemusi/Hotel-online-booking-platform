package com.zhisu.hotel.content.service;

import com.zhisu.hotel.content.dto.AnnouncementSaveRequest;
import com.zhisu.hotel.content.entity.Announcement;

import java.util.List;

public interface AnnouncementService {

    List<Announcement> getAdminAnnouncements();

    Announcement getAdminAnnouncementDetail(Long id);

    Announcement createAnnouncement(AnnouncementSaveRequest request);

    Announcement updateAnnouncement(Long id, AnnouncementSaveRequest request);

    Announcement publishAnnouncement(Long id);

    Announcement offlineAnnouncement(Long id);

    void deleteAnnouncement(Long id);

    List<Announcement> getPublicAnnouncements();

    Announcement getPublicAnnouncementDetail(Long id);
}
