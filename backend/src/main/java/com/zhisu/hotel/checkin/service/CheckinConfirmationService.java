package com.zhisu.hotel.checkin.service;

import com.zhisu.hotel.checkin.dto.ConfirmCheckinRequest;
import com.zhisu.hotel.checkin.vo.ConfirmCheckinVO;
import com.zhisu.hotel.checkin.vo.RoomAssignmentVO;

import java.util.List;

public interface CheckinConfirmationService {

    ConfirmCheckinVO confirm(Long orderId, ConfirmCheckinRequest request);

    List<RoomAssignmentVO> listAssignments(Long orderId);
}
