package com.zhisu.hotel.user.service;

import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.user.vo.CreditRecordVO;
import com.zhisu.hotel.user.vo.CreditSummaryVO;

public interface CreditService {

    boolean checkBookingPermission(Long userId);

    default void recordUserCancellation(Long userId) {
        recordUserCancellation(userId, null);
    }

    void recordUserCancellation(Long userId, Long orderId);

    default void resetContinuousCancellation(Long userId) {
        resetContinuousCancellation(userId, null);
    }

    void resetContinuousCancellation(Long userId, Long orderId);

    CreditSummaryVO getCreditSummary(Long userId);

    PageResponse<CreditRecordVO> getCreditRecords(Long userId, long current, long size);

    void unbanUser(Long userId, Long operatorId, String reason);
}
