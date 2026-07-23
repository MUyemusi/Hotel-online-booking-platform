
<template>
  <div class="daily-price-calendar-view">
    <div class="page-header">
      <div>
        <h1>每日价格日历</h1>
        <p>按房型和日期范围查询每日价格，也可以一键生成完整价格窗口。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="generating" type="primary" @click="handleGenerate">生成价格</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form label-position="top" class="filter-form">
        <div class="filter-grid">
          <el-form-item label="房型">
            <el-select v-model="filters.roomTypeId" placeholder="请选择房型" class="full-width">
              <el-option v-for="roomType in roomTypes" :key="roomType.id" :label="roomType.name" :value="roomType.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker v-model="filters.startDate" type="date" value-format="YYYY-MM-DD" class="full-width" />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker v-model="filters.endDate" type="date" value-format="YYYY-MM-DD" class="full-width" />
          </el-form-item>
        </div>
        <div class="filter-actions">
          <el-button type="primary" :loading="loading" @click="loadDailyPrices">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="dailyPrices" v-loading="loading" stripe>
        <el-table-column prop="businessDate" label="日期" width="120" />
        <el-table-column label="星期" width="120">
          <template #default="scope">{{ getWeekday(scope.row.businessDate) }}</template>
        </el-table-column>
        <el-table-column label="基础价格" width="130">
          <template #default="scope">{{ currency(scope.row.basePrice) }}</template>
        </el-table-column>
        <el-table-column prop="dateCoefficient" label="系数" width="90" />
        <el-table-column label="最终价格" width="130">
          <template #default="scope">
            <span class="price-text">{{ currency(scope.row.finalPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="价格来源" width="170">
          <template #default="scope">
            <el-tag :type="resolveSourceTagType(scope.row)">
              {{ displayValue('priceSource', scope.row.priceSource) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priceDescription" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="数据状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.sourceStatus === 'OFFICIAL' ? 'success' : scope.row.sourceStatus === 'PROVISIONAL' ? 'warning' : 'info'">
              {{ displayValue('sourceStatus', scope.row.sourceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getBasePrices } from '@/api/pricing/basePrice'
import { generateDailyPrices, getDailyPrices } from '@/api/pricing/dailyPrice'
import { currencyCny, displayValue, weekdayName } from '@/utils/adminDisplay'

const loading = ref(false)
const generating = ref(false)
const roomTypes = ref([])
const dailyPrices = ref([])

const defaultFilters = () => ({
  roomTypeId: null,
  startDate: dateOffset(0),
  endDate: dateOffset(365)
})

function dateOffset(days) { const date = new Date(); date.setDate(date.getDate() + days); return date.toISOString().slice(0, 10) }

const filters = reactive(defaultFilters())

function extractResult(result, fallbackMessage) {
  if (result.code !== 'SUCCESS') {
    throw new Error(result.message || fallbackMessage)
  }
  return result.data
}

async function loadRoomTypes() {
  try {
    const result = await getBasePrices({ page: 1, size: 100 })
    const data = extractResult(result, '加载房型失败')
    roomTypes.value = data.records || []
    if (!filters.roomTypeId && roomTypes.value.length > 0) {
      filters.roomTypeId = roomTypes.value[0].id
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载房型失败')
  }
}

async function loadDailyPrices() {
  if (!filters.roomTypeId || !filters.startDate || !filters.endDate) {
    ElMessage.warning('请先选择房型和日期范围')
    return
  }
  loading.value = true
  try {
    const result = await getDailyPrices({
      roomTypeId: filters.roomTypeId,
      startDate: filters.startDate,
      endDate: filters.endDate
    })
    dailyPrices.value = extractResult(result, '加载每日价格失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载每日价格失败')
  } finally {
    loading.value = false
  }
}

async function handleGenerate() {
  generating.value = true
  try {
    const result = await generateDailyPrices({
      startDate: filters.startDate,
      endDate: filters.endDate
    })
    const count = extractResult(result, '生成每日价格失败')
    ElMessage.success(`已生成 ${count} 条每日价格记录`)
    await loadDailyPrices()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '生成每日价格失败')
  } finally {
    generating.value = false
  }
}

function resetFilters() {
  Object.assign(filters, defaultFilters())
  if (roomTypes.value.length > 0) {
    filters.roomTypeId = roomTypes.value[0].id
  }
  dailyPrices.value = []
}

function getWeekday(dateString) {
  const date = new Date(dateString)
  return weekdayName(dateString)
}

function formatPrice(value) {
  return Number(value || 0).toFixed(2)
}

function currency(value) {
  return currencyCny(value)
}

function resolveSourceTagType(row) {
  if (row.priceSource === 'MAKEUP_WORKDAY') {
    return 'success'
  }
  if (['FRIDAY', 'SATURDAY', 'SUNDAY'].includes(row.priceSource)) {
    return 'primary'
  }
  if (row.sourceStatus === 'PROVISIONAL') {
    return 'warning'
  }
  if (row.sourceStatus === 'OFFICIAL') {
    return 'danger'
  }
  return 'info'
}

onMounted(async () => {
  await loadRoomTypes()
  if (filters.roomTypeId) {
    await loadDailyPrices()
  }
})
</script>

<style scoped>
.daily-price-calendar-view {
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
.filter-actions {
  display: flex;
  gap: 12px;
}

.filter-card,
.table-card {
  border-radius: 20px;
}

.filter-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.full-width {
  width: 100%;
}

.price-text {
  font-weight: 600;
  color: #b45309;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
