<template>
  <div class="room-list-page">
    <!-- Hero 区 —— 背景大图 + 毛玻璃搜索 -->
    <section class="hero-section">
      <div class="hero-bg-layer">
        <img src="/images/bg/hero-bg.jpg" class="hero-bg-img" alt="" />
        <div class="hero-overlay"></div>
      </div>

      <div class="hero-content">
        <p class="hero-eyebrow">CURATED SELECTION</p>
        <h1 class="hero-title">寻一处静谧，享一段时光</h1>
        <p class="hero-subtitle">每一间客房，皆以温润之姿，待您亲临</p>

        <!-- 毛玻璃搜索卡 -->
        <div class="search-glass">
          <div class="search-row">
            <div class="search-field">
              <label>入住</label>
              <el-date-picker
                v-model="searchForm.checkIn"
                type="date"
                placeholder="选择日期"
                :disabled-date="disablePastDates"
                format="MM/DD"
                value-format="YYYY-MM-DD"
              />
            </div>
            <span class="search-sep">→</span>
            <div class="search-field">
              <label>离店</label>
              <el-date-picker
                v-model="searchForm.checkOut"
                type="date"
                placeholder="选择日期"
                :disabled-date="disablePastCheckOut"
                format="MM/DD"
                value-format="YYYY-MM-DD"
              />
            </div>
            <span class="search-div"></span>
            <div class="search-field">
              <label>人数</label>
              <el-select v-model="searchForm.guests" placeholder="人数">
                <el-option v-for="n in 6" :key="n" :label="n + ' 位'" :value="n" />
              </el-select>
            </div>
            <button class="search-btn" @click="handleSearch">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              <span>检索</span>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 结果区 -->
    <section class="results-section">
      <div class="section-head">
        <div>
          <p class="sec-eyebrow">AVAILABLE ROOMS</p>
          <h2 v-if="!searchPerformed">全部房型</h2>
          <h2 v-else>可订客房 <span class="count-badge">{{ total }}</span></h2>
        </div>
        <span class="result-hint" v-if="searchPerformed">
          {{ searchForm.checkIn }} — {{ searchForm.checkOut }} · {{ searchForm.guests }}位
        </span>
      </div>

      <!-- 加载骨架 -->
      <div v-if="loading" class="room-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="sk-img"></div>
          <div class="sk-body">
            <div class="sk-line w-50"></div>
            <div class="sk-line w-35"></div>
          </div>
        </div>
      </div>

      <!-- 空态 -->
      <div v-else-if="searchPerformed && rooms.length === 0" class="empty-state">
        <span class="empty-icon">🛏️</span>
        <h3>所选日期暂无可订客房</h3>
        <p>不妨调整日期或人数，邂逅属于您的那一间</p>
      </div>

      <!-- 卡片网格 -->
      <div v-else class="room-grid">
        <article
          v-for="(room, index) in rooms"
          :key="room.id"
          class="room-card"
          :style="{ animationDelay: index * 0.06 + 's' }"
          @click="goDetail(room.id)"
        >
          <div class="card-img-box">
            <img
              v-if="room.mainImage"
              :src="room.mainImage"
              :alt="room.name"
              class="card-img"
            />
            <div v-else class="card-img-fb">
              <div class="fb-pattern"></div>
              <span>ZHISU</span>
            </div>
            <div class="card-img-tag" v-if="room.category">{{ room.category }}</div>
            <div class="card-img-fade"></div>
          </div>

          <div class="card-info">
            <div class="card-top">
              <h3>{{ room.name }}</h3>
              <div class="card-price">
                <span class="p-sym">¥</span>
                <span class="p-val">{{ room.displayPrice }}</span>
                <span class="p-unit">{{ room.priceUnit }}</span>
              </div>
            </div>

            <p class="card-desc" v-if="room.description">{{ room.description }}</p>

            <div class="card-meta">
              <span v-if="room.area"><i class="mi">◻</i>{{ room.area }}m²</span>
              <span v-if="room.bedType"><i class="mi">🛏</i>{{ room.bedType }}</span>
              <span v-if="room.maxGuests"><i class="mi">👤</i>限{{ room.maxGuests }}人</span>
            </div>

            <div class="card-tags">
              <span v-if="room.hasWifi" class="tag">Wi‑Fi</span>
              <span v-if="room.hasBreakfast" class="tag">含早</span>
              <span v-if="room.hasWindow" class="tag">有窗</span>
              <span v-if="room.hasAc" class="tag">空调</span>
            </div>

            <div class="card-foot">
              <span class="foot-link">
                查看详情
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
              </span>
            </div>
          </div>
        </article>
      </div>

      <!-- 分页 -->
      <div class="page-wrap" v-if="total > searchForm.size">
        <el-pagination
          v-model:current-page="searchForm.page"
          :page-size="searchForm.size"
          :total="total"
          background
          layout="prev, pager, next"
          @current-change="fetchRooms"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchRoomTypes } from '@/api/roomTypes'
import { normalizeBookingQuery, toBookingQuery } from '@/utils/bookingQuery'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const initialHadDates = Boolean(route.query.checkInDate || route.query.checkIn || route.query.checkOutDate || route.query.checkOut)
const searchPerformed = ref(initialHadDates)
const rooms = ref([])
const total = ref(0)

function dateOffset(days) { const d = new Date(); d.setDate(d.getDate() + days); return d.toISOString().slice(0, 10) }
const initial = normalizeBookingQuery(route.query, { checkInDate: dateOffset(1), checkOutDate: dateOffset(2), roomCount: 1, guests: 1 })
const searchForm = reactive({ page: 1, size: 9, checkIn: initial.checkInDate, checkOut: initial.checkOutDate, roomCount: initial.roomCount, guests: initial.guests, keyword: '' })

function disablePastDates(time) {
  const t = new Date(); t.setHours(0, 0, 0, 0); return time.getTime() < t.getTime()
}
function disablePastCheckOut(time) {
  if (!searchForm.checkIn) return disablePastDates(time)
  const n = new Date(searchForm.checkIn); n.setDate(n.getDate() + 1)
  return time.getTime() < n.getTime()
}
function currentQuery(roomTypeId) {
  return toBookingQuery({ checkInDate: searchForm.checkIn, checkOutDate: searchForm.checkOut, roomCount: searchForm.roomCount, guests: searchForm.guests }, roomTypeId)
}
function handleSearch() {
  if (!searchForm.checkIn || !searchForm.checkOut) return ElMessage.warning('请选择完整的入住和离店日期')
  searchForm.page = 1
  searchPerformed.value = true
  router.replace({ path: '/rooms', query: currentQuery() })
  fetchRooms()
}
async function fetchRooms() {
  loading.value = true
  try {
    const res = await searchRoomTypes({ checkInDate: searchForm.checkIn, checkOutDate: searchForm.checkOut, roomCount: searchForm.roomCount, guests: searchForm.guests })
    if (res.code !== 'SUCCESS') throw new Error(res.message || '加载房型失败')
    const all = (res.data || []).map(room => ({
      ...room,
      mainImage: room.mainImageUrl,
      hasWifi: room.facilities?.some(item => /wifi/i.test(item)),
      hasAc: room.facilities?.some(item => item.includes('空调')),
      displayPrice: searchPerformed.value ? room.totalAmount : room.basePrice,
      priceUnit: searchPerformed.value ? ' / 本次入住' : ' / 晚起'
    }))
    total.value = all.length
    const start = (searchForm.page - 1) * searchForm.size
    rooms.value = all.slice(start, start + searchForm.size)
  } catch (e) {
    rooms.value = []
    total.value = 0
    ElMessage.error(e?.response?.data?.message || e.message || '加载房型失败')
  } finally { loading.value = false }
}
function goDetail(id) { router.push({ path: `/rooms/${id}`, query: currentQuery(id) }) }
onMounted(fetchRooms)
</script>

<style scoped>
/* ===== Hero ===== */
.hero-section {
  position: relative;
  min-height: 580px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 120px 48px 80px;
  overflow: hidden;
}

.hero-bg-layer { position: absolute; inset: 0; }
.hero-bg-img {
  width: 100%; height: 100%; object-fit: cover;
}
.hero-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.25) 0%, rgba(0,0,0,0.05) 40%, rgba(0,0,0,0.25) 100%);
}

.hero-content {
  position: relative; text-align: center; max-width: 860px; z-index: 1;
}

.hero-eyebrow {
  font-size: 11px; font-weight: 600; letter-spacing: 0.3em;
  color: rgba(255,255,255,0.85); margin: 0 0 16px;
}

.hero-title {
  font-size: clamp(36px, 5vw, 56px); font-weight: 700;
  color: #fff; margin: 0 0 12px;
  text-shadow: 0 4px 24px rgba(0,0,0,0.12);
}

.hero-subtitle {
  font-size: 16px; color: rgba(255,255,255,0.82);
  margin: 0 0 40px; font-weight: 300;
}

/* 毛玻璃搜索卡 */
.search-glass {
  background: rgba(255,255,255,0.22);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255,255,255,0.45);
  border-radius: 22px;
  padding: 6px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.08);
}

.search-row {
  display: flex; align-items: center;
}

.search-field {
  flex: 1; padding: 14px 18px; text-align: left;
}

.search-field label {
  display: block; font-size: 10px; font-weight: 700;
  color: rgba(255,255,255,0.7); text-transform: uppercase; letter-spacing: 0.14em; margin-bottom: 4px;
}

.search-field :deep(.el-date-editor),
.search-field :deep(.el-select) { width: 100%; }
.search-field :deep(.el-input__wrapper) {
  background: transparent; box-shadow: none; padding: 0;
}
.search-field :deep(.el-input__inner) { color: #fff; font-size: 15px; font-weight: 500; }
.search-field :deep(.el-input__inner::placeholder) { color: rgba(255,255,255,0.5); }

.search-sep { color: rgba(255,255,255,0.5); font-size: 18px; flex-shrink: 0; }
.search-div { width: 1px; height: 36px; background: rgba(255,255,255,0.2); flex-shrink: 0; }

.search-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 16px 32px; margin: 6px;
  border: none; border-radius: 16px;
  background: linear-gradient(135deg, #c8945c, #b07840);
  color: #fff; font-size: 15px; font-weight: 600; cursor: pointer;
  transition: all 0.3s; white-space: nowrap; flex-shrink: 0;
}
.search-btn:hover { box-shadow: 0 8px 28px rgba(180,120,60,0.35); transform: translateY(-1px); }

/* ===== 结果区 ===== */
.results-section { max-width: 1280px; margin: 0 auto; padding: 56px 48px 80px; }

.section-head {
  display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 36px;
}
.sec-eyebrow { font-size: 11px; font-weight: 600; letter-spacing: 0.22em; color: var(--accent); margin: 0 0 6px; }
.section-head h2 { font-size: 30px; font-weight: 700; color: var(--text-primary); margin: 0; }
.count-badge {
  font-size: 14px; font-weight: 600; color: var(--accent);
  background: var(--accent-soft); padding: 4px 12px; border-radius: 50px; margin-left: 6px;
}
.result-hint { font-size: 13px; color: var(--text-muted); }

/* 骨架 */
@keyframes shim { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.skeleton-card { background: #fff; border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }
.sk-img { height: 240px; background: linear-gradient(90deg, #f0ebe4 25%, #f8f4ef 50%, #f0ebe4 75%); background-size: 200% 100%; animation: shim 1.5s infinite; }
.sk-body { padding: 20px; display: flex; flex-direction: column; gap: 10px; }
.sk-line { height: 14px; background: linear-gradient(90deg, #f0ebe4 25%, #f8f4ef 50%, #f0ebe4 75%); background-size: 200% 100%; animation: shim 1.5s infinite; border-radius: 6px; }
.sk-line.w-50 { width: 50%; } .sk-line.w-35 { width: 35%; }

/* 空态 */
.empty-state { text-align: center; padding: 80px 20px; }
.empty-icon { font-size: 48px; display: block; margin-bottom: 12px; }
.empty-state h3 { font-size: 20px; color: var(--text-primary); margin: 0 0 8px; }
.empty-state p { color: var(--text-muted); margin: 0; }

/* ===== 卡片网格 ===== */
.room-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 28px; }

.room-card {
  background: #fff;
  border-radius: var(--radius-xl);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: all 0.45s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation: cardUp 0.55s ease both;
}
@keyframes cardUp { from { opacity: 0; transform: translateY(28px); } to { opacity: 1; transform: translateY(0); } }

.room-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 24px 56px rgba(44,36,22,0.12), 0 0 0 1px rgba(200,148,92,0.12);
}

/* 图片区 */
.card-img-box { position: relative; height: 260px; overflow: hidden; }
.card-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.7s ease; }
.room-card:hover .card-img { transform: scale(1.06); }

.card-img-fb {
  width: 100%; height: 100%;
  background: linear-gradient(135deg, #e8d5c4, #d4b896, #cfb590);
  display: flex; align-items: center; justify-content: center; position: relative;
}
.fb-pattern {
  position: absolute; inset: 0; opacity: 0.08;
  background: repeating-linear-gradient(45deg, transparent, transparent 8px, rgba(0,0,0,0.05) 8px, rgba(0,0,0,0.05) 10px);
}
.card-img-fb span { font-size: 24px; font-weight: 700; letter-spacing: 0.24em; color: rgba(255,255,255,0.2); position: relative; }
.card-img-fade { position: absolute; bottom: 0; left:0; right:0; height: 70px; background: linear-gradient(transparent, rgba(0,0,0,0.08)); }

.card-img-tag {
  position: absolute; top: 14px; left: 14px;
  padding: 5px 14px; border-radius: 50px;
  background: rgba(255,255,255,0.8); backdrop-filter: blur(6px);
  color: var(--accent); font-size: 11px; font-weight: 600; letter-spacing: 0.06em;
}

/* 信息区 */
.card-info { padding: 22px; }
.card-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 10px; }
.card-top h3 { margin: 0; font-size: 19px; font-weight: 650; color: var(--text-primary); }
.card-price { text-align: right; white-space: nowrap; }
.p-sym { font-size: 12px; color: var(--accent); font-weight: 600; }
.p-val { font-size: 24px; font-weight: 700; color: var(--accent); }
.p-unit { font-size: 11px; color: var(--text-muted); font-weight: 400; }

.card-desc {
  margin: 10px 0 0; font-size: 13px; color: var(--text-secondary); line-height: 1.65;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.card-meta { display: flex; gap: 14px; margin-top: 14px; flex-wrap: wrap; }
.card-meta span { font-size: 12px; color: var(--text-secondary); display: inline-flex; align-items: center; gap: 3px; }
.mi { font-style: normal; font-size: 13px; }

.card-tags { display: flex; gap: 6px; margin-top: 12px; }
.tag {
  padding: 4px 10px; border-radius: 7px;
  background: var(--sage-soft); border: 1px solid rgba(139,157,131,0.18);
  color: var(--sage); font-size: 11px; font-weight: 500;
}

.card-foot { margin-top: 16px; display: flex; justify-content: flex-end; }
.foot-link {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 13px; font-weight: 650; color: var(--accent); transition: gap 0.3s;
}
.room-card:hover .foot-link { gap: 9px; }

/* 分页 */
.page-wrap { display: flex; justify-content: center; margin-top: 48px; }
.page-wrap :deep(.el-pager li.is-active) { background: var(--accent); color: #fff; border-radius: 8px; }
.page-wrap :deep(.el-pagination button:hover) { color: var(--accent); }

/* 响应式 */
@media (max-width: 1024px) { .room-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .hero-section { padding: 100px 20px 50px; min-height: auto; }
  .hero-title { font-size: 28px; }
  .search-row { flex-direction: column; }
  .search-field { padding: 10px 14px; }
  .search-sep, .search-div { display: none; }
  .search-btn { width: calc(100% - 12px); justify-content: center; }
  .results-section { padding: 32px 20px 60px; }
  .room-grid { grid-template-columns: 1fr; }
}
</style>
