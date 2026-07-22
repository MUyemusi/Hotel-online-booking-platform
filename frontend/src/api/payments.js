import request from '@/utils/request'
export const payOrder = data => request({ url: '/api/payments/pay', method: 'post', data })
export const getPayments = orderId => request({ url: '/api/payments/' + orderId, method: 'get' })