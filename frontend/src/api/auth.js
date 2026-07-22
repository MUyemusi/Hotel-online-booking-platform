import request from '@/utils/request'
export const register = data => request({ url: '/api/auth/register', method: 'post', data })
export const login = data => request({ url: '/api/auth/login', method: 'post', data })
export const refresh = () => request({ url: '/api/auth/refresh', method: 'post' })
export const logout = () => request({ url: '/api/auth/logout', method: 'post' })
export const getMe = () => request({ url: '/api/auth/me', method: 'get' })