
<template>
  <div class="base-price-view">
    <div class="page-header">
      <div>
        <h1>基础价格管理</h1>
        <p>统一维护各房型的基础价格，并记录每一次有效调价历史。</p>
      </div>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索房型名称"
          clearable
          class="filter-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="statusFilter" placeholder="全部状态" class="filter-select" @change="handleSearch">
          <el-option label="全部状态" value="" />
          <el-option label="已上线" value="ONLINE" />
          <el-option label="已下线" value="OFFLINE" />
        </el-select>
        <el-button @click="handleSearch">查询</el-button>
        <el-button :loading="loading" @click="loadBasePrices">重新加载</el-button>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="roomTypes" v-loading="loading" stripe>
        <el-table-column prop="name" label="房型名称" min-width="180" />
        <el-table-column prop="category" label="房型类别" min-width="140" />
        <el-table-column label="当前基础价格" width="150">
          <template #default="scope">
            <span class="price-text">￥{{ formatPrice(scope.row.basePrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ONLINE' ? 'success' : 'info'">
              {{ scope.row.status === 'ONLINE' ? '已上线' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可住人数" width="100">
          <template #default="scope">
            {{ scope.row.maxGuests || '-' }} 人
          </template>
        </el-table-column>
        <el-table-column prop="bedType" label="床型" min-width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(scope.row)">修改价格</el-button>
              <el-button link type="info" @click="openLogDrawer(scope.row)">查看日志</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="修改基础价格" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="房型名称">
          <el-input :model-value="currentRoomType?.name || ''" disabled />
        </el-form-item>

        <el-form-item label="当前基础价格">
          <el-input :model-value="currentRoomType ? `￥${formatPrice(currentRoomType.basePrice)}` : ''" disabled />
        </el-form-item>

        <el-form-item label="新基础价格" prop="basePrice">
          <el-input-number
            v-model="form.basePrice"
            :min="0.01"
            :step="0.01"
            :precision="2"
            class="full-width"
          />
        </el-form-item>

        <el-form-item label="修改原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入本次调价原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-drawer v-model="logDrawerVisible" size="760px" destroy-on-close>
      <template #header>
        <div>
          <h3 class="drawer-title">价格操作日志</h3>
          <p class="drawer-subtitle">
            {{ currentRoomType?.name ? `${currentRoomType.name} 的调价记录` : '全部房型的调价记录' }}
          </p>
        </div>
      </template>

      <div class="drawer-actions">
        <el-button :loading="logLoading" @click="loadLogs">重新加载</el-button>
      </div>

      <el-table :data="logs" v-loading="logLoading" stripe>
        <el-table-column prop="roomTypeName" label="房型名称" min-width="150" />
        <el-table-column label="价格变化" min-width="180">
          <template #default="scope">
            <span>￥{{ formatPrice(scope.row.oldPrice) }} → ￥{{ formatPrice(scope.row.newPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="changeReason" label="修改原因" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作人" width="140">
          <template #default="scope">
            {{ scope.row.operatorName || '待接入登录用户' }}
          </template>
        </el-table-column>
        <el-table-column label="操作时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="logPagination.page"
          :page-size="logPagination.size"
          :total="logPagination.total"
          @current-change="handleLogPageChange"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getBasePriceLogs,
  getBasePrices,
  updateBasePrice
} from '@/api/pricing/basePrice'

const loading = ref(false)
const submitting = ref(false)
const logLoading = ref(false)
const dialogVisible = ref(false)
const logDrawerVisible = ref(false)
const formRef = ref()
const roomTypes = ref([])
const logs = ref([])
const currentRoomType = ref(null)
const keyword = ref('')
const statusFilter = ref('')

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const logPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  basePrice: 0.01,
  reason: ''
})

const rules = {
  basePrice: [
    { required: true, message: '请输入新的基础价格', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入修改原因', trigger: 'blur' },
    { max: 200, message: '修改原因不能超过200个字符', trigger: 'blur' }
  ]
}

function extractResult(result, fallbackMessage) {
  if (result.code !== 'SUCCESS') {
    throw new Error(result.message || fallbackMessage)
  }
  return result.data
}

function resetForm() {
  form.basePrice = 0.01
  form.reason = ''
}

async function loadBasePrices() {
  loading.value = true
  try {
    const result = await getBasePrices({
      page: pagination.page,
      size: pagination.size,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined
    })
    const data = extractResult(result, '加载基础价格失败')
    roomTypes.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载基础价格失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadBasePrices()
}

function handlePageChange(page) {
  pagination.page = page
  loadBasePrices()
}

function openEditDialog(row) {
  currentRoomType.value = row
  resetForm()
  form.basePrice = Number(row.basePrice || 0)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || !currentRoomType.value) {
    return
  }

  submitting.value = true
  try {
    const result = await updateBasePrice(currentRoomType.value.id, {
      basePrice: Number(form.basePrice).toFixed(2),
      reason: form.reason.trim()
    })
    const updated = extractResult(result, '保存基础价格失败')
    ElMessage.success('基础价格保存成功')
    dialogVisible.value = false
    currentRoomType.value = updated
    await loadBasePrices()
    if (logDrawerVisible.value) {
      await loadLogs()
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '保存基础价格失败')
  } finally {
    submitting.value = false
  }
}

function openLogDrawer(row) {
  currentRoomType.value = row || null
  logPagination.page = 1
  logDrawerVisible.value = true
  loadLogs()
}

async function loadLogs() {
  logLoading.value = true
  try {
    const result = await getBasePriceLogs({
      page: logPagination.page,
      size: logPagination.size,
      roomTypeId: currentRoomType.value?.id
    })
    const data = extractResult(result, '加载价格日志失败')
    logs.value = data.records || []
    logPagination.total = data.total || 0
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载价格日志失败')
  } finally {
    logLoading.value = false
  }
}

function handleLogPageChange(page) {
  logPagination.page = page
  loadLogs()
}

function formatPrice(value) {
  const number = Number(value || 0)
  return number.toFixed(2)
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

loadBasePrices()
</script>

<style scoped>
.base-price-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  color: #1f2937;
}

.page-header p {
  margin: 0;
  color: #6b7280;
}

.header-actions,
.dialog-actions,
.table-actions,
.drawer-actions {
  display: flex;
  gap: 12px;
}

.header-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.filter-input {
  width: 220px;
}

.filter-select {
  width: 160px;
}

.table-card {
  border-radius: 20px;
}

.price-text {
  font-weight: 600;
  color: #b45309;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.full-width {
  width: 100%;
}

.drawer-title {
  margin: 0;
  font-size: 22px;
  color: #111827;
}

.drawer-subtitle {
  margin: 6px 0 0;
  color: #6b7280;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .header-actions,
  .filter-input,
  .filter-select {
    width: 100%;
  }
}
</style>
