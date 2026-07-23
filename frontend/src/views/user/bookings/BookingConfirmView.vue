<template>
  <div class="confirm-page">
    <!-- 返回栏 -->
    <div class="back-strip">
      <div class="back-inner">
        <button class="back-btn" @click="$router.back()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m15 18-6-6 6-6"/></svg>
          返回
        </button>
        <span class="back-crumb">预订确认 / 填写入住信息</span>
      </div>
    </div>

    <div class="confirm-body" v-if="roomType">
      <!-- 左侧：订单摘要 -->
      <section class="order-summary">
        <div class="glass-card">
          <h2>订单摘要</h2>
          <div class="summary-room">
            <img v-if="images.length" :src="images[0].imageUrl" class="summary-img" />
            <div v-else class="summary-img-fb">ZHISU</div>
            <div class="summary-info">
              <h3>{{ roomType.name }}</h3>
              <p class="cat-tag">{{ roomType.category }}</p>
              <p class="attr">{{ roomType.bedType }} · {{ roomType.area }}m² · {{ roomType.maxGuests }}人可住</p>
            </div>
          </div>

          <div class="summary-detail">
            <div class="sd-row"><span>入住</span><span><strong>{{ checkIn }}</strong> 14:00 后</span></div>
            <div class="sd-row"><span>离店</span><span><strong>{{ checkOut }}</strong> 12:00 前</span></div>
            <div class="sd-row"><span>共</span><span><strong>{{ nights }}</strong> 晚</span></div>
            <div class="sd-row"><span>房间</span><span><strong>{{ rooms }}</strong> 间 · <strong>{{ guests }}</strong> 位入住人</span></div>
          </div>

          <div class="summary-price">
            <span>预估总价</span>
            <span class="sp-num">¥{{ totalPrice }}</span>
          </div>
        </div>
      </section>

      <!-- 右侧：入住人信息表单 -->
      <section class="guest-form">
        <div class="glass-card">
          <h2>入住人信息</h2>

          <!-- 入住人列表 -->
          <div v-for="(g, i) in guestList" :key="i" class="guest-block">
            <div class="guest-head">
              <span class="gh-badge">{{ i === 0 ? '主入住人' : '入住人 ' + (i + 1) }}</span>
            </div>
            <div class="gf-row">
              <div class="gf-item">
                <label>姓名 <em>*</em></label>
                <el-input v-model="g.guestName" placeholder="真实姓名" maxlength="20" />
              </div>
              <div class="gf-item">
                <label>手机号</label>
                <el-input v-model="g.guestPhone" placeholder="手机号" maxlength="11" />
              </div>
            </div>
            <div class="gf-row">
              <div class="gf-item">
                <label>证件类型</label>
                <el-select v-model="g.idType">
                  <el-option label="身份证" value="ID_CARD" />
                  <el-option label="护照" value="PASSPORT" />
                  <el-option label="港澳通行证" value="HK_MACAU" />
                  <el-option label="台湾通行证" value="TAIWAN" />
                </el-select>
              </div>
              <div class="gf-item">
                <label>证件号码</label>
                <el-input v-model="g.idNumber" placeholder="证件号码" maxlength="30" />
              </div>
            </div>
          </div>

          <!-- 联系手机 -->
          <div class="contact-phone">
            <label>订单联系手机 <em>*</em></label>
            <el-input v-model="phone" placeholder="用于接收订单通知" maxlength="11" />
          </div>

          <!-- 备注 -->
          <div class="remark-area">
            <label>备注</label>
            <el-input v-model="remark" type="textarea" :rows="2" placeholder="如有特殊需求请备注（选填）" maxlength="500" />
          </div>

          <button class="submit-btn" :disabled="submitting" @click="submitOrder">
            <span v-if="submitting">提交中…</span>
            <span v-else>确认预订 · ¥{{ totalPrice }}</span>
          </button>
          <p class="agree-hint">点击确认即表示同意酒店的预订条款与取消政策</p>
        </div>
      </section>
    </div>

    <div v-else-if="!loading" class="nf">
      <h2>信息不完整</h2>
      <router-link to="/rooms">去浏览房型</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRoomTypeDetail, getRoomTypeImages } from '@/api/roomtype'
import { createOrder } from '@/api/booking'
import { normalizeBookingQuery } from '@/utils/bookingQuery'

const route = useRoute(); const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const roomType = ref(null)
const images = ref([])

const booking = normalizeBookingQuery(route.query)
const checkIn = ref(booking.checkInDate || '')
const checkOut = ref(booking.checkOutDate || '')
const rooms = ref(booking.roomCount)
const guests = ref(booking.guests)
const phone = ref('')
const remark = ref('')

const guestList = reactive([{ guestName: '', guestPhone: '', idType: 'ID_CARD', idNumber: '', isMain: true }])

const nights = computed(() => {
  if (!checkIn.value || !checkOut.value) return 0
  return Math.max(0, Math.round((new Date(checkOut.value + 'T00:00:00') - new Date(checkIn.value + 'T00:00:00')) / 86400000))
})
const totalPrice = computed(() => roomType.value?.basePrice ? roomType.value.basePrice * nights.value * rooms.value : 0)

onMounted(async () => {
  loading.value = true
  try {
    const id = booking.roomTypeId
    if (!id || !checkIn.value || !checkOut.value) return
    const [rr, ir] = await Promise.all([getRoomTypeDetail(id), getRoomTypeImages(id)])
    if (rr.code === 'SUCCESS') roomType.value = rr.data
    if (ir.code === 'SUCCESS') images.value = ir.data || []
  } catch (e) {
    ElMessage.error('加载房型信息失败')
  } finally { loading.value = false }
})

async function submitOrder() {
  if (!guestList[0].guestName.trim()) { ElMessage.warning('请填写主入住人姓名'); return }
  if (!phone.value || !/^1[3-9]\d{9}$/.test(phone.value)) { ElMessage.warning('请填写正确的联系手机号'); return }

  submitting.value = true
  try {
    const res = await createOrder({
      roomTypeId: booking.roomTypeId,
      checkInDate: checkIn.value,
      checkOutDate: checkOut.value,
      roomCount: rooms.value,
      guestCount: guests.value,
      guests: guestList.map(g => ({
        guestName: g.guestName.trim(),
        guestPhone: g.guestPhone || undefined,
        idType: g.idType || 'ID_CARD',
        idNumber: g.idNumber || undefined,
        isMain: g.isMain
      })),
      phone: phone.value,
      idempotencyKey: crypto.randomUUID(),
      remark: remark.value || undefined
    })
    if (res.code === 'SUCCESS') {
      ElMessage.success('订单创建成功！')
      router.replace(`/orders/${res.data.id}/pay`)
    } else {
      ElMessage.error(res.message || '创建订单失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提交失败，请重试')
  } finally { submitting.value = false }
}
</script>

<style scoped>
.confirm-page { min-height: 100vh; background: linear-gradient(180deg, #faf7f2 0%, #f5efe6 100%); }
.back-strip { max-width: 1280px; margin: 0 auto; padding: 100px 48px 0; }
.back-inner { display: flex; align-items: center; gap: 14px; padding: 14px 0; border-bottom: 1px solid rgba(0,0,0,0.06); }
.back-btn {
  display: inline-flex; align-items: center; gap: 4px; background: rgba(255,255,255,0.6);
  backdrop-filter: blur(8px); border: 1px solid rgba(0,0,0,0.06); border-radius: 50px;
  padding: 8px 18px; color: var(--text-secondary); cursor: pointer; font-size: 13px; transition: all 0.3s;
}
.back-btn:hover { border-color: var(--accent); color: var(--accent); }
.back-crumb { font-size: 13px; color: var(--text-muted); }

.confirm-body { max-width: 1280px; margin: 30px auto 0; padding: 0 48px 80px; display: grid; grid-template-columns: 1fr 1fr; gap: 40px; align-items: start; }

.glass-card {
  background: rgba(255,255,255,0.65); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.7); border-radius: var(--radius-xl);
  padding: 32px; box-shadow: var(--shadow-md);
}
.glass-card h2 { font-size: 20px; font-weight: 700; color: var(--text-primary); margin: 0 0 24px; padding-bottom: 12px; border-bottom: 1px solid rgba(0,0,0,0.05); }

/* 摘要 */
.summary-room { display: flex; gap: 16px; }
.summary-img { width: 120px; height: 80px; border-radius: 10px; object-fit: cover; }
.summary-img-fb { width: 120px; height: 80px; border-radius: 10px; background: linear-gradient(135deg, #e0cdb5, #d4b896); display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; color: rgba(255,255,255,0.3); }
.summary-info h3 { font-size: 18px; font-weight: 700; color: var(--text-primary); margin: 0; }
.cat-tag { display: inline-block; padding: 2px 10px; border-radius: 50px; background: var(--accent-soft); color: var(--accent); font-size: 11px; font-weight: 600; margin: 6px 0; }
.attr { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.summary-detail { margin-top: 20px; padding: 18px; background: var(--accent-soft); border-radius: var(--radius-sm); }
.sd-row { display: flex; justify-content: space-between; font-size: 14px; color: var(--text-secondary); padding: 4px 0; }
.sd-row strong { color: var(--text-primary); }

.summary-price { display: flex; justify-content: space-between; align-items: baseline; margin-top: 20px; padding-top: 16px; border-top: 1px solid rgba(0,0,0,0.05); }
.summary-price > span { font-size: 14px; color: var(--text-secondary); }
.sp-num { font-size: 26px; font-weight: 700; color: var(--accent); }

/* 表单 */
.guest-block { padding: 20px; margin-bottom: 16px; background: rgba(248,244,239,0.6); border-radius: var(--radius-md); border: 1px solid rgba(0,0,0,0.04); }
.guest-head { margin-bottom: 14px; }
.gh-badge { font-size: 12px; font-weight: 650; color: var(--accent); background: var(--accent-soft); padding: 4px 12px; border-radius: 50px; }

.gf-row { display: flex; gap: 12px; margin-bottom: 10px; }
.gf-item { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.gf-item label, .contact-phone label, .remark-area label { font-size: 11px; font-weight: 650; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.08em; }
.gf-item label em, .contact-phone label em { color: #e74c3c; font-style: normal; }
.gf-item :deep(.el-input), .gf-item :deep(.el-select) { width: 100%; }

.contact-phone { margin-top: 18px; display: flex; flex-direction: column; gap: 4px; }
.remark-area { margin-top: 16px; display: flex; flex-direction: column; gap: 4px; }

.submit-btn {
  width: 100%; padding: 16px; border: none; border-radius: var(--radius-md); margin-top: 24px;
  background: linear-gradient(135deg, #c8945c, #b07840);
  color: #fff; font-size: 16px; font-weight: 700; cursor: pointer;
  transition: all 0.3s; letter-spacing: 0.04em;
}
.submit-btn:hover:not(:disabled) { box-shadow: 0 8px 32px rgba(180,120,60,0.3); transform: translateY(-2px); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.agree-hint { text-align: center; font-size: 11px; color: var(--text-muted); margin: 10px 0 0; }

.nf { text-align: center; padding: 160px 20px; color: var(--text-secondary); }
.nf h2 { color: var(--text-primary); }
.nf a { color: var(--accent); text-decoration: none; margin-top: 14px; display: inline-block; font-weight: 600; }

@media (max-width: 900px) { .confirm-body { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .back-strip, .confirm-body { padding-left: 20px; padding-right: 20px; } }
</style>
