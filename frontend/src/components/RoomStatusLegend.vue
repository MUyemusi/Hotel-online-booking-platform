<template>
  <div class="room-status-legend">
    <div
      v-for="item in statusList"
      :key="item.value"
      class="legend-item"
    >
      <span class="legend-dot" :class="dotClass(item.value)"></span>
      <span class="legend-text">{{ item.label }}</span>
    </div>
  </div>
</template>

<script setup>
const statusList = [
  { value: 'AVAILABLE', label: '空闲' },
  { value: 'RESERVED', label: '已预留' },
  { value: 'OCCUPIED', label: '在住' },
  { value: 'DIRTY', label: '待清洁' },
  { value: 'MAINTENANCE', label: '维修' },
  { value: 'DISABLED', label: '停用' }
]

const dotClass = (status) => {
  const map = {
    'AVAILABLE': 'available',
    'RESERVED': 'reserved',
    'OCCUPIED': 'occupied',
    'DIRTY': 'dirty',
    'MAINTENANCE': 'maintenance',
    'DISABLED': 'disabled'
  }
  return map[status] || ''
}
</script>

<style scoped>
.room-status-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 12px 0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 4px;
}

.legend-dot.available {
  background-color: #10b981;
}

.legend-dot.reserved {
  background-color: #3b82f6;
}

.legend-dot.occupied {
  background-color: #1e40af;
}

.legend-dot.dirty {
  background-color: #f59e0b;
}

.legend-dot.maintenance {
  background-color: #ef4444;
}

.legend-dot.disabled {
  background-color: #9ca3af;
}
</style>
