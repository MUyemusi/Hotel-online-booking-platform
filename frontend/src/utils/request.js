import axios from 'axios'
import { clearSession, getToken } from '@/utils/auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000
})

// 除登录、注册外，所有业务接口都自动携带 JWT。
request.interceptors.request.use(config => {
  const url = config.url || ''
  const publicAuthApi = url === '/api/auth/login' || url === '/api/auth/register'
  const token = getToken()
  if (!publicAuthApi && token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  response => response.data,
  error => {
    const status = error?.response?.status
    if (status === 401) {
      clearSession()
      if (window.location.pathname !== '/login') {
        const redirect = encodeURIComponent(window.location.pathname + window.location.search)
        window.location.replace(`/login?redirect=${redirect}`)
      }
    }
    if (status === 403) {
      if (window.location.pathname !== '/403') window.location.replace('/403')
    }
    // 业务接口的 5xx 由当前页面就地展示错误，避免一个统计接口失败时
    // 把整个后台强制跳转到 500 页面，导致其他可用模块也无法访问。
    return Promise.reject(error)
  }
)

export default request
