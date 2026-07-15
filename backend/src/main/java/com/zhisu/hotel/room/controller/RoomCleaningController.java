package com.zhisu.hotel.room.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.room.dto.CompleteCleaningRequest;
import com.zhisu.hotel.room.dto.ReportCleaningIssueRequest;
import com.zhisu.hotel.room.service.RoomCleaningService;
import com.zhisu.hotel.room.vo.CleaningResultVO;
import com.zhisu.hotel.room.vo.DirtyRoomVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
public class RoomCleaningController {

    private final RoomCleaningService roomCleaningService;

    @GetMapping("/cleaning/pending")
    public ApiResponse<PageResponse<DirtyRoomVO>> listPendingCleaningRooms(
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(roomCleaningService.listPending(floor, roomTypeId, page, size));
    }

    @PostMapping("/{roomId}/cleaning/complete")
    public ApiResponse<CleaningResultVO> completeCleaning(@PathVariable Long roomId,
                                                          @Valid @RequestBody CompleteCleaningRequest request) {
        return ApiResponse.success(roomCleaningService.completeCleaning(roomId, request));
    }

    @PostMapping("/{roomId}/cleaning/report-issue")
    public ApiResponse<CleaningResultVO> reportIssue(@PathVariable Long roomId,
                                                     @Valid @RequestBody ReportCleaningIssueRequest request) {
        return ApiResponse.success(roomCleaningService.reportIssue(roomId, request));
    }
}
