import request from '@/utils/request'

export const getOperationLogs = params => request.get('/api/admin/operation-logs', { params })
export const getOperationLog = id => request.get(`/api/admin/operation-logs/${id}`)
