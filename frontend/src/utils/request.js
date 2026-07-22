import axios from 'axios'

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
  }
  return config
})

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