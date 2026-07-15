<template>
  <div class="daily-price-calendar-view">
    <div class="page-header">
      <div>
        <h1>Daily Price Calendar</h1>
        <p>Query generated daily prices by room type and date range, or generate a full pricing window in one click.</p>
      </div>
      <div class="header-actions">
        <el-button :loading="generating" type="primary" @click="handleGenerate">Generate Prices</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form label-position="top" class="filter-form">
        <div class="filter-grid">
          <el-form-item label="Room Type">
            <el-select v-model="filters.roomTypeId" placeholder="Select room type" class="full-width">
              <el-option v-for="roomType in roomTypes" :key="roomType.id" :label="roomType.name" :value="roomType.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="Start Date">
            <el-date-picker v-model="filters.startDate" type="date" value-format="YYYY-MM-DD" class="full-width" />
          </el-form-item>
          <el-form-item label="End Date">
            <el-date-picker v-model="filters.endDate" type="date" value-format="YYYY-MM-DD" class="full-width" />
          </el-form-item>
        </div>
        <div class="filter-actions">
          <el-button type="primary" :loading="loading" @click="loadDailyPrices">Query</el-button>
          <el-button @click="resetFilters">Reset</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="dailyPrices" v-loading="loading" stripe>
        <el-table-column prop="businessDate" label="Date" width="120" />
        <el-table-column label="Weekday" width="120">
          <template #default="scope">{{ getWeekday(scope.row.businessDate) }}</template>
        </el-table-column>
        <el-table-column label="Base Price" width="130">
          <template #default="scope">{{ currency(scope.row.basePrice) }}</template>
        </el-table-column>
        <el-table-column prop="dateCoefficient" label="Coef." width="90" />
        <el-table-column label="Final Price" width="130">
          <template #default="scope">
            <span class="price-text">{{ currency(scope.row.finalPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="Source" width="170">
          <template #default="scope">
            <el-tag :type="resolveSourceTagType(scope.row)">
              {{ scope.row.priceSource }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priceDescription" label="Description" min-width="220" show-overflow-tooltip />
        <el-table-column label="Status" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.sourceStatus === 'OFFICIAL' ? 'success' : scope.row.sourceStatus === 'PROVISIONAL' ? 'warning' : 'info'">
              {{ scope.row.sourceStatus }}
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

const loading = ref(false)
const generating = ref(false)
const roomTypes = ref([])
const dailyPrices = ref([])

const defaultFilters = () => ({
  roomTypeId: null,
  startDate: '2026-07-17',
  endDate: '2027-07-18'
})

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
    const data = extractResult(result, 'Failed to load room types')
    roomTypes.value = data.records || []
    if (!filters.roomTypeId && roomTypes.value.length > 0) {
      filters.roomTypeId = roomTypes.value[0].id
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to load room types')
  }
}

async function loadDailyPrices() {
  if (!filters.roomTypeId || !filters.startDate || !filters.endDate) {
    ElMessage.warning('Please select room type and date range first')
    return
  }
  loading.value = true
  try {
    const result = await getDailyPrices({
      roomTypeId: filters.roomTypeId,
      startDate: filters.startDate,
      endDate: filters.endDate
    })
    dailyPrices.value = extractResult(result, 'Failed to load daily prices')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to load daily prices')
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
    const count = extractResult(result, 'Failed to generate daily prices')
    ElMessage.success(`Generated ${count} daily price records`)
    await loadDailyPrices()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to generate daily prices')
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
  return ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][date.getDay()]
}

function formatPrice(value) {
  return Number(value || 0).toFixed(2)
}

function currency(value) {
  return `CNY ${formatPrice(value)}`
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
