import request from '@/utils/request'
export const createOrder = data => request({ url: '/api/orders', method: 'post', data })
export const getMyOrders = params => request({ url: '/api/orders', method: 'get', params })
export const getOrderDetail = id => request({ url: '/api/orders/' + id, method: 'get' })
export const cancelOrder = (id, cancelReason) => request({ url: '/api/orders/' + id + '/cancel', method: 'post', data: { cancelReason } })