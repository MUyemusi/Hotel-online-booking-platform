import request from '@/utils/request'

export function chatWithAssistant(data) {
  return request({
    url: '/api/public/assistant/chat',
    method: 'post',
    data
  })
}
