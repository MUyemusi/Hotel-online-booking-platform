import request from '@/utils/request'

export function getAdminAnnouncements() {
  return request({
    url: '/api/admin/announcements',
    method: 'get'
  })
}

export function getAnnouncementDetail(id) {
  return request({
    url: `/api/admin/announcements/${id}`,
    method: 'get'
  })
}

export function createAnnouncement(data) {
  return request({
    url: '/api/admin/announcements',
    method: 'post',
    data
  })
}

export function updateAnnouncement(id, data) {
  return request({
    url: `/api/admin/announcements/${id}`,
    method: 'put',
    data
  })
}

export function publishAnnouncement(id) {
  return request({
    url: `/api/admin/announcements/${id}/publish`,
    method: 'patch'
  })
}

export function offlineAnnouncement(id) {
  return request({
    url: `/api/admin/announcements/${id}/offline`,
    method: 'patch'
  })
}

export function deleteAnnouncement(id) {
  return request({
    url: `/api/admin/announcements/${id}`,
    method: 'delete'
  })
}

export function getPublicAnnouncements() {
  return request({
    url: '/api/public/announcements',
    method: 'get'
  })
}
