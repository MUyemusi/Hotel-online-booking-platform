import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000
})

function resolveAdminAuthorization() {
  const stored = window.sessionStorage.getItem('zhisu_admin_basic_auth')
  if (stored) {
    return stored
  }
  const defaultToken = `Basic ${window.btoa('admin:admin123')}`
  window.sessionStorage.setItem('zhisu_admin_basic_auth', defaultToken)
  return defaultToken
}

request.interceptors.request.use(config => {
  const url = config.url || ''
  if (url.startsWith('/api/admin/')) {
    config.headers = config.headers || {}
    config.headers.Authorization = resolveAdminAuthorization()
  }
  return config
})

request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    const status = error?.response?.status
    if (status === 401) {
      console.error('管理员认证失败，请确认默认账号 admin / admin123 已初始化')
    }
    if (status === 403) {
      console.error('当前账号没有管理员权限')
    }
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default request