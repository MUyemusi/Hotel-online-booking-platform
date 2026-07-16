package com.zhisu.hotel.checkin.service;

import com.zhisu.hotel.checkin.vo.CheckinQrGenerationVO;
import com.zhisu.hotel.checkin.vo.CheckinQrStatusVO;
import com.zhisu.hotel.checkin.vo.CheckinQrVerificationVO;

public interface CheckinQrService {

    CheckinQrGenerationVO generateForOrder(Long orderId, Long requesterUserId);

    CheckinQrStatusVO getActiveStatus(Long orderId, Long requesterUserId);

    CheckinQrVerificationVO verify(String qrContent);

    void consumeForSuccessfulCheckin(Long tokenId, Long orderId);
}
