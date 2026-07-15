import request from '@/utils/request'

export function getBasePrices(params) {
  return request({
    url: '/api/admin/pricing/base-prices',
    method: 'get',
    params
  })
}

export function updateBasePrice(roomTypeId, data) {
  return request({
    url: `/api/admin/pricing/base-prices/${roomTypeId}`,
    method: 'put',
    data
  })
}

export function getBasePriceLogs(params) {
  return request({
    url: '/api/admin/pricing/base-price-logs',
    method: 'get',
    params
  })
}
