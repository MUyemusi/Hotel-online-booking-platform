<template>
  <div class="orders-page">
    <div class="back-strip">
      <div class="back-inner">
        <button class="back-btn" @click="$router.push('/')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m15 18-6-6 6-6"/></svg>
          首页
        </button>
        <span class="back-crumb">我的订单</span>
      </div>
    </div>

    <div class="orders-body">
      <!-- 状态筛选 -->
      <div class="filter-bar">
        <button v-for="s in statuses" :key="s.key" class="fb-item" :class="{ active: currentStatus === s.key }" @click="switchStatus(s.key)">
          {{ s.label }}
        </button>
      </div>

      <!-- 加载 -->
      <div v-if="loading" class="load-wrap"><div class="sk-list"></div></div>

      <!-- 空 -->
      <div v-else-if="!orders.length" class="empty">
        <p class="empty-icon">📋</p>
        <p>暂无订单</p>
        <router-link to="/rooms">去预订</router-link>
      </div>

      <!-- 列表 -->
      <div v-else class="order-list">
        <div v-for="o in orders" :key="o.id" class="order-card" @click="$router.push(`/orders/${o.id}`)">
          <div class="oc-head">
            <span class="oc-no">{{ o.orderNo }}</span>
            <span class="oc-status" :class="statusClass(o.orderStatus)">{{ statusLabel(o.orderStatus) }}</span>
          </div>
          <div class="oc-body">
            <div class="oc-info">
              <h3>{{ o.roomTypeName }}</h3>
              <p>{{ o.checkInDate }} → {{ o.checkOutDate }} · {{ o.totalNights }}晚 · {{ o.roomCount }}间</p>
            </div>
            <div class="oc-price">¥{{ o.totalAmount }}</div>
          </div>
          <div class="oc-foot">
            <span>{{ o.createdAt?.slice(0, 10) }} 创建</span>
            <span v-if="o.paymentTimeout && o.orderStatus === 'PENDING_PAYMENT'" class="timeout-tip">请在 {{ o.paymentTimeout?.slice(11,16) }} 前支付</span>
            <button v-if="o.orderStatus === 'PENDING_PAYMENT'" class="go-pay" @click.stop="$router.push(`/payment/${o.id}`)">去支付</button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pager" v-if="total > size">
        <button :disabled="page <= 1" @click="page--; load()">上一页</button>
        <span>{{ page }} / {{ Math.ceil(total / size) }}</span>
        <button :disabled="page * size >= total" @click="page++; load()">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders } from '@/api/booking'

const loading = ref(true)
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const currentStatus = ref('')

const statuses = [
  { key: '', label: '全部' },
  { key: 'PENDING_PAYMENT', label: '待支付' },
  { key: 'PENDING_CHECK_IN', label: '待入住' },
  { key: 'CHECKED_IN', label: '已入住' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'CANCELLED', label: '已取消' },
  { key: 'CLOSED', label: '已关闭' }
]

const statusMap = {
  PENDING_PAYMENT: '待支付', PENDING_CHECK_IN: '待入住', CHECKED_IN: '已入住',
  COMPLETED: '已完成', CANCELLED: '已取消', CLOSED: '已关闭'
}
function statusLabel(s) { return statusMap[s] || s }
function statusClass(s) {
  if (s === 'PENDING_PAYMENT') return 's-pay'
  if (s === 'PENDING_CHECK_IN') return 's-checkin'
  if (s === 'CHECKED_IN') return 's-in'
  if (s === 'COMPLETED') return 's-done'
  return 's-cancel'
}

function switchStatus(s) { currentStatus.value = s; page.value = 1; load() }

async function load() {
  loading.value = true
  try {
    const res = await getMyOrders({ status: currentStatus.value || undefined, page: page.value, size: size.value })
    if (res.code === 'SUCCESS') { orders.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch { /* ignore */ } finally { loading.value = false }
}

onMounted(load)
</script>

<style scoped>
.orders-page { min-height: 100vh; background: linear-gradient(180deg, #faf7f2 0%, #f5efe6 100%); }
.back-strip { max-width: 1000px; margin: 0 auto; padding: 100px 48px 0; }
.back-inner { display: flex; align-items: center; gap: 14px; padding: 14px 0; border-bottom: 1px solid rgba(0,0,0,0.06); }
.back-btn {
  display: inline-flex; align-items: center; gap: 4px; background: rgba(255,255,255,0.6);
  backdrop-filter: blur(8px); border: 1px solid rgba(0,0,0,0.06); border-radius: 50px;
  padding: 8px 18px; color: var(--text-secondary); cursor: pointer; font-size: 13px; transition: all 0.3s;
}
.back-btn:hover { border-color: var(--accent); color: var(--accent); }
.back-crumb { font-size: 13px; color: var(--text-muted); }

.orders-body { max-width: 1000px; margin: 24px auto 0; padding: 0 48px 80px; }

.filter-bar { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }
.fb-item {
  padding: 8px 18px; border: 1px solid rgba(0,0,0,0.08); border-radius: 50px;
  background: rgba(255,255,255,0.5); font-size: 13px; color: var(--text-secondary);
  cursor: pointer; transition: all 0.3s;
}
.fb-item:hover { border-color: var(--accent); color: var(--accent); }
.fb-item.active { background: var(--accent); color: #fff; border-color: var(--accent); }

.order-card {
  background: rgba(255,255,255,0.65); backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.7); border-radius: var(--radius-lg);
  padding: 24px 28px; margin-bottom: 12px; cursor: pointer;
  box-shadow: var(--shadow-sm); transition: all 0.3s;
}
.order-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }

.oc-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.oc-no { font-size: 14px; font-weight: 650; color: var(--text-primary); font-family: monospace; }
.oc-status { font-size: 12px; font-weight: 650; padding: 4px 12px; border-radius: 50px; }
.s-pay { background: #fff3cd; color: #856404; }
.s-checkin { background: #d4edda; color: #155724; }
.s-in { background: #cce5ff; color: #004085; }
.s-done { background: #e2e3e5; color: #383d41; }
.s-cancel { background: #f8d7da; color: #721c24; }

.oc-body { display: flex; justify-content: space-between; align-items: center; }
.oc-info h3 { font-size: 16px; font-weight: 650; color: var(--text-primary); margin: 0; }
.oc-info p { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }
.oc-price { font-size: 20px; font-weight: 700; color: var(--accent); }

.oc-foot { display: flex; align-items: center; gap: 14px; margin-top: 14px; padding-top: 14px; border-top: 1px solid rgba(0,0,0,0.04); font-size: 12px; color: var(--text-muted); }
.timeout-tip { color: #e74c3c; font-weight: 600; }
.go-pay {
  margin-left: auto; padding: 8px 20px; border: none; border-radius: 50px;
  background: linear-gradient(135deg, #c8945c, #b07840); color: #fff;
  font-size: 13px; font-weight: 650; cursor: pointer; transition: all 0.3s;
}
.go-pay:hover { box-shadow: 0 4px 16px rgba(180,120,60,0.3); }

.empty { text-align: center; padding: 80px 20px; color: var(--text-secondary); }
.empty-icon { font-size: 48px; margin: 0 0 12px; }
.empty p { font-size: 16px; }
.empty a { color: var(--accent); font-weight: 600; text-decoration: none; }

.pager { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 28px; }
.pager button {
  padding: 8px 18px; border: 1px solid rgba(0,0,0,0.08); border-radius: 50px;
  background: rgba(255,255,255,0.5); cursor: pointer; font-size: 13px; color: var(--text-secondary);
}
.pager button:hover:not(:disabled) { border-color: var(--accent); color: var(--accent); }
.pager button:disabled { opacity: 0.4; cursor: not-allowed; }
.pager span { font-size: 13px; color: var(--text-muted); }

.load-wrap { min-height: 300px; }
.sk-list { height: 300px; background: linear-gradient(90deg, #f0ebe4 25%, #f8f4ef 50%, #f0ebe4 75%); background-size: 200% 100%; animation: shim 1.5s infinite; border-radius: var(--radius-lg); }
@keyframes shim { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

@media (max-width: 768px) { .back-strip, .orders-body { padding-left: 20px; padding-right: 20px; } }
</style>
