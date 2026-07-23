import request from '@/utils/request'

/**
 * 二维码核验
 */
export function verifyCheckinQr(data) {
  return request({
    url: '/api/frontdesk/checkin/qr/verify',
    method: 'post',
    data: {
      credential: data.credential
    }
  })
}

/**
 * 确认入住
 */
export function confirmCheckin(orderId, data) {
  return request({
    url: `/api/frontdesk/orders/${orderId}/check-in`,
    method: 'post',
    data
  })
}

/**
 * 获取订单分房信息
 */
export function listOrderAssignments(orderId) {
  return request({
    url: `/api/frontdesk/orders/${orderId}/assignments`,
    method: 'get'
  })
}

/**
 * 确认退房
 */
export function confirmCheckout(orderId, data) {
  return request({
    url: `/api/frontdesk/orders/${orderId}/check-out`,
    method: 'post',
    data
  })
}
