import request from '@/utils/request'

export function getAdminBanners() {
  return request({
    url: '/api/admin/banners',
    method: 'get'
  })
}

export function createBanner(data) {
  return request({
    url: '/api/admin/banners',
    method: 'post',
    data
  })
}

export function updateBanner(id, data) {
  return request({
    url: `/api/admin/banners/${id}`,
    method: 'put',
    data
  })
}

export function publishBanner(id) {
  return request({
    url: `/api/admin/banners/${id}/publish`,
    method: 'patch'
  })
}

export function offlineBanner(id) {
  return request({
    url: `/api/admin/banners/${id}/offline`,
    method: 'patch'
  })
}

export function deleteBanner(id) {
  return request({
    url: `/api/admin/banners/${id}`,
    method: 'delete'
  })
}

export function getPublicBanners() {
  return request({
    url: '/api/public/banners',
    method: 'get'
  })
}
