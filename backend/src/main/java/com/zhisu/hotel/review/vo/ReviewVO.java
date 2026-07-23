package com.zhisu.hotel.review.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO {

    private Long id;
    private Long roomTypeId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private String status;
    private LocalDateTime createdAt;
}
