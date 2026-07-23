import request from '@/utils/request'

/** 获取房型评论列表 */
export function getReviews(roomTypeId, params = {}) {
  return request({
    url: `/api/room-types/${roomTypeId}/reviews`,
    method: 'get',
    params: { current: params.current || 1, size: params.size || 20 }
  })
}

/** 发布评论 */
export function createReview(data) {
  return request({
    url: '/api/reviews',
    method: 'post',
    data
  })
}
