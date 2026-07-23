<template>
  <el-timeline>
    <el-timeline-item
      v-for="log in logs"
      :key="log.id"
      :timestamp="formatTime(log.createdAt)"
      placement="top"
    >
      <el-card shadow="hover" class="log-card">
        <div class="log-header">
          <span class="log-operator">{{ getOperatorLabel(log) }}</span>
          <span class="log-time">{{ formatTime(log.createdAt) }}</span>
        </div>
        <div class="log-content">
          <span class="log-status">
            <el-tag :type="tagType(getBeforeStatus(log))" size="small">
              {{ getBeforeStatusText(log) }}
            </el-tag>
            <el-icon><ArrowRight /></el-icon>
            <el-tag :type="tagType(getAfterStatus(log))" size="small">
              {{ getAfterStatusText(log) }}
            </el-tag>
          </span>
        </div>
        <div v-if="log.reason" class="log-reason">
          原因：{{ log.reason }}
        </div>
      </el-card>
    </el-timeline-item>
    <div v-if="!logs || logs.length === 0" class="empty-logs">
      暂无状态变更记录
    </div>
  </el-timeline>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  logs: {
    type: Array,
    default: () => []
  }
})

const getOperatorLabel = (log) => {
  if (log.operatorName) return log.operatorName
  if (log.operatorType && log.operatorId) return `${log.operatorType} #${log.operatorId}`
  if (log.operatorType) return log.operatorType
  if (log.operatorId) return `操作人 #${log.operatorId}`
  return '系统'
}

const getBeforeStatus = (log) => log.beforeStatus || log.oldStatus
const getAfterStatus = (log) => log.afterStatus || log.newStatus
const getBeforeStatusText = (log) => log.beforeStatusText || getStatusText(getBeforeStatus(log))
const getAfterStatusText = (log) => log.afterStatusText || getStatusText(getAfterStatus(log))

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getStatusText = (status) => {
  const map = {
    'AVAILABLE': '空闲',
    'RESERVED': '已预留',
    'OCCUPIED': '在住',
    'DIRTY': '待清洁',
    'MAINTENANCE': '维修',
    'DISABLED': '停用'
  }
  return map[status] || status
}

const tagType = (status) => {
  const map = {
    'AVAILABLE': 'success',
    'RESERVED': 'info',
    'OCCUPIED': 'primary',
    'DIRTY': 'warning',
    'MAINTENANCE': 'danger',
    'DISABLED': 'info'
  }
  return map[status] || 'info'
}
</script>

<style scoped>
.log-card {
  padding: 12px 16px;
}

.log-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.log-operator {
  font-weight: 500;
  color: #374151;
}

.log-time {
  color: #9ca3af;
  font-size: 12px;
}

.log-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.log-reason {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
}

.empty-logs {
  padding: 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}
</style>
