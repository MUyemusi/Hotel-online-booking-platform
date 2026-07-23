import request from '@/utils/request'

export function getRoomsByFloor(floor) {
  return request({
    url: `/api/frontdesk/rooms/floors/${floor}`,
    method: 'get'
  })
}

export function getFloorStatistics() {
  return request({
    url: '/api/frontdesk/rooms/floor-statistics',
    method: 'get'
  })
}

export function getRoomDetail(id) {
  return request({
    url: `/api/frontdesk/rooms/${id}`,
    method: 'get'
  })
}

export function getRoomStatusLogs(id) {
  return request({
    url: `/api/frontdesk/rooms/${id}/status-logs`,
    method: 'get'
  })
}
