
import request from '@/utils/request'

export function getDashboardOverview() {
  return request({
    url: '/api/admin/dashboard/overview',
    method: 'get'
  })
}

export function getDashboardTrends(params) {
  return request({
    url: '/api/admin/dashboard/trends',
    method: 'get',
    params
  })
}

export function getDashboardAnalytics(params) {
  return request({ url: '/api/admin/dashboard/analytics', method: 'get', params })
}

export function getRoomTypeStatistics() {
  return request({
    url: '/api/admin/dashboard/room-type-statistics',
    method: 'get'
  })
}

export function getInventoryWarnings() {
  return request({
    url: '/api/admin/dashboard/inventory-warnings',
    method: 'get'
  })
}
