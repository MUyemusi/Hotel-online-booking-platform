import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器：给 /api/admin/ 和 /api/frontdesk/ 自动加 JWT
request.interceptors.request.use(config => {
  const url = config.url || ''
  if (url.startsWith('/api/admin/') || url.startsWith('/api/frontdesk/')) {
    const token = window.sessionStorage.getItem('zhisu_token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  response => response.data,
  error => {
    const status = error?.response?.status
    if (status === 401) {
      console.error('未登录或登录已过期，请重新登录')
    }
    if (status === 403) {
      console.error('当前账号没有操作权限')
    }
    return Promise.reject(error)
  }
)

export default request
