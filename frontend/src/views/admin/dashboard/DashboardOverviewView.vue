<template>
  <div class="dashboard-view">
    <div class="page-header">
      <div>
        <h1>运营数据看板</h1>
        <p>汇总展示今日经营概况、近7日趋势、房型销售和库存预警。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="loadAll">重新加载</el-button>
      </div>
    </div>

    <div class="stats-grid">
      <el-card v-for="card in overviewCards" :key="card.key" shadow="hover" class="stat-card">
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-value">{{ card.value }}</div>
      </el-card>
    </div>

    <div class="content-grid">
      <el-card shadow="never" class="panel-card trend-card">
        <template #header>
          <div class="panel-header">
            <span>近7日订单趋势</span>
            <span class="panel-subtitle">无订单数据时显示为 0</span>
          </div>
        </template>
        <div class="trend-list">
          <div v-for="item in trends" :key="item.date" class="trend-row">
            <div class="trend-date">{{ item.date }}</div>
            <div class="trend-bars">
              <div class="bar-group">
                <div class="bar-label">订单</div>
                <div class="bar-track">
                  <div class="bar-fill orders" :style="{ width: getOrderBarWidth(item.orderCount) }"></div>
                </div>
                <div class="bar-value">{{ item.orderCount }}</div>
              </div>
              <div class="bar-group">
                <div class="bar-label">营收</div>
                <div class="bar-track">
                  <div class="bar-fill revenue" :style="{ width: getRevenueBarWidth(item.revenue) }"></div>
                </div>
                <div class="bar-value">￥{{ formatMoney(item.revenue) }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="panel-card inventory-card">
        <template #header>
          <div class="panel-header">
            <span>库存预警</span>
            <span class="panel-subtitle">当前窗口内可售库存较低的房型日期</span>
          </div>
        </template>
        <el-table :data="inventoryWarnings" stripe empty-text="暂无库存预警">
          <el-table-column prop="roomTypeName" label="房型" min-width="140" />
          <el-table-column prop="inventoryDate" label="日期" min-width="120" />
          <el-table-column label="可售/总房量" min-width="120">
            <template #default="scope">
              {{ scope.row.availableRooms }} / {{ scope.row.totalRooms }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" />
        </el-table>
      </el-card>
    </div>

    <div class="content-grid two-columns">
      <el-card shadow="never" class="panel-card">
        <template #header>
          <div class="panel-header">
            <span>房型销售统计</span>
            <span class="panel-subtitle">订单模块未接入前，展示 0 值基础视图</span>
          </div>
        </template>
        <el-table :data="roomTypeStatistics" stripe>
          <el-table-column prop="roomTypeName" label="房型" min-width="160" />
          <el-table-column prop="bookingCount" label="预订数量" width="120" />
          <el-table-column label="营业收入" min-width="120">
            <template #default="scope">
              ￥{{ formatMoney(scope.row.revenue) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="panel-card">
        <template #header>
          <div class="panel-header">
            <span>内容运营统计</span>
            <span class="panel-subtitle">已发布内容与启用参数</span>
          </div>
        </template>
        <div class="content-stats">
          <div class="content-stat-item">
            <span>已发布公告</span>
            <strong>{{ overview.publishedAnnouncementCount }}</strong>
          </div>
          <div class="content-stat-item">
            <span>已上线轮播图</span>
            <strong>{{ overview.onlineBannerCount }}</strong>
          </div>
          <div class="content-stat-item">
            <span>启用系统参数</span>
            <strong>{{ overview.enabledSystemConfigCount }}</strong>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getDashboardOverview,
  getDashboardTrends,
  getInventoryWarnings,
  getRoomTypeStatistics
} from '@/api/dashboard'

const loading = ref(false)
const trends = ref([])
const inventoryWarnings = ref([])
const roomTypeStatistics = ref([])

const overview = reactive({
  todayOrderCount: 0,
  todayRevenue: 0,
  todayCheckInCount: 0,
  todayCheckOutCount: 0,
  availableRoomCount: 0,
  inventoryWarningCount: 0,
  publishedAnnouncementCount: 0,
  onlineBannerCount: 0,
  enabledSystemConfigCount: 0
})

function extractResult(result, fallbackMessage) {
  if (result.code !== 'SUCCESS') {
    throw new Error(result.message || fallbackMessage)
  }
  return result.data
}

const overviewCards = computed(() => [
  { key: 'todayOrderCount', label: '今日新增订单数', value: overview.todayOrderCount },
  { key: 'todayRevenue', label: '今日营业额', value: `￥${formatMoney(overview.todayRevenue)}` },
  { key: 'todayCheckInCount', label: '今日入住数', value: overview.todayCheckInCount },
  { key: 'todayCheckOutCount', label: '今日退房数', value: overview.todayCheckOutCount },
  { key: 'availableRoomCount', label: '当前可售房间数', value: overview.availableRoomCount },
  { key: 'inventoryWarningCount', label: '库存预警房型数', value: overview.inventoryWarningCount }
])

const maxOrderCount = computed(() => Math.max(...trends.value.map(item => Number(item.orderCount || 0)), 1))
const maxRevenue = computed(() => Math.max(...trends.value.map(item => Number(item.revenue || 0)), 1))

function getOrderBarWidth(value) {
  return `${Math.max(8, (Number(value || 0) / maxOrderCount.value) * 100)}%`
}

function getRevenueBarWidth(value) {
  return `${Math.max(8, (Number(value || 0) / maxRevenue.value) * 100)}%`
}

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

async function loadAll() {
  loading.value = true
  try {
    const [overviewResult, trendsResult, statsResult, warningsResult] = await Promise.all([
      getDashboardOverview(),
      getDashboardTrends({ days: 7 }),
      getRoomTypeStatistics(),
      getInventoryWarnings()
    ])

    Object.assign(overview, extractResult(overviewResult, '加载概览失败'))
    trends.value = extractResult(trendsResult, '加载趋势失败') || []
    roomTypeStatistics.value = extractResult(statsResult, '加载房型统计失败') || []
    inventoryWarnings.value = extractResult(warningsResult, '加载库存预警失败') || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载运营看板失败')
  } finally {
    loading.value = false
  }
}

loadAll()
</script>

<style scoped>
.dashboard-view {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 20px;
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
}

.stat-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.content-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 20px;
}

.two-columns {
  grid-template-columns: 1fr 1fr;
}

.panel-card {
  border-radius: 20px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-subtitle {
  color: #6b7280;
  font-size: 13px;
}

.trend-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.trend-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 16px;
  align-items: center;
}

.trend-date {
  font-weight: 600;
  color: #1f2937;
}

.trend-bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bar-group {
  display: grid;
  grid-template-columns: 44px 1fr 90px;
  gap: 10px;
  align-items: center;
}

.bar-label,
.bar-value {
  color: #475569;
  font-size: 13px;
}

.bar-track {
  height: 10px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 999px;
}

.bar-fill.orders {
  background: linear-gradient(90deg, #0ea5e9, #0284c7);
}

.bar-fill.revenue {
  background: linear-gradient(90deg, #f59e0b, #ea580c);
}

.content-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fafc, #eef2ff);
}

.content-stat-item strong {
  font-size: 26px;
  color: #111827;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .content-grid,
  .two-columns {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .trend-row,
  .bar-group {
    grid-template-columns: 1fr;
  }
}
</style>