<template>
  <div class="dashboard-view">
    <div class="page-header">
      <div><h1>酒店经营数据看板</h1><p>从支付流水、订单、房型和库存维度观察酒店经营情况。</p></div>
      <div class="header-actions">
        <el-radio-group v-model="periodDays" @change="loadAll">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
          <el-radio-button :value="90">近90天</el-radio-button>
        </el-radio-group>
        <el-button :loading="loading" @click="loadAll">重新加载</el-button>
      </div>
    </div>

    <div class="stats-grid">
      <el-card v-for="card in overviewCards" :key="card.key" shadow="hover" class="stat-card">
        <span>{{ card.label }}</span><strong :class="card.className">{{ card.value }}</strong><small>{{ card.hint }}</small>
      </el-card>
    </div>

    <div class="chart-grid">
      <el-card shadow="never" class="panel-card wide"><template #header><b>营收与订单趋势</b></template><div ref="trendRef" class="chart"></div></el-card>
      <el-card shadow="never" class="panel-card"><template #header><b>当前入住率</b></template><div ref="occupancyRef" class="chart"></div></el-card>
      <el-card shadow="never" class="panel-card"><template #header><b>房型营收排行</b></template><div ref="roomTypeRef" class="chart"></div></el-card>
      <el-card shadow="never" class="panel-card"><template #header><b>支付方式占比</b></template><div ref="paymentRef" class="chart"></div></el-card>
      <el-card shadow="never" class="panel-card"><template #header><b>订单状态分布</b></template><div ref="statusRef" class="chart"></div></el-card>
    </div>

    <div class="content-grid">
      <el-card shadow="never" class="panel-card">
        <template #header><div class="panel-header"><b>未来7天库存预警</b><span>阈值由系统参数控制</span></div></template>
        <el-table :data="inventoryWarnings" stripe empty-text="暂无库存预警">
          <el-table-column prop="roomTypeName" label="房型" min-width="140" />
          <el-table-column prop="inventoryDate" label="日期" width="120" />
          <el-table-column label="可售/总房量" width="130"><template #default="{ row }">{{ row.availableRooms }} / {{ row.totalRooms }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="{ row }">{{ displayValue('status', row.status) }}</template></el-table-column>
        </el-table>
      </el-card>
      <el-card shadow="never" class="panel-card">
        <template #header><b>内容运营概况</b></template>
        <div class="content-stats">
          <div><span>已发布公告</span><strong>{{ overview.publishedAnnouncementCount }}</strong></div>
          <div><span>已上线轮播图</span><strong>{{ overview.onlineBannerCount }}</strong></div>
          <div><span>启用系统参数</span><strong>{{ overview.enabledSystemConfigCount }}</strong></div>
          <div><span>当前待清洁房间</span><strong>{{ overview.dirtyRoomCount }}</strong></div>
        </div>
      </el-card>
    </div>

    <el-card shadow="never" class="panel-card">
      <template #header><b>最近支付流水</b></template>
      <el-table :data="paymentRecords" stripe empty-text="暂无支付流水">
        <el-table-column prop="paidAt" label="支付时间" width="170">
          <template #default="{ row }">{{ row.paidAt?.slice(0, 19) }}</template>
        </el-table-column>
        <el-table-column prop="paymentNo" label="支付单号" min-width="180" />
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="paymentMethod" label="方式" width="110">
          <template #default="{ row }">{{ displayValue('paymentMethod', row.paymentMethod) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120">
          <template #default="{ row }">￥{{ money(row.amount) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts/core'
import { BarChart, GaugeChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getDashboardAnalytics, getDashboardOverview, getDashboardPaymentRecords, getInventoryWarnings } from '@/api/dashboard'
import { displayValue } from '@/utils/adminDisplay'

echarts.use([BarChart, GaugeChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const loading = ref(false)
const periodDays = ref(30)
const inventoryWarnings = ref([])
const paymentRecords = ref([])
const trendRef = ref()
const occupancyRef = ref()
const roomTypeRef = ref()
const paymentRef = ref()
const statusRef = ref()
const charts = []

const overview = reactive({
  todayOrderCount: 0, todayPaidOrderCount: 0, todayRevenue: 0, todayCheckInCount: 0,
  todayCheckOutCount: 0, todayUserCancellationCount: 0, availableRoomCount: 0,
  occupiedRoomCount: 0, dirtyRoomCount: 0, occupancyRate: 0, inventoryWarningCount: 0,
  publishedAnnouncementCount: 0, onlineBannerCount: 0, enabledSystemConfigCount: 0
})
const analytics = reactive({
  currentPeriodRevenue: 0, previousPeriodRevenue: 0, revenueGrowthRate: 0,
  trends: [], roomTypeStatistics: [], paymentMethods: [], orderStatuses: []
})

const money = value => Number(value || 0).toFixed(2)
const overviewCards = computed(() => {
  const growth = Number(analytics.revenueGrowthRate || 0)
  return [
    { key: 'periodRevenue', label: '本周期支付营收', value: '￥' + money(analytics.currentPeriodRevenue), hint: '近' + periodDays.value + '天成功支付流水' },
    { key: 'growth', label: '较上一周期', value: (growth >= 0 ? '+' : '') + growth.toFixed(2) + '%', hint: '相同天数环比', className: growth < 0 ? 'down' : 'up' },
    { key: 'todayRevenue', label: '今日支付金额', value: '￥' + money(overview.todayRevenue), hint: overview.todayPaidOrderCount + ' 笔支付订单' },
    { key: 'orders', label: '今日新增订单', value: overview.todayOrderCount, hint: overview.todayUserCancellationCount + ' 笔用户取消' },
    { key: 'occupancy', label: '当前入住率', value: Number(overview.occupancyRate || 0).toFixed(2) + '%', hint: overview.occupiedRoomCount + ' 间在住' },
    { key: 'inventory', label: '今日可售房量', value: overview.availableRoomCount, hint: overview.inventoryWarningCount + ' 个房型预警' }
  ]
})

function resultData(result, message) {
  if (result.code !== 'SUCCESS') throw new Error(result.message || message)
  return result.data
}
function emptyTitle(hasData) {
  return hasData ? undefined : { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#94a3b8', fontSize: 14, fontWeight: 400 } }
}
function chartAt(element) {
  const chart = echarts.getInstanceByDom(element) || echarts.init(element)
  if (!charts.includes(chart)) charts.push(chart)
  return chart
}
function renderCharts() {
  const trends = analytics.trends || []
  chartAt(trendRef.value).setOption({
    title: emptyTitle(trends.length), color: ['#496454', '#b78652'], tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '支付营收'] }, grid: { left: 55, right: 60, bottom: 35, top: 45 },
    xAxis: { type: 'category', data: trends.map(x => x.date), axisLabel: { hideOverlap: true } },
    yAxis: [{ type: 'value', name: '订单' }, { type: 'value', name: '营收(元)' }],
    series: [
      { name: '订单数', type: 'bar', barMaxWidth: 24, data: trends.map(x => x.orderCount) },
      { name: '支付营收', type: 'line', yAxisIndex: 1, smooth: true, symbolSize: 7, data: trends.map(x => Number(x.revenue || 0)) }
    ]
  }, true)

  chartAt(occupancyRef.value).setOption({
    series: [{ type: 'gauge', startAngle: 210, endAngle: -30, min: 0, max: 100,
      progress: { show: true, width: 16 }, axisLine: { lineStyle: { width: 16 } }, axisTick: { show: false },
      splitLine: { length: 8 }, axisLabel: { distance: 22 }, pointer: { length: '58%' },
      detail: { formatter: '{value}%', fontSize: 25, offsetCenter: [0, '65%'] },
      data: [{ value: Number(overview.occupancyRate || 0).toFixed(2), name: '入住率' }] }]
  }, true)

  const rooms = analytics.roomTypeStatistics || []
  chartAt(roomTypeRef.value).setOption({
    title: emptyTitle(rooms.length), tooltip: { trigger: 'axis' }, grid: { left: 90, right: 30, bottom: 25, top: 15 },
    xAxis: { type: 'value', axisLabel: { formatter: '￥{value}' } },
    yAxis: { type: 'category', data: rooms.map(x => x.roomTypeName).reverse() },
    series: [{ type: 'bar', data: rooms.map(x => Number(x.revenue || 0)).reverse(), itemStyle: { color: '#b78652', borderRadius: [0, 5, 5, 0] } }]
  }, true)

  const payments = analytics.paymentMethods || []
  chartAt(paymentRef.value).setOption({
    title: emptyTitle(payments.length), tooltip: { trigger: 'item', formatter: '{b}<br/>￥{c}（{d}%）' },
    legend: { bottom: 0 }, series: [{ type: 'pie', radius: ['42%', '68%'], center: ['50%', '44%'],
      data: payments.map(x => ({ name: x.label, value: Number(x.amount || 0) })),
      label: { formatter: '{b}\n{d}%' } }]
  }, true)

  const statuses = analytics.orderStatuses || []
  chartAt(statusRef.value).setOption({
    title: emptyTitle(statuses.length), tooltip: { trigger: 'item' }, legend: { bottom: 0 },
    series: [{ type: 'pie', radius: '68%', center: ['50%', '43%'],
      data: statuses.map(x => ({ name: x.label, value: x.count })),
      label: { formatter: '{b}\n{c}单' } }]
  }, true)
}

async function loadAll() {
  loading.value = true
  try {
    const [overviewResult, analyticsResult, warningsResult] = await Promise.all([
      getDashboardOverview(),
      getDashboardAnalytics({ days: periodDays.value }),
      getInventoryWarnings()
    ])
    Object.assign(overview, resultData(overviewResult, '加载今日概览失败'))
    Object.assign(analytics, resultData(analyticsResult, '加载经营分析失败'))
    inventoryWarnings.value = resultData(warningsResult, '加载库存预警失败') || []
    try {
      const paymentsResult = await getDashboardPaymentRecords({ days: periodDays.value, limit: 20 })
      paymentRecords.value = resultData(paymentsResult, '加载支付流水失败') || []
    } catch {
      paymentRecords.value = []
    }
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载经营看板失败')
  } finally {
    loading.value = false
  }
}
function resizeCharts() { charts.forEach(chart => chart.resize()) }
onMounted(() => { window.addEventListener('resize', resizeCharts); loadAll() })
onBeforeUnmount(() => { window.removeEventListener('resize', resizeCharts); charts.forEach(chart => chart.dispose()) })
</script>

<style scoped>
.dashboard-view{display:flex;flex-direction:column;gap:20px}.page-header,.header-actions,.panel-header{display:flex;align-items:center;justify-content:space-between;gap:16px}.page-header h1{margin:0 0 8px;font-size:28px;color:#1f2937}.page-header p,.panel-header span{margin:0;color:#6b7280}.stats-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}.stat-card{border-radius:20px}.stat-card span,.stat-card small{display:block;color:#64748b}.stat-card strong{display:block;margin:9px 0 5px;font-size:27px;color:#17212b}.stat-card strong.up{color:#16835f}.stat-card strong.down{color:#c2413b}.chart-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:20px}.panel-card{border-radius:20px}.panel-card.wide{grid-column:span 2}.chart{width:100%;height:330px}.wide .chart{height:360px}.content-grid{display:grid;grid-template-columns:1.4fr 1fr;gap:20px}.content-stats{display:grid;grid-template-columns:1fr 1fr;gap:14px}.content-stats div{display:flex;align-items:center;justify-content:space-between;padding:20px;border-radius:14px;background:linear-gradient(135deg,#f8fafc,#eef2ef)}.content-stats strong{font-size:25px;color:#243b31}@media(max-width:1000px){.stats-grid{grid-template-columns:repeat(2,1fr)}.chart-grid,.content-grid{grid-template-columns:1fr}.panel-card.wide{grid-column:span 1}}@media(max-width:680px){.page-header,.header-actions{align-items:flex-start;flex-direction:column}.stats-grid{grid-template-columns:1fr}.content-stats{grid-template-columns:1fr}.chart{height:290px}}
</style>
