<template>
  <div class="inventory-manage-view">
    <div class="page-header">
      <div>
        <h1>每日库存管理</h1>
        <p>按房型 + 日期维度管理库存，支持修改、暂停和恢复销售。</p>
      </div>
    </div>

    <!-- 选择房型 + 日期 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="房型">
          <el-select v-model="filter.roomTypeId" placeholder="选择房型" @change="fetchInventory" style="width:220px;">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filter.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            @change="fetchInventory"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchInventory">查询库存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 库存表格 -->
    <el-card shadow="never" class="table-card" v-if="inventoryList.length">
      <el-table :data="inventoryList" v-loading="loading" stripe>
        <el-table-column prop="inventoryDate" label="日期" width="130" sortable />
        <el-table-column prop="totalRooms" label="总库存" width="100" />
        <el-table-column prop="lockedRooms" label="已锁定" width="100" />
        <el-table-column prop="bookedRooms" label="已预订" width="100" />
        <el-table-column label="可售库存" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.availableRooms > 0 ? 'success' : 'danger'">
              {{ scope.row.availableRooms }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'warning'">
              {{ scope.row.status === 'ACTIVE' ? '正常' : '暂停' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEdit(scope.row)">修改</el-button>
            <el-button
              v-if="scope.row.status === 'ACTIVE'"
              link type="warning"
              @click="handlePause(scope.row.id)"
            >暂停</el-button>
            <el-button
              v-else
              link type="success"
              @click="handleResume(scope.row.id)"
            >恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 库存日历视图 -->
    <el-card shadow="never" class="table-card" v-if="inventoryList.length">
      <template #header>
        <span>库存日历 · {{ filter.dateRange?.[0] || '' }} ~ {{ filter.dateRange?.[1] || '' }}</span>
      </template>
      <div class="calendar-grid">
        <div
          v-for="item in inventoryList"
          :key="item.inventoryDate"
          class="cal-cell"
          :class="{
            'cal-low': item.availableRooms === 0,
            'cal-warn': item.availableRooms > 0 && item.availableRooms <= 2,
            'cal-ok': item.availableRooms > 2,
            'cal-paused': item.status !== 'ACTIVE'
          }"
        >
          <div class="cal-date">{{ formatShortDate(item.inventoryDate) }}</div>
          <div class="cal-count">{{ item.availableRooms }}<small>间</small></div>
          <div class="cal-bar">
            <div class="cal-fill" :style="{ width: fillPercent(item) + '%' }"></div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 修改弹窗 -->
    <el-dialog v-model="editVisible" title="修改库存" width="480px" destroy-on-close>
      <el-form :model="editForm" label-position="top">
        <el-form-item label="日期">
          <span>{{ editForm.inventoryDate }}</span>
        </el-form-item>
        <el-form-item label="总库存">
          <el-input-number v-model="editForm.totalRooms" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="可售库存">
          <el-input-number v-model="editForm.availableRooms" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingEdit" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 库存变更日志 -->
    <el-card shadow="never" class="table-card" v-if="inventoryList.length">
      <template #header>
        <div class="card-header-row">
          <span>库存变更日志</span>
          <el-button size="small" @click="fetchLogs">查看日志</el-button>
        </div>
      </template>
      <el-table :data="logs" v-loading="loadingLogs" stripe max-height="360">
        <el-table-column prop="inventoryDate" label="日期" width="120" />
        <el-table-column prop="changeType" label="操作类型" width="140">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.changeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeAmount" label="变动量" width="90" />
        <el-table-column prop="beforeValue" label="变动前" width="90" />
        <el-table-column prop="afterValue" label="变动后" width="90" />
        <el-table-column prop="changeReason" label="原因" min-width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminRoomTypes } from '@/api/roomtype'
import { getInventory, updateInventory, pauseInventory, resumeInventory, getInventoryLogs } from '@/api/inventory'

const loading = ref(false)
const roomTypes = ref([])
const inventoryList = ref([])

const filter = reactive({
  roomTypeId: null,
  dateRange: null
})

onMounted(async () => {
  try {
    const res = await getAdminRoomTypes({ page: 1, size: 100, status: 'ONLINE' })
    if (res.code === 'SUCCESS') roomTypes.value = res.data?.records || []
  } catch (e) { /* ignore */ }
})

function extractResult(res, msg) {
  if (res.code !== 'SUCCESS') throw new Error(res.message || msg)
  return res.data
}

async function fetchInventory() {
  if (!filter.roomTypeId || !filter.dateRange) {
    ElMessage.warning('请选择房型和日期范围')
    return
  }
  loading.value = true
  try {
    inventoryList.value = extractResult(await getInventory({
      roomTypeId: filter.roomTypeId,
      startDate: filter.dateRange[0],
      endDate: filter.dateRange[1]
    }), '查询失败') || []
  } catch (e) { ElMessage.error(e.message) } finally { loading.value = false }
}

// 编辑
const editVisible = ref(false)
const savingEdit = ref(false)
const editForm = reactive({ id: null, inventoryDate: '', totalRooms: 0, availableRooms: 0 })

function openEdit(row) {
  editForm.id = row.id
  editForm.inventoryDate = row.inventoryDate
  editForm.totalRooms = row.totalRooms
  editForm.availableRooms = row.availableRooms
  editVisible.value = true
}

async function saveEdit() {
  savingEdit.value = true
  try {
    await extractResult(await updateInventory(editForm.id, {
      totalRooms: editForm.totalRooms, availableRooms: editForm.availableRooms
    }), '修改失败')
    ElMessage.success('库存已更新')
    editVisible.value = false
    fetchInventory()
  } catch (e) { ElMessage.error(e.message) } finally { savingEdit.value = false }
}

async function handlePause(id) {
  try {
    await extractResult(await pauseInventory(id), '操作失败')
    ElMessage.success('已暂停')
    fetchInventory()
  } catch (e) { ElMessage.error(e.message) }
}

async function handleResume(id) {
  try {
    await extractResult(await resumeInventory(id), '操作失败')
    ElMessage.success('已恢复')
    fetchInventory()
  } catch (e) { ElMessage.error(e.message) }
}

// 日志
const logs = ref([])
const loadingLogs = ref(false)

async function fetchLogs() {
  if (!filter.roomTypeId || !filter.dateRange) return
  loadingLogs.value = true
  try {
    logs.value = extractResult(await getInventoryLogs({
      roomTypeId: filter.roomTypeId,
      startDate: filter.dateRange[0],
      endDate: filter.dateRange[1]
    }), '日志查询失败') || []
  } catch (e) { ElMessage.error(e.message) } finally { loadingLogs.value = false }
}

// 工具
function formatShortDate(d) {
  if (!d) return ''
  return d.slice(5) // MM-DD
}

function fillPercent(item) {
  if (!item.totalRooms || item.totalRooms === 0) return 0
  return Math.round(item.availableRooms / item.totalRooms * 100)
}
</script>

<style scoped>
.inventory-manage-view { display: flex; flex-direction: column; gap: 20px; }

.page-header h1 { margin: 0 0 8px; font-size: 28px; color: #1f2937; }
.page-header p { margin: 0; color: #6b7280; }
.filter-card, .table-card { border-radius: 20px; }

.card-header-row { display: flex; justify-content: space-between; align-items: center; }

/* 日历视图 */
.calendar-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 10px; }

.cal-cell { padding: 12px 10px; border-radius: 12px; text-align: center; background: #f0fdf4; border: 1px solid #bbf7d0; }
.cal-cell.cal-low { background: #fef2f2; border-color: #fecaca; }
.cal-cell.cal-warn { background: #fffbeb; border-color: #fde68a; }
.cal-cell.cal-paused { background: #f3f4f6; border-color: #d1d5db; opacity: 0.6; }

.cal-date { font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.cal-count { font-size: 22px; font-weight: 700; color: #1f2937; }
.cal-count small { font-size: 12px; font-weight: 400; color: #9ca3af; }

.cal-bar { height: 4px; background: #e5e7eb; border-radius: 4px; margin-top: 8px; overflow: hidden; }
.cal-fill { height: 100%; background: #22c55e; border-radius: 4px; transition: width 0.3s; }
.cal-low .cal-fill { background: #ef4444; }
.cal-warn .cal-fill { background: #f59e0b; }

@media (max-width: 768px) {
  .calendar-grid { grid-template-columns: repeat(4, 1fr); }
}
</style>
