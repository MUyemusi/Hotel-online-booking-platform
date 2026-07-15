package com.zhisu.hotel.room.vo;

import com.zhisu.hotel.common.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusLogVO {

    private Long id;

    private Long roomId;

    private String roomNo;

    private RoomStatus beforeStatus;

    private String beforeStatusText;

    private RoomStatus afterStatus;

    private String afterStatusText;

    private Long operatorId;

    private String operatorType;

    private String reason;

    private Long relatedOrderId;

    private LocalDateTime createdAt;
}
