package com.zhisu.hotel.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.content.dto.AnnouncementSaveRequest;
import com.zhisu.hotel.content.entity.Announcement;
import com.zhisu.hotel.content.mapper.AnnouncementMapper;
import com.zhisu.hotel.content.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_OFFLINE = "OFFLINE";

    private final AnnouncementMapper announcementMapper;

    @Override
    public List<Announcement> getAdminAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getUpdatedAt)
                .orderByDesc(Announcement::getId);
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public Announcement getAdminAnnouncementDetail(Long id) {
        return requireAnnouncement(id);
    }

    @Override
    @Transactional
    public Announcement createAnnouncement(AnnouncementSaveRequest request) {
        validateRequest(request);

        Announcement announcement = new Announcement();
        fillAnnouncement(announcement, request);
        announcement.setStatus(STATUS_DRAFT);
        announcement.setPublishTime(null);
        announcement.setOfflineTime(null);
        announcementMapper.insert(announcement);

        return announcementMapper.selectById(announcement.getId());
    }

    @Override
    @Transactional
    public Announcement updateAnnouncement(Long id, AnnouncementSaveRequest request) {
        validateRequest(request);

        Announcement announcement = requireAnnouncement(id);
        fillAnnouncement(announcement, request);
        announcementMapper.updateById(announcement);

        return announcementMapper.selectById(id);
    }

    @Override
    @Transactional
    public Announcement publishAnnouncement(Long id) {
        Announcement announcement = requireAnnouncement(id);
        if (STATUS_PUBLISHED.equals(announcement.getStatus())) {
            return announcement;
        }

        announcement.setStatus(STATUS_PUBLISHED);
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setOfflineTime(null);
        announcementMapper.updateById(announcement);

        return announcementMapper.selectById(id);
    }

    @Override
    @Transactional
    public Announcement offlineAnnouncement(Long id) {
        Announcement announcement = requireAnnouncement(id);
        if (STATUS_OFFLINE.equals(announcement.getStatus())) {
            return announcement;
        }

        announcement.setStatus(STATUS_OFFLINE);
        announcement.setOfflineTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);

        return announcementMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement announcement = requireAnnouncement(id);
        if (STATUS_PUBLISHED.equals(announcement.getStatus())) {
            throw new BusinessException("ANNOUNCEMENT_DELETE_FORBIDDEN", "请先下架公告后再删除");
        }
        announcementMapper.deleteById(id);
    }

    @Override
    public List<Announcement> getPublicAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, STATUS_PUBLISHED)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getId);
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public Announcement getPublicAnnouncementDetail(Long id) {
        Announcement announcement = requireAnnouncement(id);
        if (!STATUS_PUBLISHED.equals(announcement.getStatus())) {
            throw new BusinessException("ANNOUNCEMENT_NOT_FOUND", "公告不存在");
        }
        return announcement;
    }

    private void fillAnnouncement(Announcement announcement, AnnouncementSaveRequest request) {
        BeanUtils.copyProperties(request, announcement, "top");
        announcement.setIsTop(Boolean.TRUE.equals(request.getTop()) ? 1 : 0);
    }

    private void validateRequest(AnnouncementSaveRequest request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException("ANNOUNCEMENT_TITLE_REQUIRED", "公告标题不能为空");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException("ANNOUNCEMENT_CONTENT_REQUIRED", "公告内容不能为空");
        }
        if (request.getTop() == null) {
            throw new BusinessException("ANNOUNCEMENT_TOP_REQUIRED", "是否置顶不能为空");
        }
    }

    private Announcement requireAnnouncement(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("ANNOUNCEMENT_NOT_FOUND", "公告不存在");
        }
        return announcement;
    }
}
