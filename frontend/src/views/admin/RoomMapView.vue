<template>
  <div class="room-map-view">
    <div class="page-header">
      <div>
        <h1>楼层房态地图</h1>
        <p>实时查看各楼层房间状态分布</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="errorMessage"
      type="error"
      :title="errorMessage"
      show-icon
      class="page-alert"
    />

    <el-card shadow="never" class="stats-card">
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-label">房间总数</span>
          <span class="stat-value">{{ totalStats.total || 0 }}</span>
        </div>
        <div class="stat-item available">
          <span class="stat-label">空闲</span>
          <span class="stat-value">{{ totalStats.available || 0 }}</span>
        </div>
        <div class="stat-item occupied">
          <span class="stat-label">在住</span>
          <span class="stat-value">{{ totalStats.occupied || 0 }}</span>
        </div>
        <div class="stat-item reserved">
          <span class="stat-label">已预留</span>
          <span class="stat-value">{{ totalStats.reserved || 0 }}</span>
        </div>
        <div class="stat-item dirty">
          <span class="stat-label">待清洁</span>
          <span class="stat-value">{{ totalStats.dirty || 0 }}</span>
        </div>
        <div class="stat-item maintenance">
          <span class="stat-label">维修</span>
          <span class="stat-value">{{ totalStats.maintenance || 0 }}</span>
        </div>
        <div class="stat-item disabled">
          <span class="stat-label">停用</span>
          <span class="stat-value">{{ totalStats.disabled || 0 }}</span>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <div class="filter-label">楼层选择：</div>
        <el-radio-group v-model="currentFloor" size="large" @change="onFloorChange">
          <el-radio-button
            v-for="floor in [3, 4, 5, 6, 7, 8]"
            :key="floor"
            :label="floor"
          >
            {{ floor }}楼
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-row filter-controls">
        <el-select v-model="roomTypeFilter" placeholder="全部房型" clearable style="width: 180px">
          <el-option
            v-for="item in roomTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 150px">
          <el-option value="AVAILABLE" label="空闲" />
          <el-option value="RESERVED" label="已预留" />
          <el-option value="OCCUPIED" label="在住" />
          <el-option value="DIRTY" label="待清洁" />
          <el-option value="MAINTENANCE" label="维修" />
          <el-option value="DISABLED" label="停用" />
        </el-select>
        <el-input
          v-model="roomNoKeyword"
          clearable
          placeholder="搜索房间号"
          style="width: 180px"
          @keyup.enter="noop"
        />
        <el-button @click="resetFilters">重置</el-button>
        <div class="spacer"></div>
        <RoomStatusLegend />
      </div>
    </el-card>

    <el-card shadow="never" class="rooms-card">
      <div class="floor-header">
        <div class="floor-title">{{ currentFloor }}楼房间布局</div>
        <div class="floor-count">当前楼层 {{ currentFloorRooms.length }} 间，筛选后 {{ filteredRooms.length }} 间</div>
      </div>
      <div v-loading="loading" class="rooms-grid">
        <RoomStatusCard
          v-for="room in filteredRooms"
          :key="room.id"
          :room="room"
          @click="openRoomDetail(room)"
        />
        <div v-if="!loading && filteredRooms.length === 0" class="empty-state">
          该楼层暂无符合条件的房间
        </div>
      </div>
    </el-card>

    <el-drawer
      v-model="detailVisible"
      :title="`房间 ${selectedRoom?.roomNo || ''} 详情`"
      size="480px"
    >
      <div v-loading="detailLoading">
        <template v-if="selectedRoom">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="房间号">{{ selectedRoom.roomNo }}</el-descriptions-item>
            <el-descriptions-item label="楼层">{{ selectedRoom.floor }}楼</el-descriptions-item>
            <el-descriptions-item label="房型">{{ selectedRoom.roomTypeName }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="tagType(selectedRoom.roomStatus)">
                {{ selectedRoom.roomStatusText }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="selectedRoom.maintenanceReason" label="维修原因">
              {{ selectedRoom.maintenanceReason }}
            </el-descriptions-item>
            <el-descriptions-item v-if="selectedRoom.remark" label="备注">
              {{ selectedRoom.remark }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatTime(selectedRoom.updatedAt) }}
            </el-descriptions-item>
          </el-descriptions>

          <section class="detail-section">
            <h4>当前住客</h4>
            <div class="guest-card">
              <template v-if="selectedRoom.currentGuestName || selectedRoom.currentGuestPhone">
                <div>
                  <span class="detail-label">姓名</span>
                  <strong>{{ selectedRoom.currentGuestName || '未登记' }}</strong>
                </div>
                <div>
                  <span class="detail-label">电话</span>
                  <strong>{{ selectedRoom.currentGuestPhone || '未登记' }}</strong>
                </div>
                <div v-if="selectedRoom.currentOrderNo">
                  <span class="detail-label">订单号</span>
                  <strong>{{ selectedRoom.currentOrderNo }}</strong>
                </div>
              </template>
              <span v-else class="muted-text">
                {{ selectedRoom.roomStatus === 'OCCUPIED' ? '暂无住客信息' : '当前无在住住客' }}
              </span>
            </div>
          </section>

          <section class="detail-section">
            <h4>房间位置</h4>
            <div class="feature-list">
              <el-tag :type="selectedRoom.nearElevator ? 'success' : 'info'" effect="light">
                {{ selectedRoom.nearElevator ? '近电梯' : '非近电梯' }}
              </el-tag>
              <el-tag :type="selectedRoom.headRoom ? 'warning' : 'info'" effect="light">
                {{ selectedRoom.headRoom ? '头房' : '非头房' }}
              </el-tag>
              <el-tag :type="selectedRoom.tailRoom ? 'warning' : 'info'" effect="light">
                {{ selectedRoom.tailRoom ? '尾房' : '非尾房' }}
              </el-tag>
            </div>
            <el-form v-if="isAdmin" class="room-meta-form" label-position="top">
              <el-form-item label="管理员维护">
                <el-checkbox v-model="roomMetaForm.nearElevator">近电梯</el-checkbox>
                <el-checkbox v-model="roomMetaForm.headRoom">头房</el-checkbox>
                <el-checkbox v-model="roomMetaForm.tailRoom">尾房</el-checkbox>
              </el-form-item>
            </el-form>
          </section>

          <section class="detail-section">
            <div class="section-title-row">
              <h4>CAD 图片</h4>
              <el-upload
                v-if="isAdmin"
                accept="image/*"
                :show-file-list="false"
                :http-request="uploadCadImage"
              >
                <el-button size="small" :loading="cadUploading">
                  <el-icon><UploadFilled /></el-icon>
                  导入图片
                </el-button>
              </el-upload>
            </div>
            <el-image
              v-if="cadPreviewUrl"
              class="cad-preview"
              :src="cadPreviewUrl"
              :preview-src-list="[cadPreviewUrl]"
              fit="contain"
            />
            <div v-else class="cad-empty">尚未导入 CAD 图片</div>
            <el-button
              v-if="isAdmin"
              type="primary"
              class="save-meta-button"
              :loading="metaSaving"
              @click="saveRoomMetadata"
            >
              保存房间资料
            </el-button>
          </section>

          <div class="status-actions">
            <h4>状态操作</h4>
            <el-alert
              v-if="!canOperateRoom"
              type="info"
              title="当前账号仅可查看房态。清洁、维修、停用与恢复操作需管理员账号。"
              show-icon
              :closable="false"
              class="role-alert"
            />
            <div v-else class="action-buttons">
              <el-button
                v-if="selectedRoom.roomStatus === 'AVAILABLE'"
                type="danger"
                @click="handleMarkMaintenance"
              >
                标记维修
              </el-button>
              <el-button
                v-if="selectedRoom.roomStatus === 'AVAILABLE'"
                type="warning"
                @click="handleMarkDisabled"
              >
                停用房间
              </el-button>
              <el-button
                v-if="selectedRoom.roomStatus === 'MAINTENANCE'"
                type="success"
                @click="handleMaintenanceComplete"
              >
                维修完成
              </el-button>
              <el-button
                v-if="selectedRoom.roomStatus === 'DISABLED'"
                type="success"
                @click="handleRestoreAvailable"
              >
                恢复使用
              </el-button>
              <el-button
                v-if="selectedRoom.roomStatus === 'DIRTY'"
                type="success"
                @click="handleCleaningComplete"
              >
                清洁完成
              </el-button>
              <el-button
                v-if="selectedRoom.roomStatus === 'DIRTY'"
                type="danger"
                @click="handleReportIssue"
              >
                发现问题转维修
              </el-button>
            </div>
          </div>

          <div class="status-logs">
            <h4>状态变更记录</h4>
            <RoomStatusTimeline :logs="roomLogs" />
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="maintenanceDialogVisible"
      title="标记维修"
      width="420px"
    >
      <el-form :model="maintenanceForm" label-width="80px">
        <el-form-item label="维修原因">
          <el-input
            v-model="maintenanceForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入维修原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="maintenanceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMarkMaintenance" :loading="submitting">
          确认
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="cleaningDialogVisible"
      title="清洁完成"
      width="420px"
    >
      <el-form :model="cleaningForm" label-width="80px">
        <el-form-item label="清洁备注">
          <el-input
            v-model="cleaningForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入清洁备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cleaningDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCleaningComplete" :loading="submitting">
          确认
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="issueDialogVisible"
      title="发现问题转维修"
      width="420px"
    >
      <el-form :model="issueForm" label-width="80px">
        <el-form-item label="问题描述">
          <el-input
            v-model="issueForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请描述发现的问题"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReportIssue" :loading="submitting">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, UploadFilled } from '@element-plus/icons-vue'
import { getCurrentUser } from '@/utils/request'
import {
  getRoomsByFloor as getAdminRoomsByFloor,
  getFloorStatistics as getAdminFloorStatistics,
  getRoomDetail as getAdminRoomDetail,
  getRoomStatusLogs as getAdminRoomStatusLogs,
  updateRoom,
  changeRoomStatus
} from '@/api/room/rooms'
import {
  completeCleaning,
  reportCleaningIssue
} from '@/api/room/cleaning'
import {
  getRoomsByFloor as getFrontdeskRoomsByFloor,
  getFloorStatistics as getFrontdeskFloorStatistics,
  getRoomDetail as getFrontdeskRoomDetail,
  getRoomStatusLogs as getFrontdeskRoomStatusLogs
} from '@/api/frontdesk/rooms'
import { uploadImage } from '@/api/content/upload'
import RoomStatusLegend from '@/components/RoomStatusLegend.vue'
import RoomStatusCard from '@/components/RoomStatusCard.vue'
import RoomStatusTimeline from '@/components/RoomStatusTimeline.vue'

const currentUser = getCurrentUser() || {}

const loading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const currentFloor = ref(3)
const statusFilter = ref('')
const roomTypeFilter = ref('')
const roomNoKeyword = ref('')
const currentFloorRooms = ref([])
const floorStatistics = ref([])
const detailVisible = ref(false)
const selectedRoom = ref(null)
const roomLogs = ref([])
const metaSaving = ref(false)
const cadUploading = ref(false)
const roomMetaForm = ref({
  nearElevator: false,
  headRoom: false,
  tailRoom: false,
  cadImageUrl: ''
})

const maintenanceDialogVisible = ref(false)
const cleaningDialogVisible = ref(false)
const issueDialogVisible = ref(false)

const maintenanceForm = ref({ reason: '' })
const cleaningForm = ref({ remark: '' })
const issueForm = ref({ reason: '' })

const statusTextMap = {
  AVAILABLE: '空闲',
  RESERVED: '已预留',
  OCCUPIED: '在住',
  DIRTY: '待清洁',
  MAINTENANCE: '维修',
  DISABLED: '停用'
}

const isAdmin = computed(() => currentUser?.roles?.includes('ADMIN'))
const canOperateRoom = computed(() => isAdmin.value)
const cadPreviewUrl = computed(() => {
  return isAdmin.value ? roomMetaForm.value.cadImageUrl : selectedRoom.value?.cadImageUrl
})

const roomTypeOptions = computed(() => {
  const map = new Map()
  currentFloorRooms.value.forEach(room => {
    if (room.roomTypeId && room.roomTypeName) {
      map.set(room.roomTypeId, room.roomTypeName)
    }
  })
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }))
})

const filteredRooms = computed(() => {
  const keyword = roomNoKeyword.value.trim()
  return currentFloorRooms.value.filter(room => {
    if (statusFilter.value && room.roomStatus !== statusFilter.value) return false
    if (roomTypeFilter.value && room.roomTypeId !== roomTypeFilter.value) return false
    if (keyword && !room.roomNo.includes(keyword)) return false
    return true
  })
})

const totalStats = computed(() => {
  return floorStatistics.value.reduce((acc, item) => {
    acc.total += Number(item.totalCount || 0)
    acc.available += Number(item.availableCount || 0)
    acc.reserved += Number(item.reservedCount || 0)
    acc.occupied += Number(item.occupiedCount || 0)
    acc.dirty += Number(item.dirtyCount || 0)
    acc.maintenance += Number(item.maintenanceCount || 0)
    acc.disabled += Number(item.disabledCount || 0)
    return acc
  }, {
    total: 0,
    available: 0,
    reserved: 0,
    occupied: 0,
    dirty: 0,
    maintenance: 0,
    disabled: 0
  })
})

const normalizeRoom = (room = {}) => ({
  ...room,
  id: room.id ?? room.roomId,
  roomId: room.roomId ?? room.id,
  nearElevator: Boolean(room.nearElevator),
  headRoom: Boolean(room.headRoom),
  tailRoom: Boolean(room.tailRoom),
  roomStatusText: room.roomStatusText || statusTextMap[room.roomStatus] || room.roomStatus
})

const syncRoomMetaForm = (room = {}) => {
  roomMetaForm.value = {
    nearElevator: Boolean(room.nearElevator),
    headRoom: Boolean(room.headRoom),
    tailRoom: Boolean(room.tailRoom),
    cadImageUrl: room.cadImageUrl || ''
  }
}

const normalizeLog = (log = {}) => ({
  ...log,
  id: log.id ?? `${log.roomId || 'room'}_${log.createdAt || Date.now()}`
})

const getQueryApi = () => {
  if (isAdmin.value) {
    return {
      getRoomsByFloor: getAdminRoomsByFloor,
      getFloorStatistics: getAdminFloorStatistics,
      getRoomDetail: getAdminRoomDetail,
      getRoomStatusLogs: getAdminRoomStatusLogs
    }
  }
  return {
    getRoomsByFloor: getFrontdeskRoomsByFloor,
    getFloorStatistics: getFrontdeskFloorStatistics,
    getRoomDetail: getFrontdeskRoomDetail,
    getRoomStatusLogs: getFrontdeskRoomStatusLogs
  }
}

const extractErrorMessage = (error, fallback = '操作失败') => {
  return error?.response?.data?.message || error?.message || fallback
}

const createRequestToken = (prefix) => {
  if (window.crypto?.randomUUID) {
    return `${prefix}_${window.crypto.randomUUID().replace(/-/g, '')}`
  }
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
}

const noop = () => {}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const tagType = (status) => {
  const map = {
    AVAILABLE: 'success',
    RESERVED: 'info',
    OCCUPIED: 'primary',
    DIRTY: 'warning',
    MAINTENANCE: 'danger',
    DISABLED: 'info'
  }
  return map[status] || 'info'
}

const loadStatistics = async () => {
  const api = getQueryApi()
  const response = await api.getFloorStatistics()
  floorStatistics.value = response.data || []
}

const loadCurrentFloorRooms = async () => {
  const api = getQueryApi()
  const response = await api.getRoomsByFloor(currentFloor.value)
  currentFloorRooms.value = (response.data || []).map(normalizeRoom)
}

const loadSelectedRoomDetail = async (roomId) => {
  if (!roomId) return
  detailLoading.value = true
  try {
    const api = getQueryApi()
    const [detailResponse, logsResponse] = await Promise.all([
      api.getRoomDetail(roomId),
      api.getRoomStatusLogs(roomId)
    ])
    selectedRoom.value = normalizeRoom(detailResponse.data || {})
    syncRoomMetaForm(selectedRoom.value)
    roomLogs.value = (logsResponse.data || []).map(normalizeLog)
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '房间详情加载失败'))
  } finally {
    detailLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    await Promise.all([loadStatistics(), loadCurrentFloorRooms()])
    if (detailVisible.value && selectedRoom.value?.id) {
      await loadSelectedRoomDetail(selectedRoom.value.id)
    }
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, '房态数据加载失败')
    currentFloorRooms.value = []
  } finally {
    loading.value = false
  }
}

const onFloorChange = async () => {
  await loadCurrentFloorRooms()
}

const resetFilters = () => {
  currentFloor.value = 3
  statusFilter.value = ''
  roomTypeFilter.value = ''
  roomNoKeyword.value = ''
  loadData()
}

const openRoomDetail = async (room) => {
  selectedRoom.value = normalizeRoom(room)
  syncRoomMetaForm(selectedRoom.value)
  detailVisible.value = true
  await loadSelectedRoomDetail(selectedRoom.value.id)
}

const uploadCadImage = async (options) => {
  cadUploading.value = true
  try {
    const response = await uploadImage(options.file)
    if (response.code !== 'SUCCESS') throw new Error(response.message || '上传失败')
    const url = response?.data?.url
    if (!url) throw new Error('上传结果缺少图片地址')
    roomMetaForm.value.cadImageUrl = url
    selectedRoom.value = {
      ...selectedRoom.value,
      cadImageUrl: url
    }
    options.onSuccess?.(response)
    ElMessage.success('CAD 图片已上传，请保存房间资料')
  } catch (error) {
    options.onError?.(error)
    ElMessage.error(extractErrorMessage(error, 'CAD 图片上传失败'))
  } finally {
    cadUploading.value = false
  }
}

const saveRoomMetadata = async () => {
  if (!selectedRoom.value?.id) return
  metaSaving.value = true
  try {
    await updateRoom(selectedRoom.value.id, {
      roomTypeId: selectedRoom.value.roomTypeId,
      remark: selectedRoom.value.remark || undefined,
      nearElevator: roomMetaForm.value.nearElevator,
      headRoom: roomMetaForm.value.headRoom,
      tailRoom: roomMetaForm.value.tailRoom,
      cadImageUrl: roomMetaForm.value.cadImageUrl || undefined
    })
    await refreshAfterMutation('房间资料已保存')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '房间资料保存失败'))
  } finally {
    metaSaving.value = false
  }
}

const refreshAfterMutation = async (successMessage) => {
  await Promise.all([loadStatistics(), loadCurrentFloorRooms()])
  if (selectedRoom.value?.id) {
    await loadSelectedRoomDetail(selectedRoom.value.id)
  }
  ElMessage.success(successMessage)
}

const updateRoomStatusWithApi = async (targetStatus, reason = '') => {
  await changeRoomStatus(selectedRoom.value.id, {
    targetStatus,
    reason: reason || undefined
  })
}

const handleMarkMaintenance = () => {
  maintenanceForm.value = { reason: '' }
  maintenanceDialogVisible.value = true
}

const confirmMarkMaintenance = async () => {
  if (!maintenanceForm.value.reason) {
    ElMessage.warning('请输入维修原因')
    return
  }
  submitting.value = true
  try {
    await updateRoomStatusWithApi('MAINTENANCE', maintenanceForm.value.reason)
    maintenanceDialogVisible.value = false
    await refreshAfterMutation('已标记为维修状态')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '标记维修失败'))
  } finally {
    submitting.value = false
  }
}

const handleMarkDisabled = async () => {
  try {
    await ElMessageBox.confirm('确定要停用该房间吗？', '停用确认', { type: 'warning' })
    await updateRoomStatusWithApi('DISABLED', '管理员停用')
    await refreshAfterMutation('房间已停用')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(extractErrorMessage(error, '停用房间失败'))
    }
  }
}

const handleMaintenanceComplete = async () => {
  try {
    await ElMessageBox.confirm('维修已完成，将房间恢复为空闲状态？', '维修完成确认', { type: 'success' })
    await updateRoomStatusWithApi('AVAILABLE', '维修完成')
    await refreshAfterMutation('房间已恢复为空闲状态')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(extractErrorMessage(error, '维修恢复失败'))
    }
  }
}

const handleRestoreAvailable = async () => {
  try {
    await ElMessageBox.confirm('确定要恢复该房间的使用吗？', '恢复确认', { type: 'success' })
    await updateRoomStatusWithApi('AVAILABLE', '恢复使用')
    await refreshAfterMutation('房间已恢复使用')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(extractErrorMessage(error, '恢复使用失败'))
    }
  }
}

const handleCleaningComplete = () => {
  cleaningForm.value = { remark: '' }
  cleaningDialogVisible.value = true
}

const confirmCleaningComplete = async () => {
  submitting.value = true
  try {
    await completeCleaning(selectedRoom.value.id, {
      requestToken: createRequestToken('CLEAN'),
      remark: cleaningForm.value.remark || undefined
    })
    cleaningDialogVisible.value = false
    await refreshAfterMutation('清洁完成，房间已恢复为空闲状态')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '清洁完成失败'))
  } finally {
    submitting.value = false
  }
}

const handleReportIssue = () => {
  issueForm.value = { reason: '' }
  issueDialogVisible.value = true
}

const confirmReportIssue = async () => {
  if (!issueForm.value.reason) {
    ElMessage.warning('请描述发现的问题')
    return
  }
  submitting.value = true
  try {
    await reportCleaningIssue(selectedRoom.value.id, {
      requestToken: createRequestToken('ISSUE'),
      reason: issueForm.value.reason,
      remark: '清洁异常转维修'
    })
    issueDialogVisible.value = false
    await refreshAfterMutation('已上报问题，房间转为维修状态')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '转维修失败'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.room-map-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header h1 {
  margin: 0 0 6px;
  font-size: 28px;
  color: #1f2937;
}

.page-header p {
  margin: 0;
  color: #6b7280;
}

.data-source {
  padding: 8px 14px;
  border-radius: 20px;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 12px;
}

.data-source.mock {
  background: #fef3c7;
  color: #b45309;
}

.page-alert {
  margin-bottom: 4px;
}

.stats-card,
.filter-card,
.rooms-card {
  border-radius: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  border-radius: 12px;
  background: #f9fafb;
}

.stat-item.available {
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
}

.stat-item.occupied {
  background: linear-gradient(135deg, #eef2ff, #c7d2fe);
}

.stat-item.reserved {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
}

.stat-item.dirty {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
}

.stat-item.maintenance {
  background: linear-gradient(135deg, #fef2f2, #fecaca);
}

.stat-item.disabled {
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
}

.stat-label {
  display: block;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.filter-controls {
  flex-wrap: wrap;
}

.filter-label {
  font-size: 14px;
  color: #4b5563;
  white-space: nowrap;
}

.spacer {
  flex: 1;
}

.floor-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.floor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.floor-count {
  font-size: 13px;
  color: #6b7280;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  font-size: 14px;
}

.detail-section,
.status-actions,
.status-logs {
  margin-top: 24px;
}

.role-alert {
  margin-top: 12px;
}

.detail-section h4,
.status-actions h4,
.status-logs h4 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.guest-card {
  display: grid;
  gap: 10px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
}

.guest-card > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.detail-label {
  color: #6b7280;
  font-size: 13px;
}

.muted-text {
  color: #9ca3af;
  font-size: 13px;
}

.feature-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.room-meta-form {
  margin-top: 12px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
}

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-title-row h4 {
  margin-bottom: 0;
}

.cad-preview {
  width: 100%;
  height: 260px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.cad-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 140px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  color: #94a3b8;
  font-size: 13px;
  background: #f8fafc;
}

.save-meta-button {
  width: 100%;
  margin-top: 12px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .page-header,
  .floor-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
