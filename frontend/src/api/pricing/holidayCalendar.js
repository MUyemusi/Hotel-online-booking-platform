import request from '@/utils/request'

export function getHolidayCalendar(params) {
  return request({
    url: '/api/admin/holiday-calendar',
    method: 'get',
    params
  })
}

export function getHolidayCalendarDetail(id) {
  return request({
    url: `/api/admin/holiday-calendar/${id}`,
    method: 'get'
  })
}

export function createHolidayCalendar(data) {
  return request({
    url: '/api/admin/holiday-calendar',
    method: 'post',
    data
  })
}

export function updateHolidayCalendar(id, data) {
  return request({
    url: `/api/admin/holiday-calendar/${id}`,
    method: 'put',
    data
  })
}

export function updateHolidayCalendarStatus(id, status) {
  return request({
    url: `/api/admin/holiday-calendar/${id}/status`,
    method: 'patch',
    data: { status }
  })
}

export function deleteHolidayCalendar(id) {
  return request({
    url: `/api/admin/holiday-calendar/${id}`,
    method: 'delete'
  })
}

export function previewHolidayRule(date) {
  return request({
    url: '/api/admin/holiday-calendar/preview',
    method: 'get',
    params: { date }
  })
}