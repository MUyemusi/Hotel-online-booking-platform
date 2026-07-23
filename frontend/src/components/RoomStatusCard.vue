<template>
  <div
    class="room-status-card"
    :class="`status-${room.roomStatus.toLowerCase()}`"
    @click="handleClick"
  >
    <div class="room-header">
      <span class="room-no">{{ room.roomNo }}</span>
      <el-tag :type="tagType" size="small" effect="light">
        {{ room.roomStatusText }}</el-tag
      >
    </div>
    <div class="room-type">{{ room.roomTypeName }}</div>
    <div v-if="featureTags.length" class="room-features">
      <span v-for="tag in featureTags" :key="tag" class="feature-tag">{{ tag }}</span>
    </div>
    <div v-if="room.maintenanceReason" class="room-remark">
      {{ room.maintenanceReason }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  room: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const tagType = computed(() => {
  const map = {
    'AVAILABLE': 'success',
    'RESERVED': 'info',
    'OCCUPIED': 'primary',
    'DIRTY': 'warning',
    'MAINTENANCE': 'danger',
    'DISABLED': 'info'
  }
  return map[props.room.roomStatus] || 'info'
})

const featureTags = computed(() => {
  const tags = []
  if (props.room.nearElevator) tags.push('近电梯')
  if (props.room.headRoom) tags.push('头房')
  if (props.room.tailRoom) tags.push('尾房')
  if (props.room.cadImageUrl) tags.push('CAD')
  return tags
})

const handleClick = () => {
  emit('click', props.room)
}
</script>

<style scoped>
.room-status-card {
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  border: 2px solid #e5e7eb;
  background: #fff;
  transition: all 0.2s;
}

.room-status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.room-status-card.status-available {
  border-color: #d1fae5;
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
}

.room-status-card.status-reserved {
  border-color: #dbeafe;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
}

.room-status-card.status-occupied {
  border-color: #c7d2fe;
  background: linear-gradient(135deg, #eef2ff, #c7d2fe);
}

.room-status-card.status-dirty {
  border-color: #fef3c7;
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
}

.room-status-card.status-maintenance {
  border-color: #fecaca;
  background: linear-gradient(135deg, #fef2f2, #fecaca);
}

.room-status-card.status-disabled {
  border-color: #e5e7eb;
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.room-no {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.room-type {
  font-size: 12px;
  color: #6b7280;
}

.room-features {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 8px;
}

.feature-tag {
  padding: 2px 6px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.35);
  color: #475569;
  font-size: 11px;
  line-height: 1.4;
}

.room-remark {
  margin-top: 6px;
  font-size: 11px;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
