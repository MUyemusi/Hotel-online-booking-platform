import request from '@/utils/request'

export function getPublicHotelInfo() {
  return request({
    url: '/api/public/hotel-info',
    method: 'get'
  })
}

export function getAdminHotelInfo() {
  return request({
    url: '/api/admin/hotel-info',
    method: 'get'
  })
}

export function updateHotelInfo(data) {
  return request({
    url: '/api/admin/hotel-info',
    method: 'put',
    data
  })
}
