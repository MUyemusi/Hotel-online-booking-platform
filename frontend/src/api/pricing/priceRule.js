import request from '@/utils/request'

export function getPriceRules(params) {
  return request({ url: '/api/admin/pricing/rules', method: 'get', params })
}

export function createPriceRule(data) {
  return request({ url: '/api/admin/pricing/rules', method: 'post', data })
}

export function updatePriceRule(id, data) {
  return request({ url: `/api/admin/pricing/rules/${id}`, method: 'put', data })
}

export function updatePriceRuleStatus(id, status) {
  return request({ url: `/api/admin/pricing/rules/${id}/status`, method: 'patch', data: { status } })
}

export function deletePriceRule(id) {
  return request({ url: `/api/admin/pricing/rules/${id}`, method: 'delete' })
}
