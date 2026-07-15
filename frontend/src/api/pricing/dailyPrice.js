import request from '@/utils/request'

export function generateDailyPrices(data) {
  return request({
    url: '/api/admin/pricing/daily-prices/generate',
    method: 'post',
    data
  })
}

export function getDailyPrices(params) {
  return request({
    url: '/api/admin/pricing/daily-prices',
    method: 'get',
    params
  })
}