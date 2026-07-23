

<template>
  <div class="inventory-manage-view">
    <div class="page-header">
      <div>
        <h1>每日库存管理</h1>
        <p>按房型 + 日期维度管理库存，支持单条/批量修改、自动生成、暂停和恢复销售。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="fetchInventory">刷新</el-button>
        <el-button type="warning" @click="openBatchEdit" :disabled="!inventoryList.length">批量修改</el-button>
        <el-button type="success" @click="openGenerate">生成库存</el-button>
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

    <!-- 单条修改弹窗 -->
    <el-dialog v-model="editVisible" title="修改库存" width="480px" destroy-on-close>
      <el-form :model="editForm" label-position="top">
        <el-form-item label="日期">
          <span>{{ editForm.inventoryDate }}</span>
        </el-form-item>
        <el-form-item label="总库存">
          <el-input-number v-model="editForm.totalRooms" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-alert
          v-if="editForm.occupied > 0"
          title="当前已锁定 + 已预订 = {{ editForm.occupied }}，总库存不可低于此值"
          type="warning" :closable="false" show-icon
          style="margin-top:8px;"
        />
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingEdit" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量修改弹窗 -->
    <el-dialog v-model="batchVisible" title="批量修改库存" width="520px" destroy-on-close>
      <el-form :model="batchForm" label-position="top">
        <el-form-item label="日期范围">
          <span>{{ filter.dateRange?.[0] || '—' }} ~ {{ filter.dateRange?.[1] || '—' }}</span>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="新总库存">
              <el-input-number v-model="batchForm.totalRooms" :min="0" controls-position="right" style="width:100%" placeholder="留空则不修改" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新状态">
              <el-select v-model="batchForm.status" placeholder="留空则不修改" clearable style="width:100%">
                <el-option label="正常" value="ACTIVE" />
                <el-option label="暂停" value="PAUSED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert
          title="批量修改会自动跳过不满足校验的日期（如总库存低于已占用），并在结果中提示。"
          type="info" :closable="false" show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingBatch" @click="saveBatch">应用批量修改</el-button>
      </template>
    </el-dialog>

    <!-- 生成库存弹窗 -->
    <el-dialog v-model="genVisible" title="自动生成库存" width="480px" destroy-on-close>
      <el-form :model="genForm" label-position="top">
        <el-form-item label="生成天数">
          <el-input-number v-model="genForm.days" :min="1" :max="90" controls-position="right" style="width:100%" />
          <span style="color:#9ca3af;font-size:12px;">从今天起自动创建未来 N 天的库存记录（已存在的日期不会重复创建）</span>
        </el-form-item>
        <el-form-item label="默认总库存">
          <el-input-number v-model="genForm.totalRooms" :min="1" :max="9999" controls-position="right" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingGen" @click="saveGen">
          {{ filter.roomTypeId ? '为当前房型生成' : '为全部房型生成' }}
        </el-button>
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
            <el-tag size="small">{{ displayValue('inventoryChange', scope.row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeAmount" label="变动量" width="90" />
        <el-table-column prop="beforeValue" label="变动前" width="90" />
        <el-table-column prop="afterValue" label="变动后" width="90" />
        <el-table-column label="原因" min-width="160"><template #default="{ row }">{{ displayValue('inventoryReason', row.changeReason) }}</template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { displayValue } from '@/utils/adminDisplay'
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminRoomTypes } from '@/api/roomtype'
import { getInventory, updateInventory, batchUpdateInventory, generateInventory, pauseInventory, resumeInventory, getInventoryLogs } from '@/api/inventory'

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

// ===== 单条编辑 =====
const editVisible = ref(false)
const savingEdit = ref(false)
const editForm = reactive({ id: null, inventoryDate: '', totalRooms: 0, occupied: 0 })

function openEdit(row) {
  editForm.id = row.id
  editForm.inventoryDate = row.inventoryDate
  editForm.totalRooms = row.totalRooms
  editForm.occupied = (row.lockedRooms || 0) + (row.bookedRooms || 0)
  editVisible.value = true
}

async function saveEdit() {
  savingEdit.value = true
  try {
    await extractResult(await updateInventory(editForm.id, {
      totalRooms: editForm.totalRooms
    }), '修改失败')
    ElMessage.success('库存已更新')
    editVisible.value = false
    fetchInventory()
  } catch (e) { ElMessage.error(e.message) } finally { savingEdit.value = false }
}

// ===== 批量修改 =====
const batchVisible = ref(false)
const savingBatch = ref(false)
const batchForm = reactive({ totalRooms: null, status: '' })

function openBatchEdit() {
  if (!inventoryList.value.length) return
  batchForm.totalRooms = null
  batchForm.status = ''
  batchVisible.value = true
}

async function saveBatch() {
  if (!filter.roomTypeId || !filter.dateRange) return
  savingBatch.value = true
  try {
    const data = extractResult(await batchUpdateInventory({
      roomTypeId: filter.roomTypeId,
      startDate: filter.dateRange[0],
      endDate: filter.dateRange[1],
      totalRooms: batchForm.totalRooms || undefined,
      status: batchForm.status || undefined
    }), '批量修改失败')
    const msg = `成功更新 ${data.updated} 条`
    if (data.skipped > 0) {
      ElMessage.warning(msg + `，跳过 ${data.skipped} 条（不满足校验）`)
      console.warn('跳过的日期:', data.skipReasons)
    } else {
      ElMessage.success(msg)
    }
    batchVisible.value = false
    fetchInventory()
  } catch (e) { ElMessage.error(e.message) } finally { savingBatch.value = false }
}

// ===== 自动生成库存 =====
const genVisible = ref(false)
const savingGen = ref(false)
const genForm = reactive({ days: 15, totalRooms: 10 })

function openGenerate() {
  genForm.days = 15
  genForm.totalRooms = 10
  genVisible.value = true
}

async function saveGen() {
  savingGen.value = true
  try {
    const params = {
      days: genForm.days,
      totalRooms: genForm.totalRooms
    }
    if (filter.roomTypeId) {
      params.roomTypeId = filter.roomTypeId
    }
    await extractResult(await generateInventory(params), '生成失败')
    ElMessage.success('库存生成成功（已有记录会自动跳过）')
    genVisible.value = false
    if (inventoryList.value.length) fetchInventory()
  } catch (e) { ElMessage.error(e.message) } finally { savingGen.value = false }
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
  return d.slice(5)
}

function fillPercent(item) {
  if (!item.totalRooms || item.totalRooms === 0) return 0
  return Math.round(item.availableRooms / item.totalRooms * 100)
}
</script>

<style scoped>
.inventory-manage-view { display: flex; flex-direction: column; gap: 20px; }

.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.page-header h1 { margin: 0 0 8px; font-size: 28px; color: #1f2937; }
.page-header p { margin: 0; color: #6b7280; }
.header-actions { display: flex; gap: 8px; flex-wrap: wrap; }

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
  .page-header { flex-direction: column; }
  .calendar-grid { grid-template-columns: repeat(4, 1fr); }
}
</style>
