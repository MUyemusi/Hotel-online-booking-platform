<template>
  <div class="payment-page">
    <div class="back-strip">
      <div class="back-inner">
        <button class="back-btn" @click="$router.back()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m15 18-6-6 6-6"/></svg>
          返回
        </button>
        <span class="back-crumb">订单支付</span>
      </div>
    </div>

    <div class="pay-body" v-if="order">
      <!-- 支付状态：已支付 -->
      <div v-if="order.paymentStatus === 'PAID'" class="paid-card">
        <div class="paid-icon">✓</div>
        <h2>支付成功</h2>
        <p>订单号：{{ order.orderNo || route.query.orderNo }}</p>
        <p class="paid-amount">已付 ¥{{ order.totalAmount || route.query.amount }}</p>
        <div class="paid-actions">
          <router-link :to="`/orders/${order.id}`" class="btn-outline">查看订单</router-link>
          <router-link to="/orders" class="btn-primary">我的订单</router-link>
        </div>
      </div>

      <!-- 待支付 -->
      <template v-else>
        <div class="pay-card">
          <div class="pc-head">
            <h2>确认支付</h2>
            <p class="pc-order">订单号：{{ order.orderNo || route.query.orderNo }}</p>
          </div>

          <div class="pc-amount">
            <span class="pca-label">应付金额</span>
            <span class="pca-num">¥{{ order.totalAmount || route.query.amount }}</span>
          </div>

          <div class="pc-methods">
            <h3>选择支付方式</h3>
            <div class="methods-grid">
              <div
                v-for="m in methods"
                :key="m.key"
                class="method-item"
                :class="{ selected: selectedMethod === m.key }"
                @click="selectedMethod = m.key"
              >
                <span class="mi-icon">{{ m.icon }}</span>
                <span class="mi-name">{{ m.label }}</span>
                <span class="mi-check" v-if="selectedMethod === m.key">✓</span>
              </div>
            </div>
          </div>

          <button class="pay-btn" :disabled="paying" @click="doPay">
            <span v-if="paying">支付处理中…</span>
            <span v-else>立即支付 ¥{{ order.totalAmount || route.query.amount }}</span>
          </button>
          <p class="pay-hint">此为模拟支付，不会产生真实扣款</p>
        </div>

        <!-- 支付超时倒计时 -->
        <div class="timeout-bar" v-if="remaining > 0">
          <span>支付剩余时间</span>
          <span class="timeout-num" :class="{ urgent: remaining <= 180 }">{{ fmtTime }}</span>
        </div>
      </template>
    </div>

    <div v-else-if="!loading" class="nf">
      <h2>订单不存在</h2>
      <router-link to="/orders">查看我的订单</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail } from '@/api/booking'
import { pay } from '@/api/payment'

const route = useRoute(); const router = useRouter()
const loading = ref(true)
const paying = ref(false)
const order = ref(null)
const selectedMethod = ref('WEIXIN')
let timer = null

const remaining = ref(0)
const fmtTime = computed(() => {
  const m = Math.floor(remaining.value / 60)
  const s = remaining.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

const methods = [
  { key: 'WEIXIN', label: '微信支付', icon: '💬' },
  { key: 'ALIPAY', label: '支付宝', icon: '🔵' },
  { key: 'BANK_CARD', label: '银行卡', icon: '💳' }
]

onMounted(async () => {
  loading.value = true
  try {
    const id = route.params.orderId
    const res = await getOrderDetail(id)
    if (res.code === 'SUCCESS') {
      order.value = res.data
      if (res.data.paymentRemainingSeconds != null && res.data.paymentRemainingSeconds > 0) {
        remaining.value = res.data.paymentRemainingSeconds
        startCountdown()
      }
    }
  } catch { /* order not found */ } finally { loading.value = false }
})

function startCountdown() {
  timer = setInterval(() => {
    if (remaining.value > 0) remaining.value--
    else { clearInterval(timer); order.value = null }
  }, 1000)
}

async function doPay() {
  paying.value = true
  try {
    const res = await pay({ orderId: Number(route.params.orderId), paymentMethod: selectedMethod.value })
    if (res.code === 'SUCCESS') {
      ElMessage.success('支付成功！')
      order.value = { ...order.value, ...res.data, paymentStatus: 'PAID' }
      clearInterval(timer)
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '支付失败，请重试')
  } finally { paying.value = false }
}

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.payment-page { min-height: 100vh; background: linear-gradient(180deg, #faf7f2 0%, #f5efe6 100%); }
.back-strip { max-width: 800px; margin: 0 auto; padding: 100px 48px 0; }
.back-inner { display: flex; align-items: center; gap: 14px; padding: 14px 0; border-bottom: 1px solid rgba(0,0,0,0.06); }
.back-btn {
  display: inline-flex; align-items: center; gap: 4px; background: rgba(255,255,255,0.6);
  backdrop-filter: blur(8px); border: 1px solid rgba(0,0,0,0.06); border-radius: 50px;
  padding: 8px 18px; color: var(--text-secondary); cursor: pointer; font-size: 13px; transition: all 0.3s;
}
.back-btn:hover { border-color: var(--accent); color: var(--accent); }
.back-crumb { font-size: 13px; color: var(--text-muted); }

.pay-body { max-width: 600px; margin: 30px auto 0; padding: 0 48px 80px; }

.pay-card {
  background: rgba(255,255,255,0.65); backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.7); border-radius: var(--radius-xl);
  padding: 36px; box-shadow: var(--shadow-md);
}
.pc-head h2 { font-size: 22px; font-weight: 700; color: var(--text-primary); margin: 0; }
.pc-order { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.pc-amount { text-align: center; margin: 28px 0; padding: 24px; background: var(--accent-soft); border-radius: var(--radius-md); }
.pca-label { display: block; font-size: 13px; color: var(--text-muted); }
.pca-num { display: block; font-size: 38px; font-weight: 700; color: var(--accent); margin-top: 4px; }

.pc-methods h3 { font-size: 14px; font-weight: 650; color: var(--text-primary); margin: 0 0 12px; }
.methods-grid { display: flex; flex-direction: column; gap: 10px; }
.method-item {
  display: flex; align-items: center; gap: 12px; padding: 16px 20px;
  border: 2px solid rgba(0,0,0,0.06); border-radius: var(--radius-md);
  cursor: pointer; transition: all 0.3s;
}
.method-item:hover { border-color: var(--accent); }
.method-item.selected { border-color: var(--accent); background: var(--accent-soft); }
.mi-icon { font-size: 22px; }
.mi-name { flex: 1; font-size: 15px; font-weight: 600; color: var(--text-primary); }
.mi-check { font-size: 16px; color: var(--accent); font-weight: 700; }

.pay-btn {
  width: 100%; padding: 16px; border: none; border-radius: var(--radius-md); margin-top: 24px;
  background: linear-gradient(135deg, #c8945c, #b07840);
  color: #fff; font-size: 16px; font-weight: 700; cursor: pointer;
  transition: all 0.3s;
}
.pay-btn:hover:not(:disabled) { box-shadow: 0 8px 32px rgba(180,120,60,0.3); transform: translateY(-2px); }
.pay-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.pay-hint { text-align: center; font-size: 11px; color: var(--text-muted); margin: 10px 0 0; }

.timeout-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 16px; padding: 14px 20px; background: rgba(255,255,255,0.6); border-radius: var(--radius-md); font-size: 13px; color: var(--text-secondary); }
.timeout-num { font-weight: 700; font-size: 18px; color: var(--text-primary); }
.timeout-num.urgent { color: #e74c3c; }

/* 已支付 */
.paid-card { text-align: center; padding: 60px 48px; background: rgba(255,255,255,0.65); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.7); border-radius: var(--radius-xl); box-shadow: var(--shadow-md); }
.paid-icon { width: 64px; height: 64px; border-radius: 50%; background: #27ae60; color: #fff; font-size: 32px; line-height: 64px; margin: 0 auto 20px; }
.paid-card h2 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0; }
.paid-card p { font-size: 14px; color: var(--text-secondary); margin: 6px 0; }
.paid-amount { font-size: 20px; font-weight: 700; color: var(--accent); }
.paid-actions { display: flex; gap: 12px; justify-content: center; margin-top: 28px; }
.btn-outline, .btn-primary { display: inline-block; padding: 12px 28px; border-radius: 50px; font-weight: 650; font-size: 14px; text-decoration: none; transition: all 0.3s; }
.btn-outline { border: 2px solid var(--accent); color: var(--accent); }
.btn-outline:hover { background: var(--accent-soft); }
.btn-primary { background: linear-gradient(135deg, #c8945c, #b07840); color: #fff; }
.btn-primary:hover { box-shadow: 0 6px 20px rgba(180,120,60,0.3); }

.nf { text-align: center; padding: 160px 20px; color: var(--text-secondary); }
.nf h2 { color: var(--text-primary); }
.nf a { color: var(--accent); text-decoration: none; margin-top: 14px; display: inline-block; font-weight: 600; }

@media (max-width: 768px) { .back-strip, .pay-body { padding-left: 20px; padding-right: 20px; } .pay-card { padding: 24px; } }
</style>
