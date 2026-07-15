package com.zhisu.hotel.frontdesk.service;

import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.frontdesk.dto.FrontdeskOrderQueryRequest;
import com.zhisu.hotel.frontdesk.vo.CheckinVerificationVO;
import com.zhisu.hotel.frontdesk.vo.FrontdeskOrderDetailVO;
import com.zhisu.hotel.frontdesk.vo.FrontdeskOrderListVO;

public interface FrontdeskOrderQueryService {

    PageResponse<FrontdeskOrderListVO> searchOrders(FrontdeskOrderQueryRequest request);

    FrontdeskOrderDetailVO getOrderDetail(Long orderId);

    CheckinVerificationVO verifyCheckin(Long orderId);

    CheckinVerificationVO verifyCheckinByOrderNo(String orderNo);
}
