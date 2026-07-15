import request from '@/utils/request'

export function getSystemConfigs(group) {
  return request({
    url: '/api/admin/system-configs',
    method: 'get',
    params: group ? { group } : undefined
  })
}

export function getSystemConfig(configKey) {
  return request({
    url: `/api/admin/system-configs/${configKey}`,
    method: 'get'
  })
}

export function updateSystemConfig(configKey, data) {
  return request({
    url: `/api/admin/system-configs/${configKey}`,
    method: 'put',
    data
  })
}
