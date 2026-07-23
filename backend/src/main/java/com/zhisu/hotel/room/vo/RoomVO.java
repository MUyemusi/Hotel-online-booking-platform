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
public class RoomVO {

    private Long id;

    private String roomNo;

    private Integer floor;

    private Long roomTypeId;

    private String roomTypeName;

    private RoomStatus roomStatus;

    private String roomStatusText;

    private String maintenanceReason;

    private Boolean nearElevator;

    private Boolean headRoom;

    private Boolean tailRoom;

    private String cadImageUrl;

    private String remark;

    private LocalDateTime updatedAt;
}
