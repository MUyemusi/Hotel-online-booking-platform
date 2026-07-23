import request from '@/utils/request'

/** 查询日期范围内的库存 */
export function getInventory(params) {
  return request({
    url: '/api/admin/inventory',
    method: 'get',
    params: {
      roomTypeId: params.roomTypeId,
      startDate: params.startDate,
      endDate: params.endDate
    }
  })
}

/** 修改单条库存 */
export function updateInventory(id, data) {
  return request({
    url: `/api/admin/inventory/${id}`,
    method: 'put',
    data
  })
}

/** 暂停销售 */
export function pauseInventory(id) {
  return request({
    url: `/api/admin/inventory/${id}/pause`,
    method: 'put'
  })
}

/** 恢复销售 */
export function resumeInventory(id) {
  return request({
    url: `/api/admin/inventory/${id}/resume`,
    method: 'put'
  })
}

/** 按日期范围批量修改库存 */
export function batchUpdateInventory(data) {
  return request({
    url: '/api/admin/inventory/batch',
    method: 'put',
    data
  })
}

/** 自动生成库存 */
export function generateInventory(params) {
  return request({
    url: '/api/admin/inventory/generate',
    method: 'post',
    params
  })
}

/** 库存变更日志 */
export function getInventoryLogs(params) {
  return request({
    url: '/api/admin/inventory/logs',
    method: 'get',
    params: {
      roomTypeId: params.roomTypeId,
      startDate: params.startDate,
      endDate: params.endDate
    }
  })
}
