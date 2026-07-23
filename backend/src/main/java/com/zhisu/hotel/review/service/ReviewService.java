package com.zhisu.hotel.review.service;

import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.review.dto.CreateReviewRequest;
import com.zhisu.hotel.review.vo.ReviewVO;

public interface ReviewService {

    PageResponse<ReviewVO> getReviewsByRoomType(Long roomTypeId, long current, long size);

    ReviewVO createReview(CreateReviewRequest request, Long userId, String nickname);
}
