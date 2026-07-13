import request from '@/utils/request'

export const checkHealth = () => {
  return request({
    url: '/api/health',
    method: 'get'
  })
}
