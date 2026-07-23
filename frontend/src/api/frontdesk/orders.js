import request from '@/utils/request'

/**
 * 前台搜索订单
 */
export function searchFrontdeskOrders(params) {
  return request({
    url: '/api/frontdesk/orders',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 */
export function getFrontdeskOrderDetail(orderId) {
  return request({
    url: `/api/frontdesk/orders/${orderId}`,
    method: 'get'
  })
}

/**
 * 入住资格核验
 */
export function verifyCheckin(orderId) {
  return request({
    url: `/api/frontdesk/orders/${orderId}/check-in-verification`,
    method: 'get'
  })
}

/**
 * 通过订单号核验
 */
export function verifyCheckinByOrderNo(orderNo) {
  return request({
    url: '/api/frontdesk/orders/verify',
    method: 'get',
    params: { orderNo }
  })
}
