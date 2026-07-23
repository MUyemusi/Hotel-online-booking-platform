import request from '@/utils/request'

/**
 * 获取指定楼层的房间列表
 */
export function getRoomsByFloor(floor) {
  return request({
    url: `/api/admin/rooms/floors/${floor}`,
    method: 'get'
  })
}

/**
 * 获取房间列表（分页）
 */
export function pageRooms(params) {
  return request({
    url: '/api/admin/rooms',
    method: 'get',
    params
  })
}

/**
 * 获取房间详情
 */
export function getRoomDetail(id) {
  return request({
    url: `/api/admin/rooms/${id}`,
    method: 'get'
  })
}

export function updateRoom(id, data) {
  return request({
    url: `/api/admin/rooms/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取楼层统计信息
 */
export function getFloorStatistics() {
  return request({
    url: '/api/admin/rooms/floor-statistics',
    method: 'get'
  })
}

/**
 * 获取房间状态变更日志
 */
export function getRoomStatusLogs(id) {
  return request({
    url: `/api/admin/rooms/${id}/status-logs`,
    method: 'get'
  })
}

/**
 * 变更房间状态
 */
export function changeRoomStatus(id, data) {
  return request({
    url: `/api/admin/rooms/${id}/status`,
    method: 'patch',
    data
  })
}
