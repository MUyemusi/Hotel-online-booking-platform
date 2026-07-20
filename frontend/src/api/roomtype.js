import request from '@/utils/request'

// ========== 用户端 ==========

/** 获取已上架房型列表（含分页） */
export function getPublicRoomTypes(params = {}) {
  return request({
    url: '/api/room-types',
    method: 'get',
    params: {
      page: params.page || 1,
      size: params.size || 10,
      keyword: params.keyword || undefined
    }
  })
}

/** 房型详情 */
export function getRoomTypeDetail(id) {
  return request({
    url: `/api/room-types/${id}`,
    method: 'get'
  })
}

/** 房型图片 */
export function getRoomTypeImages(id) {
  return request({
    url: `/api/room-types/${id}/images`,
    method: 'get'
  })
}

/** 房型设施 */
export function getRoomTypeFacilities(id) {
  return request({
    url: `/api/room-types/${id}/facilities`,
    method: 'get'
  })
}

/** 房型摘要 */
export function getRoomTypeSummary(id) {
  return request({
    url: `/api/room-types/${id}/summary`,
    method: 'get'
  })
}

/** 检查房型是否可售 */
export function checkRoomTypeAvailable(id) {
  return request({
    url: `/api/room-types/${id}/available`,
    method: 'get'
  })
}

// ========== 管理端 ==========

/** 管理端房型分页列表 */
export function getAdminRoomTypes(params = {}) {
  return request({
    url: '/api/admin/room-types',
    method: 'get',
    params: {
      page: params.page || 1,
      size: params.size || 10,
      keyword: params.keyword || undefined,
      category: params.category || undefined,
      status: params.status || undefined
    }
  })
}

/** 管理端房型详情 */
export function getAdminRoomTypeDetail(id) {
  return request({
    url: `/api/admin/room-types/${id}`,
    method: 'get'
  })
}

/** 新增房型 */
export function createRoomType(data) {
  return request({
    url: '/api/admin/room-types',
    method: 'post',
    data
  })
}

/** 修改房型 */
export function updateRoomType(id, data) {
  return request({
    url: `/api/admin/room-types/${id}`,
    method: 'put',
    data
  })
}

/** 删除房型 */
export function deleteRoomType(id) {
  return request({
    url: `/api/admin/room-types/${id}`,
    method: 'delete'
  })
}

/** 上架房型 */
export function onlineRoomType(id) {
  return request({
    url: `/api/admin/room-types/${id}/online`,
    method: 'put'
  })
}

/** 下架房型 */
export function offlineRoomType(id) {
  return request({
    url: `/api/admin/room-types/${id}/offline`,
    method: 'put'
  })
}

// ========== 房型图片管理 ==========

export function getAdminRoomTypeImages(id) {
  return request({
    url: `/api/admin/room-types/${id}/images`,
    method: 'get'
  })
}

export function addRoomTypeImage(id, data) {
  return request({
    url: `/api/admin/room-types/${id}/images`,
    method: 'post',
    data
  })
}

export function deleteRoomTypeImage(imageId) {
  return request({
    url: `/api/admin/room-types/images/${imageId}`,
    method: 'delete'
  })
}

export function setMainImage(imageId) {
  return request({
    url: `/api/admin/room-types/images/${imageId}/main`,
    method: 'put'
  })
}

// ========== 房型设施管理 ==========

export function getAdminRoomTypeFacilities(id) {
  return request({
    url: `/api/admin/room-types/${id}/facilities`,
    method: 'get'
  })
}

export function addRoomTypeFacility(id, data) {
  return request({
    url: `/api/admin/room-types/${id}/facilities`,
    method: 'post',
    data
  })
}

export function deleteRoomTypeFacility(facilityId) {
  return request({
    url: `/api/admin/room-types/facilities/${facilityId}`,
    method: 'delete'
  })
}
