import request from '@/utils/request'
export const searchRoomTypes = params => request({ url: '/api/public/room-types', method: 'get', params })
export const getRoomType = id => request({ url: '/api/room-types/' + id, method: 'get' })
export const getRoomTypeImages = id => request({ url: '/api/room-types/' + id + '/images', method: 'get' })
export const getRoomTypeFacilities = id => request({ url: '/api/room-types/' + id + '/facilities', method: 'get' })