package com.zhisu.hotel.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hotel_info")
public class HotelInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String hotelName;

    private String logoUrl;

    private String introduction;

    private String address;

    private String contactPhone;

    private String facilities;

    private String checkinNotice;

    private String cancellationPolicy;

    private String trafficInfo;

    private String faqContent;

    private Double latitude;

    private Double longitude;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
