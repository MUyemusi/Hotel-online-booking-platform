import request from '@/utils/request'

/** 创建订单 */
export function createOrder(data) {
  return request({
    url: '/api/orders',
    method: 'post',
    data
  })
}

/** 我的订单列表 */
export function getMyOrders(params = {}) {
  return request({
    url: '/api/orders',
    method: 'get',
    params: {
      status: params.status || undefined,
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

/** 订单详情 */
export function getOrderDetail(id) {
  return request({
    url: `/api/orders/${id}`,
    method: 'get'
  })
}

/** 取消订单 */
export function cancelOrder(id, data) {
  return request({
    url: `/api/orders/${id}/cancel`,
    method: 'post',
    data
  })
}
