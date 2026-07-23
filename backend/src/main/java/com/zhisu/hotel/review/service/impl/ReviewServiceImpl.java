package com.zhisu.hotel.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.review.dto.CreateReviewRequest;
import com.zhisu.hotel.review.entity.Review;
import com.zhisu.hotel.review.mapper.ReviewMapper;
import com.zhisu.hotel.review.service.ReviewService;
import com.zhisu.hotel.review.vo.ReviewVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    @Override
    public PageResponse<ReviewVO> getReviewsByRoomType(Long roomTypeId, long current, long size) {
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRoomTypeId, roomTypeId)
                .eq(Review::getStatus, "APPROVED")
                .orderByDesc(Review::getCreatedAt)
                .orderByDesc(Review::getId);

        Page<Review> page = reviewMapper.selectPage(new Page<>(safeCurrent, safeSize), wrapper);

        List<ReviewVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResponse.<ReviewVO>builder()
                .records(records)
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    @Transactional
    public ReviewVO createReview(CreateReviewRequest request, Long userId, String nickname) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        Review entity = Review.builder()
                .roomTypeId(request.getRoomTypeId())
                .userId(userId)
                .nickname(nickname != null && !nickname.isBlank() ? nickname : "住客")
                .avatar(null)
                .content(request.getContent().trim())
                .status("APPROVED")
                .build();
        if (reviewMapper.insert(entity) != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "评价发布失败");
        }
        return toVO(entity);
    }

    private ReviewVO toVO(Review entity) {
        return ReviewVO.builder()
                .id(entity.getId())
                .roomTypeId(entity.getRoomTypeId())
                .userId(entity.getUserId())
                .nickname(entity.getNickname())
                .avatar(entity.getAvatar())
                .content(entity.getContent())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
