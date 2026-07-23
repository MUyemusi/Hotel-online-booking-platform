import request from '@/utils/request'

/** 模拟支付 */
export function pay(data) {
  return request({
    url: '/api/payments/pay',
    method: 'post',
    data
  })
}

/** 获取订单支付记录 */
export function getPayments(orderId) {
  return request({
    url: `/api/payments/${orderId}`,
    method: 'get'
  })
}
