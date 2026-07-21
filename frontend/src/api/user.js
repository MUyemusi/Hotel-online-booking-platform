import request from '@/utils/request'

export const getCurrentUser = () => request.get('/api/users/me')
export const updateCurrentUser = data => request.put('/api/users/me', data)
export const changePassword = data => request.put('/api/users/me/password', data)
export const getMyCredit = () => request.get('/api/users/me/credit')
export const getMyCreditRecords = params => request.get('/api/users/me/credit/records', { params })

export const getUsers = params => request.get('/api/admin/users', { params })
export const getUser = userId => request.get(`/api/admin/users/${userId}`)
export const updateUserStatus = (userId, status) => request.put(`/api/admin/users/${userId}/status`, { status })
export const updateUserRoles = (userId, roleCodes) => request.put(`/api/admin/users/${userId}/roles`, { roleCodes })
export const getUserCredit = userId => request.get(`/api/admin/users/${userId}/credit`)
export const getUserCreditRecords = (userId, params) => request.get(`/api/admin/users/${userId}/credit/records`, { params })
export const unbanUser = (userId, reason) => request.put(`/api/admin/users/${userId}/credit/unban`, { reason })
