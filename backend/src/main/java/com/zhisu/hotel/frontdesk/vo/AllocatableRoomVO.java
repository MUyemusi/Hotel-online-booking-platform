package com.zhisu.hotel.frontdesk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocatableRoomVO {

    private Long roomId;
    private String roomNo;
    private Integer floor;
    private Long roomTypeId;
    private String roomTypeName;
    private String roomStatus;
    private String roomStatusText;
    private String remark;
}
