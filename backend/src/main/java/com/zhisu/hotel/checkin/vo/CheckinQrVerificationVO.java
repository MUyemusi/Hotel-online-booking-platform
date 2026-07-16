package com.zhisu.hotel.checkin.vo;

import com.zhisu.hotel.frontdesk.vo.AllocatableRoomVO;
import com.zhisu.hotel.frontdesk.vo.CheckinCheckItemVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinQrVerificationVO {

    private Boolean valid;
    private Long tokenId;
    private String tokenStatus;
    private LocalDateTime expiresAt;
    private String failureCode;
    private String failureMessage;
    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private String paymentStatus;
    private String maskedContactPhone;
    private String contactName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long roomTypeId;
    private String roomTypeName;
    private Integer requiredRoomCount;
    private Integer availableRoomCount;
    private List<CheckinCheckItemVO> checkItems;
    private List<AllocatableRoomVO> allocatableRooms;
    private LocalDateTime verifiedAt;
}
