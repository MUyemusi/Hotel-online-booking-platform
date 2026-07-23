import request from '@/utils/request'

/**
 * 获取待清洁房间列表
 */
export function listPendingCleaningRooms(params) {
  return request({
    url: '/api/admin/rooms/cleaning/pending',
    method: 'get',
    params
  })
}

/**
 * 完成清洁
 */
export function completeCleaning(roomId, data) {
  return request({
    url: `/api/admin/rooms/${roomId}/cleaning/complete`,
    method: 'post',
    data
  })
}

/**
 * 上报清洁问题（转维修）
 */
export function reportCleaningIssue(roomId, data) {
  return request({
    url: `/api/admin/rooms/${roomId}/cleaning/report-issue`,
    method: 'post',
    data
  })
}
