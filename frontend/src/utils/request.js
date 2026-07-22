import axios from 'axios'
import { clearSession, getToken } from '@/utils/auth'

const ACCESS_KEY = 'zhisu_access_token'
const USER_KEY = 'zhisu_current_user'
const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const request = axios.create({ baseURL, timeout: 15000, withCredentials: true })
let refreshPromise = null

export function getAccessToken() { return window.sessionStorage.getItem(ACCESS_KEY) }
export function getCurrentUser() {
  try { return JSON.parse(window.sessionStorage.getItem(USER_KEY) || 'null') } catch { return null }
}
export function setAuthSession(data) {
  if (data?.accessToken) window.sessionStorage.setItem(ACCESS_KEY, data.accessToken)
  if (data?.user) window.sessionStorage.setItem(USER_KEY, JSON.stringify(data.user))
}
export function clearAuthSession() {
  window.sessionStorage.removeItem(ACCESS_KEY)
  window.sessionStorage.removeItem(USER_KEY)
}
export function hasRole(role) {
  return Boolean(getAccessToken() && getCurrentUser()?.roles?.includes(role))
}

request.interceptors.request.use(config => {
  const token = getAccessToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = 'Bearer ' + token
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
  async error => {
    const original = error.config || {}
    const isAuthEndpoint = String(original.url || '').includes('/api/auth/')
    if (error?.response?.status === 401 && !original._retry && !isAuthEndpoint) {
      original._retry = true
      try {
        refreshPromise ||= axios.post(baseURL + '/api/auth/refresh', null, { withCredentials: true })
          .then(response => response.data)
          .finally(() => { refreshPromise = null })
        const result = await refreshPromise
        if (result?.code !== 'SUCCESS') throw new Error(result?.message || '登录已过期')
        setAuthSession(result.data)
        original.headers = original.headers || {}
        original.headers.Authorization = 'Bearer ' + result.data.accessToken
        return request(original)
      } catch (refreshError) {
        clearAuthSession()
        if (window.location.pathname !== '/login') {
          window.location.assign('/login?redirect=' + encodeURIComponent(window.location.pathname + window.location.search))
        }
        return Promise.reject(refreshError)
      }
    }
    return Promise.reject(error)
  }
)
export default request
  response => {
    const payload = response.data
    if (payload && typeof payload === 'object' && payload.code && payload.code !== 'SUCCESS') {
      const error = new Error(payload.message || '操作失败')
      error.code = payload.code
      error.response = response
      return Promise.reject(error)
    }
    return payload
  },
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
