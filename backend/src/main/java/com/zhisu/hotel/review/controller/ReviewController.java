package com.zhisu.hotel.review.controller;

import com.zhisu.hotel.auth.security.CurrentUserProvider;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.review.dto.CreateReviewRequest;
import com.zhisu.hotel.review.service.ReviewService;
import com.zhisu.hotel.review.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/room-types/{roomTypeId}/reviews")
    public ApiResponse<PageResponse<ReviewVO>> listReviews(
            @PathVariable Long roomTypeId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.success(reviewService.getReviewsByRoomType(roomTypeId, current, size));
    }

    @PostMapping("/api/reviews")
    public ApiResponse<ReviewVO> createReview(@Valid @RequestBody CreateReviewRequest request) {
        var user = CurrentUserProvider.require();
        return ApiResponse.success(reviewService.createReview(request, user.id(), user.username()));
    }
}
