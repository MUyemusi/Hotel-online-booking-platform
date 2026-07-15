package com.zhisu.hotel.room.service;

import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.room.dto.CompleteCleaningRequest;
import com.zhisu.hotel.room.dto.ReportCleaningIssueRequest;
import com.zhisu.hotel.room.vo.CleaningResultVO;
import com.zhisu.hotel.room.vo.DirtyRoomVO;

public interface RoomCleaningService {

    PageResponse<DirtyRoomVO> listPending(Integer floor, Long roomTypeId, int page, int size);

    CleaningResultVO completeCleaning(Long roomId, CompleteCleaningRequest request);

    CleaningResultVO reportIssue(Long roomId, ReportCleaningIssueRequest request);
}
