<template>
  <div class="detail-page">
    <div class="back-strip">
      <div class="back-inner">
        <button class="back-btn" @click="$router.back()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m15 18-6-6 6-6"/></svg>
          返回
        </button>
        <span class="back-crumb">订单详情</span>
      </div>
    </div>

    <div class="detail-body" v-if="order">
      <!-- 订单状态头 -->
      <div class="status-banner" :class="bannerClass">
        <div>
          <h2>{{ order.roomTypeName }}</h2>
          <p>订单号：{{ order.orderNo }}</p>
        </div>
        <span class="sb-status">{{ statusLabel(order.orderStatus) }}</span>
      </div>

      <div class="detail-grid">
        <!-- 左侧 -->
        <div class="detail-main">
          <!-- 入住信息 -->
          <div class="section-card">
            <h3>入住信息</h3>
            <div class="info-grid">
              <div class="ig-item"><span>入住日期</span><b>{{ order.checkInDate }}</b></div>
              <div class="ig-item"><span>离店日期</span><b>{{ order.checkOutDate }}</b></div>
              <div class="ig-item"><span>共住</span><b>{{ order.totalNights }} 晚</b></div>
              <div class="ig-item"><span>房间</span><b>{{ order.roomCount }} 间 · {{ order.guestCount }} 位</b></div>
            </div>
          </div>

          <!-- 入住人 -->
          <div class="section-card" v-if="order.guests?.length">
            <h3>入住人</h3>
            <div v-for="g in order.guests" :key="g.id" class="guest-row">
              <span class="gr-name">{{ g.guestName }}</span>
              <span class="gr-id">{{ g.idType }} {{ g.idNumber }}</span>
              <span class="gr-tag" v-if="g.isMain">主</span>
            </div>
          </div>

          <!-- 每日价格 -->
          <div class="section-card" v-if="order.dailyPrices?.length">
            <h3>每日价格明细</h3>
            <div v-for="dp in order.dailyPrices" :key="dp.date" class="price-row">
              <span>{{ dp.date }}</span>
              <span>¥{{ dp.price }} × {{ order.roomCount }}间</span>
            </div>
          </div>

          <!-- 支付记录 -->
          <div class="section-card" v-if="order.latestPayment">
            <h3>支付记录</h3>
            <div class="pay-record">
              <div class="pr-row"><span>支付单号</span><b>{{ order.latestPayment.paymentNo }}</b></div>
              <div class="pr-row"><span>支付方式</span><b>{{ order.latestPayment.paymentMethod }}</b></div>
              <div class="pr-row"><span>支付金额</span><b class="pr-amount">¥{{ order.latestPayment.amount }}</b></div>
              <div class="pr-row"><span>支付时间</span><b>{{ order.latestPayment.paidAt?.slice(0, 19) }}</b></div>
              <template v-if="order.latestPayment.refundAmount">
                <div class="pr-row"><span>退款金额</span><b class="pr-refund">¥{{ order.latestPayment.refundAmount }}</b></div>
                <div class="pr-row"><span>退款时间</span><b>{{ order.latestPayment.refundedAt?.slice(0, 19) }}</b></div>
              </template>
            </div>
          </div>

          <!-- 取消记录 -->
          <div class="section-card" v-if="order.cancellation">
            <h3>取消记录</h3>
            <div class="pr-row"><span>取消类型</span><b>{{ order.cancellation.cancelType }}</b></div>
            <div class="pr-row"><span>取消原因</span><b>{{ order.cancellation.cancelReason }}</b></div>
            <div class="pr-row"><span>取消时间</span><b>{{ order.cancellation.cancelledAt?.slice(0, 19) }}</b></div>
            <div class="pr-row" v-if="order.cancellation.refundAmount"><span>退款</span><b>¥{{ order.cancellation.refundAmount }}</b></div>
          </div>
        </div>

        <!-- 右侧：操作 -->
        <aside class="detail-side">
          <div class="side-card">
            <div class="sc-price">
              <span>订单金额</span>
              <span class="scp-num">¥{{ order.totalAmount }}</span>
            </div>

            <!-- 待支付 -->
            <template v-if="order.orderStatus === 'PENDING_PAYMENT'">
              <div class="sc-timeout" v-if="order.paymentRemainingSeconds > 0">
                <span>支付剩余</span>
                <span class="sc-tnum">{{ fmtRemaining }}</span>
              </div>
              <button class="sc-btn primary" @click="$router.push(`/orders/${order.id}/pay`)">去支付</button>
              <button class="sc-btn cancel" @click="showCancel = true">取消订单</button>
            </template>

            <!-- 待入住 -->
            <template v-if="order.orderStatus === 'PENDING_CHECK_IN'">
              <div class="qr-panel">
                <div class="qr-head">
                  <span>入住二维码</span>
                  <button class="qr-generate" :disabled="qrLoading" @click="generateQr">
                    {{ qrLoading ? '生成中...' : (qrImageDataUrl ? '刷新二维码' : '生成二维码') }}
                  </button>
                </div>
                <div class="qr-body">
                  <img v-if="qrImageDataUrl" class="qr-img" :src="qrImageDataUrl" alt="入住二维码" />
                  <div v-else class="qr-empty">
                    <span>点击生成二维码</span>
                    <small>到店后向前台出示，用于办理入住</small>
                  </div>
                  <div v-if="verificationCode" class="verify-code-box">
                    <span class="verify-code-label">唯一核验码</span>
                    <div class="verify-code-value">{{ verificationCode }}</div>
                    <small>演示场景下，前台也可直接输入该核验码完成核验</small>
                  </div>
                  <p v-if="qrExpiresAt" class="qr-expire">有效期至：{{ qrExpiresAt.slice(0, 19) }}</p>
                  <p v-else-if="qrHasActiveToken && !qrExpired" class="qr-expire">当前已有有效二维码，可刷新以重新生成</p>
                  <p v-else-if="qrHasActiveToken && qrExpired" class="qr-expire">二维码已过期，请重新生成</p>
                </div>
              </div>
              <button class="sc-btn cancel" @click="showCancel = true">取消订单</button>
            </template>

            <!-- 已入住 -->
            <template v-if="order.orderStatus === 'CHECKED_IN'">
              <div class="checkout-panel">
                <strong>当前已入住</strong>
                <p>如已完成实际离店，可在住客端自助办理退房。确认后订单将完成，房间转为待清洁状态。</p>
              </div>
              <button class="sc-btn checkout" @click="showCheckout = true">办理退房</button>
            </template>

            <!-- 已完成/已取消/已关闭 -->
            <p v-if="['COMPLETED','CANCELLED','CLOSED'].includes(order.orderStatus)" class="sc-done">
              {{ statusLabel(order.orderStatus) }}
            </p>
          </div>
        </aside>
      </div>
    </div>

    <!-- 取消弹窗 -->
    <el-dialog v-model="showCancel" title="取消订单" width="420px" :close-on-click-modal="false">
      <div class="cancel-dialog">
        <p>确定要取消此订单吗？取消后无法撤销。</p>
        <el-input v-model="cancelReason" type="textarea" :rows="2" placeholder="请填写取消原因" maxlength="500" />
      </div>
      <template #footer>
        <el-button @click="showCancel = false">再想想</el-button>
        <el-button type="danger" :loading="cancelling" @click="doCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 退房弹窗 -->
    <el-dialog v-model="showCheckout" title="办理退房" width="440px" :close-on-click-modal="false">
      <div class="cancel-dialog">
        <p>确认办理退房后，订单将变为已完成，已入住房间将转为待清洁状态。</p>
      </div>
      <template #footer>
        <el-button @click="showCheckout = false">暂不退房</el-button>
        <el-button type="primary" :loading="checkingOut" @click="doCheckout">确认退房</el-button>
      </template>
    </el-dialog>

    <div v-if="!loading && !order" class="nf">
      <h2>订单不存在</h2>
      <router-link to="/orders">返回我的订单</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, cancelOrder, checkoutOrder, generateCheckinQr, getCheckinQrStatus } from '@/api/booking'

const route = useRoute()
const loading = ref(true)
const order = ref(null)
const showCancel = ref(false)
const showCheckout = ref(false)
const cancelReason = ref('')
const cancelling = ref(false)
const checkingOut = ref(false)
const qrLoading = ref(false)
const qrImageDataUrl = ref('')
const qrExpiresAt = ref('')
const qrHasActiveToken = ref(false)
const qrExpired = ref(false)
const verificationCode = ref('')

function createRequestToken(prefix) {
  if (window.crypto?.randomUUID) return `${prefix}-${window.crypto.randomUUID()}`
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

const statusMap = {
  PENDING_PAYMENT: '待支付', PENDING_CHECK_IN: '待入住', CHECKED_IN: '已入住',
  COMPLETED: '已完成', CANCELLED: '已取消', CLOSED: '已关闭'
}
function statusLabel(s) { return statusMap[s] || s }

const bannerClass = computed(() => {
  const s = order.value?.orderStatus
  if (s === 'PENDING_PAYMENT') return 'b-pay'
  if (s === 'PENDING_CHECK_IN') return 'b-checkin'
  if (s === 'CHECKED_IN') return 'b-in'
  if (s === 'COMPLETED') return 'b-done'
  return 'b-cancel'
})

const fmtRemaining = computed(() => {
  const s = order.value?.paymentRemainingSeconds || 0
  const m = Math.floor(s / 60); const sec = s % 60
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    if (res.code === 'SUCCESS') {
      order.value = res.data
      await loadQrStatus()
    }
  } catch { /* */ } finally { loading.value = false }
})

async function loadQrStatus() {
  if (order.value?.orderStatus !== 'PENDING_CHECK_IN') return
  try {
    const res = await getCheckinQrStatus(order.value.id)
    if (res.code !== 'SUCCESS') return
    qrHasActiveToken.value = Boolean(res.data?.hasActiveToken)
    qrExpired.value = Boolean(res.data?.expired)
    qrExpiresAt.value = res.data?.expiresAt || ''
    verificationCode.value = res.data?.verificationCode || ''
  } catch { /* */ }
}

async function generateQr() {
  if (!order.value?.id) return
  qrLoading.value = true
  try {
    const res = await generateCheckinQr(order.value.id)
    if (res.code !== 'SUCCESS') {
      ElMessage.error(res.message || '生成二维码失败')
      return
    }
    qrImageDataUrl.value = res.data?.qrImageDataUrl || ''
    qrExpiresAt.value = res.data?.expiresAt || ''
    qrHasActiveToken.value = true
    qrExpired.value = false
    verificationCode.value = res.data?.verificationCode || ''
    ElMessage.success('二维码已生成')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '生成二维码失败')
  } finally {
    qrLoading.value = false
  }
}

async function doCancel() {
  if (!cancelReason.value.trim()) { ElMessage.warning('请填写取消原因'); return }
  cancelling.value = true
  try {
    const res = await cancelOrder(route.params.id, { cancelReason: cancelReason.value.trim() })
    if (res.code === 'SUCCESS') {
      ElMessage.success('订单已取消')
      order.value = { ...order.value, orderStatus: 'CANCELLED', cancellation: res.data }
      showCancel.value = false
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '取消失败')
  } finally { cancelling.value = false }
}

async function doCheckout() {
  if (!order.value?.id) return
  checkingOut.value = true
  try {
    const res = await checkoutOrder(order.value.id, {
      requestToken: createRequestToken('USER-CHECKOUT'),
      remark: '住客端自助退房'
    })
    if (res.code === 'SUCCESS') {
      ElMessage.success('退房办理成功')
      order.value = { ...order.value, orderStatus: 'COMPLETED' }
      showCheckout.value = false
    } else {
      ElMessage.error(res.message || '退房办理失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '退房办理失败')
  } finally {
    checkingOut.value = false
  }
}
</script>

<style scoped>
.detail-page { min-height: 100vh; background: linear-gradient(180deg, #faf7f2 0%, #f5efe6 100%); }
.back-strip { max-width: 1100px; margin: 0 auto; padding: 100px 48px 0; }
.back-inner { display: flex; align-items: center; gap: 14px; padding: 14px 0; border-bottom: 1px solid rgba(0,0,0,0.06); }
.back-btn {
  display: inline-flex; align-items: center; gap: 4px; background: rgba(255,255,255,0.6);
  backdrop-filter: blur(8px); border: 1px solid rgba(0,0,0,0.06); border-radius: 50px;
  padding: 8px 18px; color: var(--text-secondary); cursor: pointer; font-size: 13px; transition: all 0.3s;
}
.back-btn:hover { border-color: var(--accent); color: var(--accent); }
.back-crumb { font-size: 13px; color: var(--text-muted); }

.status-banner { max-width: 1100px; margin: 24px auto 0; padding: 28px 48px; display: flex; justify-content: space-between; align-items: center; border-radius: var(--radius-xl); }
.status-banner h2 { font-size: 22px; font-weight: 700; margin: 0; color: #fff; }
.status-banner p { font-size: 13px; margin: 4px 0 0; opacity: 0.8; color: #fff; }
.sb-status { font-size: 15px; font-weight: 700; padding: 8px 22px; border-radius: 50px; background: rgba(255,255,255,0.25); color: #fff; }
.b-pay { background: linear-gradient(135deg, #f39c12, #e67e22); }
.b-checkin { background: linear-gradient(135deg, #27ae60, #2ecc71); }
.b-in { background: linear-gradient(135deg, #2980b9, #3498db); }
.b-done { background: linear-gradient(135deg, #7f8c8d, #95a5a6); }
.b-cancel { background: linear-gradient(135deg, #c0392b, #e74c3c); }

.detail-grid { max-width: 1100px; margin: 28px auto 0; padding: 0 48px 80px; display: grid; grid-template-columns: 1fr 340px; gap: 32px; align-items: start; }

.section-card {
  background: rgba(255,255,255,0.65); backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.7); border-radius: var(--radius-lg);
  padding: 24px 28px; margin-bottom: 16px; box-shadow: var(--shadow-sm);
}
.section-card h3 { font-size: 16px; font-weight: 700; color: var(--text-primary); margin: 0 0 16px; padding-bottom: 10px; border-bottom: 1px solid rgba(0,0,0,0.05); }

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.ig-item { display: flex; flex-direction: column; gap: 2px; }
.ig-item span { font-size: 11px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.04em; }
.ig-item b { font-size: 15px; color: var(--text-primary); }

.guest-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid rgba(0,0,0,0.03); }
.guest-row:last-child { border: none; }
.gr-name { font-weight: 650; color: var(--text-primary); font-size: 14px; }
.gr-id { font-size: 12px; color: var(--text-muted); flex: 1; }
.gr-tag { padding: 2px 8px; border-radius: 50px; background: var(--accent-soft); color: var(--accent); font-size: 11px; font-weight: 650; }

.price-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 13px; color: var(--text-secondary); }

.pay-record { font-size: 13px; }
.pr-row { display: flex; justify-content: space-between; padding: 6px 0; color: var(--text-secondary); }
.pr-row b { color: var(--text-primary); }
.pr-amount { color: var(--accent) !important; font-size: 16px; }
.pr-refund { color: #e74c3c !important; }

.detail-side { position: sticky; top: 100px; }
.side-card {
  background: rgba(255,255,255,0.65); backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.7); border-radius: var(--radius-xl);
  padding: 28px; box-shadow: var(--shadow-md);
}
.sc-price { text-align: center; }
.sc-price > span { font-size: 13px; color: var(--text-muted); }
.scp-num { display: block; font-size: 32px; font-weight: 700; color: var(--accent); margin-top: 4px; }

.sc-timeout { display: flex; justify-content: space-between; align-items: center; margin: 16px 0; padding: 12px; background: #fff3cd; border-radius: var(--radius-sm); font-size: 13px; color: #856404; }
.sc-tnum { font-weight: 700; font-size: 18px; }

.sc-btn { width: 100%; padding: 14px; border: none; border-radius: var(--radius-md); font-size: 15px; font-weight: 650; cursor: pointer; margin-top: 12px; transition: all 0.3s; }
.sc-btn.primary { background: linear-gradient(135deg, #c8945c, #b07840); color: #fff; }
.sc-btn.primary:hover { box-shadow: 0 8px 32px rgba(180,120,60,0.3); }
.sc-btn.cancel { background: transparent; border: 2px solid #e74c3c; color: #e74c3c; }
.sc-btn.cancel:hover { background: #fdecea; }
.sc-btn.checkout { background: linear-gradient(135deg, #496454, #33483b); color: #fff; }
.sc-btn.checkout:hover { box-shadow: 0 8px 28px rgba(73,100,84,0.24); }

.checkout-panel{margin-top:18px;padding:15px;border-radius:14px;background:rgba(73,100,84,0.08);border:1px solid rgba(73,100,84,0.16)}
.checkout-panel strong{display:block;color:#33483b;font-size:15px}
.checkout-panel p{margin:7px 0 0;color:var(--text-secondary);font-size:13px;line-height:1.7}

.qr-panel{margin-top:18px;padding:14px;border-radius:14px;background:rgba(255,255,255,0.55);border:1px solid rgba(0,0,0,0.06)}
.qr-head{display:flex;align-items:center;justify-content:space-between;gap:12px;font-size:13px;color:var(--text-secondary);font-weight:650}
.qr-generate{border:none;background:rgba(39,174,96,0.14);color:#1f8a55;padding:7px 10px;border-radius:10px;cursor:pointer;font-size:12px;font-weight:650}
.qr-generate:disabled{opacity:0.6;cursor:not-allowed}
.qr-body{margin-top:10px}
.qr-img{width:100%;border-radius:12px;border:1px solid rgba(0,0,0,0.06);background:#fff}
.qr-empty{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:6px;padding:18px 10px;border-radius:12px;border:1px dashed rgba(0,0,0,0.15);color:var(--text-muted);background:rgba(255,255,255,0.5)}
.qr-empty small{font-size:12px;color:var(--text-muted)}
.verify-code-box{margin-top:12px;padding:12px;border-radius:12px;background:#f7f8fa;border:1px solid rgba(0,0,0,0.06)}
.verify-code-label{display:block;font-size:12px;color:var(--text-muted);margin-bottom:6px}
.verify-code-value{font-size:18px;font-weight:700;letter-spacing:.08em;color:#1f2937;word-break:break-all}
.verify-code-box small{display:block;margin-top:6px;color:var(--text-muted);font-size:12px}
.qr-expire{margin:10px 0 0;font-size:12px;color:var(--text-muted)}

.sc-done { text-align: center; font-size: 14px; color: var(--text-muted); padding: 20px 0 0; }

.cancel-dialog p { font-size: 14px; color: var(--text-secondary); margin: 0 0 14px; }

.nf { text-align: center; padding: 160px 20px; color: var(--text-secondary); }
.nf h2 { color: var(--text-primary); }
.nf a { color: var(--accent); text-decoration: none; margin-top: 14px; display: inline-block; font-weight: 600; }

@media (max-width: 900px) { .detail-grid { grid-template-columns: 1fr; } .detail-side { position: static; } }
@media (max-width: 768px) { .back-strip, .detail-grid, .status-banner { padding-left: 20px; padding-right: 20px; } }
</style>
