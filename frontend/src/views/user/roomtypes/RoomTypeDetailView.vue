
<template>
  <div class="detail-page">
    <!-- 返回栏 -->
    <div class="back-strip">
      <div class="back-inner">
        <button class="back-btn" @click="$router.back()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m15 18-6-6 6-6"/></svg>
          返回
        </button>
        <span class="back-crumb">客房鉴赏 / {{ room.name || '详情' }}</span>
      </div>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="load-wrap">
      <div class="sk-hero"></div>
    </div>

    <!-- 主体 -->
    <template v-else-if="room.id">
      <!-- 全屏大图 -->
      <section class="gallery-hero">
        <div class="gallery-main">
          <img
            v-if="images.length"
            :src="images[activeIdx].imageUrl"
            :alt="room.name"
            class="gallery-img"
          />
          <div v-else class="gallery-fb">
            <div class="fb-texture"></div>
            <span>ZHISU HOTEL</span>
          </div>
        </div>
        <div v-if="images.length > 1" class="gallery-strip">
          <button
            v-for="(img, i) in images"
            :key="img.id"
            class="gs-item" :class="{ on: i === activeIdx }"
            @click="activeIdx = i"
          >
            <img :src="img.imageUrl" alt="" />
          </button>
        </div>
      </section>

      <!-- 内容 + 侧栏 -->
      <section class="detail-body">
        <div class="detail-main">
          <!-- 头 -->
          <div class="info-head">
            <div>
              <p class="head-tag" v-if="room.category">{{ room.category }}</p>
              <h1>{{ room.name }}</h1>
            </div>
            <div class="head-price">
              <span class="hp-sym">¥</span>
              <span class="hp-num">{{ room.basePrice }}</span>
              <span class="hp-suf">/ 晚起</span>
            </div>
          </div>

          <!-- 指标 -->
          <div class="stat-row">
            <div class="stat" v-if="room.area"><b>{{ room.area }}<small>m²</small></b><span>面积</span></div>
            <div class="stat" v-if="room.bedType"><b>{{ room.bedType }}</b><span>床型</span></div>
            <div class="stat" v-if="room.maxGuests"><b>{{ room.maxGuests }}<small>人</small></b><span>可住</span></div>
            <div class="stat"><b>{{ room.hasBreakfast ? '含早' : '无早' }}</b><span>早餐</span></div>
          </div>

          <!-- 描述 -->
          <div class="info-block" v-if="room.description">
            <h3>房型介绍</h3>
            <p>{{ room.description }}</p>
          </div>

          <!-- 设施 -->
          <div class="info-block" v-if="facilities.length">
            <h3>客房设施</h3>
            <div class="fac-grid">
              <div v-for="f in facilities" :key="f.id" class="fac">
                <span class="fac-ico">{{ f.facilityIcon || '✦' }}</span>
                <span>{{ f.facilityName }}</span>
              </div>
            </div>
          </div>

          <!-- 住客评价 -->
          <div class="info-block">
            <h3>住客评价 ({{ reviewTotal }})</h3>
            <div v-if="reviews.length" class="review-list">
              <div v-for="r in reviews" :key="r.id" class="review-card">
                <div class="review-hd">
                  <span class="review-avatar">{{ r.avatar || '👤' }}</span>
                  <div class="review-meta">
                    <span class="review-nick">{{ r.nickname }}</span>
                    <span class="review-date">{{ r.createdAt?.slice(0, 10) }}</span>
                  </div>
                </div>
                <p class="review-text">{{ r.content }}</p>
              </div>
            </div>
            <p v-else class="review-empty">暂无评价，成为第一位评价的客人吧</p>

            <div v-if="loggedIn" class="review-form">
              <el-input v-model="newReview" type="textarea" :rows="3" placeholder="分享您的入住体验..." maxlength="500" show-word-limit />
              <button class="review-submit" :disabled="!newReview.trim() || posting" @click="postReview">{{ posting ? '发布中...' : '发布评价' }}</button>
            </div>
            <div v-else class="review-login-hint">
              <router-link :to="`/login?redirect=/rooms/${room.id}`">登录</router-link> 后即可发表评价
            </div>
          </div>
        </div>

        <!-- 侧边毛玻璃预订卡 -->
        <aside class="detail-side">
          <div class="book-glass">
            <div class="bg-head">
              <span>预订此房型</span>
              <span class="bg-price">¥{{ room.basePrice }}<small>/晚起</small></span>
            </div>

            <div class="bg-form">
              <el-checkbox v-if="earlyMorningAvailable" v-model="earlyMode">凌晨入住（当日中午退房）</el-checkbox>
              <div class="bf">
                <label>入住日期</label>
                <el-date-picker v-model="bk.checkIn" type="date" placeholder="选择" :disabled-date="noPast" format="MM/DD" value-format="YYYY-MM-DD"><template #default="cell"><span>{{ cell.text }}</span><sup v-if="isEarlyCell(cell)">凌</sup></template></el-date-picker>
              </div>
              <div class="bf">
                <label>离店日期</label>
                <el-date-picker v-model="bk.checkOut" type="date" placeholder="选择" :disabled-date="noPastOut" format="MM/DD" value-format="YYYY-MM-DD" />
              </div>
              <div class="bf-row">
                <div class="bf">
                  <label>房间</label>
                  <el-input-number v-model="bk.rooms" :min="1" :max="5" controls-position="right" />
                </div>
                <div class="bf">
                  <label>人数</label>
                  <el-select v-model="bk.guests">
                    <el-option v-for="n in 6" :key="n" :label="n+'位'" :value="n" />
                  </el-select>
                </div>
              </div>

              <div class="bg-sum" v-if="bk.checkIn && bk.checkOut">
                <div class="sum-line"><span>入住</span><span>{{ nights }} 晚</span></div>
                <div class="sum-line"><span>预估</span><span class="sum-total">¥{{ totalPrice }}</span></div>
              </div>

              <button class="book-btn" @click="doBook">立即预订</button>
            </div>
          </div>
        </aside>
      </section>
    </template>

    <div v-else class="nf">
      <h2>房型不存在或已下架</h2>
      <router-link to="/rooms">返回列表</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRoomTypeDetail, getRoomTypeImages, getRoomTypeFacilities } from '@/api/roomtype'
import { searchRoomTypes } from '@/api/roomTypes'
import { normalizeBookingQuery, toBookingQuery } from '@/utils/bookingQuery'
import { getReviews, createReview } from '@/api/review'
import { getAccessToken } from '@/utils/request'
import { hotelToday, isEarlyMorningAvailable, nextDate } from '@/utils/bookingStay'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const room = ref({})
const images = ref([])
const facilities = ref([])
const quote = ref(null)
const activeIdx = ref(0)
let quoteRequestId = 0

const initial = normalizeBookingQuery(route.query, { roomTypeId: Number(route.params.id), roomCount: 1, guests: 1 })
const bk = reactive({ checkIn: initial.checkInDate, checkOut: initial.checkOutDate, rooms: initial.roomCount, guests: initial.guests, stayType: initial.stayType })
const earlyMorningAvailable = isEarlyMorningAvailable()
const earlyMode = computed({
  get: () => bk.stayType === 'EARLY_MORNING',
  set: enabled => {
    bk.stayType = enabled ? 'EARLY_MORNING' : 'STANDARD'
    if (enabled) bk.checkIn = bk.checkOut = hotelToday()
    else if (bk.checkIn) bk.checkOut = nextDate(bk.checkIn)
  }
})
const nights = computed(() => {
  if (!bk.checkIn || !bk.checkOut) return 0
  if (bk.stayType === 'EARLY_MORNING') return 1
  return Math.max(0, Math.round((new Date(bk.checkOut) - new Date(bk.checkIn)) / 86400000))
})
const totalPrice = computed(() => quote.value?.totalAmount ?? (room.value.basePrice ? room.value.basePrice * nights.value * bk.rooms : 0))

function noPast(t) { if (earlyMode.value) return t.toLocaleDateString('sv-SE') !== hotelToday(); const d = new Date(); d.setHours(0, 0, 0, 0); return t.getTime() < d.getTime() }
function noPastOut(t) {
  if (earlyMode.value) return t.toLocaleDateString('sv-SE') !== hotelToday()
  if (!bk.checkIn) return noPast(t)
  const n = new Date(bk.checkIn + 'T00:00:00'); n.setDate(n.getDate() + 1); return t.getTime() < n.getTime()
}
async function loadQuote(showError = false) {
  if (!bk.checkIn || !bk.checkOut || !room.value.id) { quote.value = null; return null }
  const requestId = ++quoteRequestId
  try {
    const response = await searchRoomTypes({ checkInDate: bk.checkIn, checkOutDate: bk.checkOut, roomCount: bk.rooms, guests: bk.guests, stayType: bk.stayType })
    if (requestId !== quoteRequestId) return quote.value
    const matched = (response.data || []).find(item => item.id === room.value.id)
    quote.value = matched || null
    if (!matched && showError) ElMessage.error('所选日期库存不足或房型不可订，请调整日期或数量')
    return matched
  } catch (e) {
    if (requestId === quoteRequestId) quote.value = null
    if (showError) ElMessage.error(e?.response?.data?.message || e.message || '报价失败')
    return null
  }
}
// ---- 评论 ----
const reviews = ref([])
const reviewTotal = ref(0)
const newReview = ref('')
const posting = ref(false)
const loggedIn = computed(() => Boolean(getAccessToken()))

async function loadReviews() {
  try {
    const res = await getReviews(route.params.id)
    if (res.code === 'SUCCESS') { reviews.value = res.data.records || []; reviewTotal.value = res.data.total || 0 }
  } catch { /* ignore */ }
}

async function postReview() {
  if (!newReview.value.trim()) return
  posting.value = true
  try {
    const res = await createReview({ roomTypeId: room.value.id, content: newReview.value.trim() })
    if (res.code === 'SUCCESS') {
      ElMessage.success('评价发布成功')
      reviews.value.unshift(res.data)
      reviewTotal.value++
      newReview.value = ''
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发布失败')
  } finally { posting.value = false }
}

async function load() {
  loading.value = true
  try {
    const id = route.params.id
    const [rr, ir, fr] = await Promise.all([getRoomTypeDetail(id), getRoomTypeImages(id), getRoomTypeFacilities(id)])
    if (rr.code === 'SUCCESS') room.value = rr.data
    if (ir.code === 'SUCCESS') images.value = ir.data || []
    if (fr.code === 'SUCCESS') facilities.value = fr.data || []
    await Promise.all([loadQuote(), loadReviews()])
  } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false }
}
async function doBook() {
  if (!bk.checkIn || !bk.checkOut) { ElMessage.warning('请选择日期'); return }
  const currentQuote = await loadQuote(true)
  if (!currentQuote) return
  router.push({ path: '/booking/confirm', query: toBookingQuery({ checkInDate: bk.checkIn, checkOutDate: bk.checkOut, roomCount: bk.rooms, guests: bk.guests, stayType: bk.stayType }, room.value.id) })
}
function isEarlyCell(cell) { return earlyMorningAvailable && cell?.dayjs?.format?.('YYYY-MM-DD') === hotelToday() }
watch(() => bk.checkIn, value => {
  if (bk.stayType === 'STANDARD' && value && (!bk.checkOut || bk.checkOut <= value)) bk.checkOut = nextDate(value)
})
watch(() => [bk.checkIn, bk.checkOut, bk.rooms, bk.guests, bk.stayType], () => { loadQuote() })
watch(() => route.params.id, id => { if (id) load() })
onMounted(load)
</script>

<style scoped>
/* 返回 */
.back-strip { max-width: 1280px; margin: 0 auto; padding: 100px 48px 0; }
.back-inner { display: flex; align-items: center; gap: 14px; padding: 14px 0; border-bottom: 1px solid rgba(0,0,0,0.06); }
.back-btn {
  display: inline-flex; align-items: center; gap: 4px; background: rgba(255,255,255,0.6);
  backdrop-filter: blur(8px); border: 1px solid rgba(0,0,0,0.06); border-radius: 50px;
  padding: 8px 18px; color: var(--text-secondary); cursor: pointer; font-size: 13px; transition: all 0.3s;
}
.back-btn:hover { border-color: var(--accent); color: var(--accent); }
.back-crumb { font-size: 13px; color: var(--text-muted); }

/* 大图 */
.gallery-hero { max-width: 1280px; margin: 20px auto 0; padding: 0 48px; }
.gallery-main { border-radius: var(--radius-xl); overflow: hidden; aspect-ratio: 21/9; background: #e8ddd0; }
.gallery-img { width: 100%; height: 100%; object-fit: cover; }
.gallery-fb {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e0cdb5, #d4b896); position: relative;
}
.fb-texture { position: absolute; inset: 0; opacity: 0.06; background: repeating-linear-gradient(45deg, transparent, transparent 6px, rgba(0,0,0,0.08) 6px, rgba(0,0,0,0.08) 8px); }
.gallery-fb span { font-size: 32px; font-weight: 700; letter-spacing: 0.3em; color: rgba(255,255,255,0.15); position: relative; }

.gallery-strip { display: flex; gap: 8px; margin-top: 10px; }
.gs-item { width: 72px; height: 50px; border-radius: 8px; overflow: hidden; border: 2px solid transparent; cursor: pointer; opacity: 0.5; transition: all 0.3s; padding: 0; }
.gs-item.on { border-color: var(--accent); opacity: 1; }
.gs-item:hover { opacity: 0.8; }
.gs-item img { width: 100%; height: 100%; object-fit: cover; }

/* 内容 */
.detail-body { max-width: 1280px; margin: 40px auto 0; padding: 0 48px 80px; display: grid; grid-template-columns: 1fr 380px; gap: 48px; align-items: start; }
.detail-main { min-width: 0; }

.info-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 20px; flex-wrap: wrap; }
.head-tag { display: inline-block; padding: 4px 12px; border-radius: 50px; background: var(--accent-soft); color: var(--accent); font-size: 11px; font-weight: 600; letter-spacing: 0.08em; margin: 0 0 8px; }
.info-head h1 { font-size: 34px; font-weight: 700; color: var(--text-primary); margin: 0; }

.head-price { text-align: right; }
.hp-sym { font-size: 15px; color: var(--accent); }
.hp-num { font-size: 34px; font-weight: 700; color: var(--accent); }
.hp-suf { font-size: 13px; color: var(--text-muted); }

.stat-row { display: flex; gap: 12px; margin-top: 28px; }
.stat {
  flex: 1; text-align: center; padding: 18px 12px; border-radius: var(--radius-md);
  background: rgba(255,255,255,0.55); backdrop-filter: blur(6px); border: 1px solid rgba(255,255,255,0.7);
  box-shadow: var(--shadow-sm);
}
.stat b { display: block; font-size: 19px; font-weight: 700; color: var(--text-primary); }
.stat b small { font-size: 12px; font-weight: 400; color: var(--text-muted); }
.stat span { display: block; margin-top: 2px; font-size: 11px; color: var(--text-muted); }

.info-block { margin-top: 36px; }
.info-block h3 { font-size: 18px; font-weight: 650; color: var(--text-primary); margin: 0 0 14px; padding-bottom: 10px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.info-block p { font-size: 15px; line-height: 1.85; color: var(--text-secondary); white-space: pre-wrap; margin: 0; }

.fac-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(130px, 1fr)); gap: 10px; }
.fac {
  display: flex; align-items: center; gap: 8px; padding: 12px 14px;
  background: rgba(255,255,255,0.5); backdrop-filter: blur(4px);
  border: 1px solid rgba(0,0,0,0.04); border-radius: var(--radius-sm);
  font-size: 13px; color: var(--text-secondary);
}
.fac-ico { font-size: 15px; }

/* 侧边毛玻璃卡 */
.detail-side { position: sticky; top: 100px; }
.book-glass {
  background: rgba(255,255,255,0.65); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.7); border-radius: var(--radius-xl);
  padding: 28px; box-shadow: var(--shadow-md);
}
.bg-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 22px; }
.bg-head > span { font-size: 16px; font-weight: 650; color: var(--text-primary); }
.bg-price { font-size: 17px; font-weight: 700; color: var(--accent); }
.bg-price small { font-size: 11px; font-weight: 400; color: var(--text-muted); }

.bg-form { display: flex; flex-direction: column; gap: 14px; }
.bf { display: flex; flex-direction: column; gap: 4px; }
.bf label { font-size: 11px; font-weight: 650; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.08em; }
.bf :deep(.el-date-editor), .bf :deep(.el-select), .bf :deep(.el-input-number) { width: 100%; }
.bf-row { display: flex; gap: 10px; }
.bf-row .bf { flex: 1; }

.bg-sum { margin-top: 6px; padding: 14px; background: var(--accent-soft); border: 1px solid rgba(200,148,92,0.1); border-radius: var(--radius-sm); }
.sum-line { display: flex; justify-content: space-between; font-size: 13px; color: var(--text-secondary); padding: 2px 0; }
.sum-total { font-size: 17px; font-weight: 700; color: var(--accent); }

.book-btn {
  width: 100%; padding: 16px; border: none; border-radius: var(--radius-md);
  background: linear-gradient(135deg, #c8945c, #b07840);
  color: #fff; font-size: 16px; font-weight: 700; cursor: pointer;
  transition: all 0.3s; letter-spacing: 0.04em; margin-top: 2px;
}
.book-btn:hover { box-shadow: 0 8px 32px rgba(180,120,60,0.3); transform: translateY(-2px); }

/* 加载/404 */
.load-wrap { min-height: 100vh; }
.sk-hero { height: 450px; background: linear-gradient(90deg, #f0ebe4 25%, #f8f4ef 50%, #f0ebe4 75%); background-size: 200% 100%; animation: shim 1.5s infinite; border-radius: var(--radius-xl); margin: 120px 48px 0; }
@keyframes shim { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.nf { text-align: center; padding: 160px 20px; color: var(--text-secondary); }
.nf h2 { color: var(--text-primary); }
.nf a { color: var(--accent); text-decoration: none; margin-top: 14px; display: inline-block; font-weight: 600; }

/* Reviews */
.review-list { display: flex; flex-direction: column; gap: 14px; }
.review-card {
  padding: 16px 20px; border-radius: var(--radius-md);
  background: rgba(255,255,255,0.5); backdrop-filter: blur(4px);
  border: 1px solid rgba(0,0,0,0.04); box-shadow: var(--shadow-sm);
}
.review-hd { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.review-avatar {
  width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  background: var(--accent-soft); font-size: 18px; flex-shrink: 0;
}
.review-meta { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; flex: 1; }
.review-nick { font-weight: 650; font-size: 13px; color: var(--text-primary); }
.review-date { font-size: 11px; color: var(--text-muted); margin-left: auto; }
.review-text { font-size: 14px; line-height: 1.75; color: var(--text-secondary); margin: 0; }
.review-empty { font-size: 14px; color: var(--text-muted); text-align: center; padding: 24px 0; }
.review-form { margin-top: 18px; display: flex; flex-direction: column; gap: 10px; }
.review-submit {
  align-self: flex-end; padding: 10px 28px; border: none; border-radius: var(--radius-sm);
  background: linear-gradient(135deg, #c8945c, #b07840);
  color: #fff; font-size: 13px; font-weight: 650; cursor: pointer; transition: all 0.3s;
}
.review-submit:hover:not(:disabled) { box-shadow: 0 6px 20px rgba(180,120,60,0.3); transform: translateY(-1px); }
.review-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.review-login-hint { margin-top: 18px; text-align: center; font-size: 13px; color: var(--text-muted); }
.review-login-hint a { color: var(--accent); text-decoration: none; font-weight: 600; }

@media (max-width: 1024px) { .detail-body { grid-template-columns: 1fr; } .detail-side { position: static; } }
@media (max-width: 768px) {
  .back-strip, .gallery-hero, .detail-body { padding-left: 20px; padding-right: 20px; }
  .info-head h1 { font-size: 26px; }
  .stat-row { flex-wrap: wrap; }
}
</style>
