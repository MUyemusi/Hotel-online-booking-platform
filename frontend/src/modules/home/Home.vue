
<template>
  <div class="home-page">
    <section class="hero">
      <picture>
        <source media="(max-width: 720px)" :srcset="heroImage" />
        <img class="hero-image" :src="heroImage" :alt="hotelName + '外景'" fetchpriority="high" />
      </picture>
      <div class="hero-shade"></div>
      <div class="hero-copy">
        <p class="eyebrow">ZHISU · COASTAL RETREAT</p>
        <h1>向海而居，<br />让自然住进旅程</h1>
        <p class="hero-text">以海风、原木与日光，构筑一段安静而丰盛的海滨假日。</p>
        <div class="hero-actions">
          <router-link to="/rooms" class="primary-link">探索客房 <span>→</span></router-link>
          <a href="#experience" class="ghost-link">了解智宿</a>
        </div>
      </div>
      <div class="hero-note">
        <span class="note-line"></span>
        <div><small>OUR PHILOSOPHY</small><p>自然并非装饰，而是每一次入住的主角</p></div>
      </div>
      <a href="#booking" class="scroll-hint"><span></span>SCROLL</a>
    </section>

    <section id="booking" class="booking-wrap">
      <div class="booking-card">
        <div class="booking-intro"><span>RESERVATION</span><strong>{{ hotelName }}</strong><el-checkbox v-if="earlyMorningAvailable" v-model="earlyMode">凌晨入住（当日中午退房）</el-checkbox></div>
        <div class="booking-field"><small>入住日期</small><el-date-picker v-model="booking.checkInDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" :disabled-date="disablePast"><template #default="cell"><span>{{ cell.text }}</span><sup v-if="isEarlyCell(cell)">凌</sup></template></el-date-picker></div>
        <div class="booking-field"><small>离店日期</small><el-date-picker v-model="booking.checkOutDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" :disabled-date="disableCheckOut" /></div>
        <div class="booking-field"><small>入住人数</small><el-select v-model="booking.guests"><el-option v-for="n in 6" :key="n" :label="`${n} 位宾客`" :value="n" /></el-select></div>
        <button class="booking-btn" @click="searchRooms">查询客房 <span>→</span></button>
      </div>
    </section>

    <section id="experience" class="story section-shell">
      <div class="story-copy">
        <p class="eyebrow dark">A SLOWER RHYTHM</p>
        <h2>住进海岸，<br />也住进自然的节奏</h2>
        <p>{{ hotelIntroduction }}</p>
        <div class="story-metrics"><div><strong>7</strong><span>种特色房型</span></div><div><strong>海岸</strong><span>自然景观体验</span></div><div><strong>一站式</strong><span>预订与入住服务</span></div></div>
      </div>
      <div class="story-image"><img src="/images/eco/zhisu-courtyard.png" alt="酒店自然庭院" loading="lazy" /><span>石 · 木 · 水 · 光</span></div>
    </section>

    <section class="stay-section">
      <div class="section-shell">
        <div class="section-title"><div><p class="eyebrow dark">STAY WITH NATURE</p><h2>一间房，一片海</h2></div><router-link to="/rooms">查看全部房型 <span>→</span></router-link></div>
        <article class="room-feature">
          <img src="/images/eco/zhisu-ocean-suite.png" alt="智宿海景套房" loading="lazy" />
          <div class="room-panel"><p>OCEAN SUITE</p><h3>临海自然套房</h3><span>让晨光越过海面抵达枕边，让原木与亚麻留住松弛的触感。</span><ul><li>独立海景露台</li><li>天然材质客房</li><li>舒适起居空间</li></ul><router-link to="/rooms">发现更多 <b>→</b></router-link></div>
        </article>
      </div>
    </section>

    <section class="experience-grid section-shell">
      <div class="experience-copy"><p class="eyebrow dark">TASTE THE COAST</p><h2>让味觉，也抵达海边</h2><p>从清晨的在地早餐，到暮色中的临海晚餐，我们以当季食材和自然风味，延续海岸生活的轻盈与丰盛。</p></div>
      <img src="/images/eco/zhisu-seaside-dining.png" alt="智宿临海餐厅" loading="lazy" />
    </section>

    <MapSection
      :latitude="hotelLatitude"
      :longitude="hotelLongitude"
      :address="hotelAddress"
      :contact-phone="hotelPhone"
      :traffic-info="hotelTraffic"
    />

    <NearbyRecommendation
        :latitude="hotelLatitude"
        :longitude="hotelLongitude"
    />

    <section v-if="announcements.length" class="announcement-section section-shell">
      <div class="section-title compact"><div><p class="eyebrow dark">NEWS & STORIES</p><h2>酒店讯息</h2></div></div>
      <div class="announcement-list">
        <article v-for="item in announcements" :key="item.id" class="announcement-row">
          <div class="announcement-meta">
            <small>{{ formatDate(item.publishTime) }}</small>
            <h3>{{ item.title }}</h3>
          </div>
          <div class="announcement-body">
            <div class="announcement-content" :class="{ expanded: expandedAnnouncementIds.includes(item.id) }">
              <p v-for="(paragraph, index) in formatAnnouncementParagraphs(item.content)" :key="`${item.id}-${index}`">
                {{ paragraph }}
              </p>
            </div>
            <button
              v-if="shouldShowAnnouncementToggle(item.content)"
              type="button"
              class="announcement-toggle"
              @click="toggleAnnouncement(item.id)"
            >
              {{ expandedAnnouncementIds.includes(item.id) ? '收起' : '展开全文' }}
            </button>
          </div>
        </article>
      </div>
    </section>

    <div v-if="!healthy" class="health-pill" :class="{ failed: healthFailed }">{{ healthFailed ? '服务暂时离线' : '正在连接服务…' }}</div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { checkHealth } from '@/api/health'
import { getPublicAnnouncements } from '@/api/content/announcement'
import { getPublicBanners } from '@/api/content/banner'
import { getPublicHotelInfo } from '@/api/content/hotelInfo'
import { toBookingQuery } from '@/utils/bookingQuery'
import { hotelToday, isEarlyMorningAvailable, nextDate } from '@/utils/bookingStay'
import MapSection from '@/components/MapSection.vue'
import NearbyRecommendation from '@/components/NearbyRecommendation.vue'

const router = useRouter()
const announcements = ref([])
const expandedAnnouncementIds = ref([])
const banners = ref([])
const bannerIndex = ref(0)
const hotelName = ref('智宿海滨生态酒店')
const hotelIntroduction = ref('浅色石材保留海岸的温度，木质格栅筛下柔和日光，庭院与海景在室内自然延伸。智宿以克制的设计，让每一处空间都安静、通透而有生命力。')
const healthy = ref(false)
const healthFailed = ref(false)
const booking = reactive({ checkInDate: '', checkOutDate: '', roomCount: 1, guests: 2, stayType: 'STANDARD' })
const earlyMorningAvailable = isEarlyMorningAvailable()
const earlyMode = computed({
  get: () => booking.stayType === 'EARLY_MORNING',
  set: enabled => {
    booking.stayType = enabled ? 'EARLY_MORNING' : 'STANDARD'
    if (enabled) booking.checkInDate = booking.checkOutDate = hotelToday()
    else if (booking.checkInDate) booking.checkOutDate = nextDate(booking.checkInDate)
  }
})
let bannerTimer
const heroImage = computed(() => banners.value[bannerIndex.value]?.imageUrl || '/images/eco/zhisu-coastal-hero.png')
function disablePast(t) {
  if (earlyMode.value) return t.toLocaleDateString('sv-SE') !== hotelToday()
  const d = new Date(); d.setHours(0, 0, 0, 0); return t.getTime() < d.getTime()
}
function disableCheckOut(t) {
  if (earlyMode.value) return t.toLocaleDateString('sv-SE') !== hotelToday()
  if (!booking.checkInDate) return disablePast(t)
  const d = new Date(booking.checkInDate); d.setDate(d.getDate() + 1); return t.getTime() < d.getTime()
}
function isEarlyCell(cell) { return earlyMorningAvailable && cell?.dayjs?.format?.('YYYY-MM-DD') === hotelToday() }
function searchRooms() {
  if ((booking.checkInDate && !booking.checkOutDate) || (!booking.checkInDate && booking.checkOutDate)) return ElMessage.warning('请选择完整的入住和离店日期')
  router.push({ path: '/rooms', query: toBookingQuery(booking) })
}
watch(() => booking.checkInDate, value => {
  if (booking.stayType === 'STANDARD' && value && (!booking.checkOutDate || booking.checkOutDate <= value)) booking.checkOutDate = nextDate(value)
})
function formatDate(v) { return v ? v.slice(0, 10).replaceAll('-', '.') : '' }
function toggleAnnouncement(id) {
  expandedAnnouncementIds.value = expandedAnnouncementIds.value.includes(id)
    ? expandedAnnouncementIds.value.filter(item => item !== id)
    : [...expandedAnnouncementIds.value, id]
}
function shouldShowAnnouncementToggle(content) {
  return typeof content === 'string' && content.trim().length > 90
}
function formatAnnouncementParagraphs(content) {
  if (typeof content !== 'string') return []
  const normalized = content
    .replace(/\r\n/g, '\n')
    .replace(/\u3000/g, ' ')
    .replace(/([\uFF1A:])\s*(\u5C0A\u656C\u7684\u5BBE\u5BA2|\u60A8\u597D|\u4EB2\u7231\u7684\u5BBE\u5BA2|\u5404\u4F4D\u5BBE\u5BA2)/g, '$1\n\n$2')
    .replace(/(\u60A8\u597D\uFF01?)/g, '$1\n\n')
    .replace(/(\u611F\u8C22\u60A8\u7684\u7406\u89E3\u4E0E\u914D\u5408\uFF0C\u795D\u60A8\u5165\u4F4F\u6109\u5FEB\uFF01?)/g, '\n\n$1')
    .replace(/(\u7531\u6B64\u7ED9\u60A8\u5E26\u6765\u7684\u4E0D\u4FBF\uFF0C\u656C\u8BF7\u8C05\u89E3\u3002?)/g, '\n\n$1')
    .replace(/(\u611F\u8C22\u60A8\u5BF9\u9152\u5E97\u5DE5\u4F5C\u7684\u652F\u6301\u4E0E\u914D\u5408\uFF01?)/g, '\n\n$1')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
  return normalized
    .split(/\n\n+/)
    .map(part => part.replace(/\s*\n\s*/g, ' ').trim())
    .filter(Boolean)
}
function startBannerRotation() {
  if (banners.value.length < 2) return
  bannerTimer = window.setInterval(() => { bannerIndex.value = (bannerIndex.value + 1) % banners.value.length }, 6000)
}
onMounted(async () => {
  try { const r = await checkHealth(); healthy.value = r.code === 'SUCCESS'; healthFailed.value = !healthy.value } catch { healthFailed.value = true }
  try { const r = await getPublicAnnouncements(); if (r.code === 'SUCCESS') announcements.value = r.data || [] } catch { announcements.value = [] }
  try {
    const r = await getPublicHotelInfo()
    if (r.code === 'SUCCESS' && r.data) {
      hotelName.value = r.data.hotelName || hotelName.value
      hotelIntroduction.value = r.data.introduction || hotelIntroduction.value
      hotelAddress.value = r.data.address || ''
      hotelPhone.value = r.data.contactPhone || ''
      hotelTraffic.value = r.data.trafficInfo || ''
      hotelLatitude.value = r.data.latitude || null
      hotelLongitude.value = r.data.longitude || null
    }
  } catch { /* keep current visual fallback */ }
  try {
    const r = await getPublicBanners()
    if (r.code === 'SUCCESS') banners.value = (r.data || []).filter(item => item.imageUrl)
    startBannerRotation()
  } catch { banners.value = [] }
})
onBeforeUnmount(() => { if (bannerTimer) window.clearInterval(bannerTimer) })
</script>

<style scoped>
.home-page{background:#f7f2e9;color:#302a24;overflow:hidden}.hero{position:relative;height:min(860px,94vh);min-height:680px;color:#fff}.hero-image,.hero-shade{position:absolute;inset:0;width:100%;height:100%}.hero-image{object-fit:cover}.hero-shade{background:linear-gradient(90deg,rgba(25,35,30,.74) 0%,rgba(31,39,34,.32) 42%,rgba(25,30,27,.08) 70%),linear-gradient(0deg,rgba(18,23,20,.38),transparent 45%)}.hero-copy{position:relative;z-index:2;max-width:1280px;margin:auto;padding:190px 48px 0}.eyebrow{margin:0 0 20px;font-size:11px;font-weight:650;letter-spacing:.3em;color:#e4d2b6}.eyebrow.dark{color:#8c6d4f}.hero h1{margin:0;font-family:"Noto Serif SC","Songti SC",serif;font-size:clamp(48px,6vw,82px);font-weight:500;line-height:1.18;letter-spacing:.04em}.hero-text{max-width:520px;margin:26px 0 0;font-size:16px;line-height:1.9;color:rgba(255,255,255,.82)}.hero-actions{display:flex;align-items:center;gap:28px;margin-top:38px}.primary-link,.ghost-link{display:inline-flex;align-items:center;gap:35px;color:#fff;text-decoration:none}.primary-link{padding:15px 24px;background:#a4774f;font-weight:650}.primary-link:hover{background:#8f623f}.ghost-link{padding-bottom:6px;border-bottom:1px solid rgba(255,255,255,.45)}.hero-note{position:absolute;z-index:2;right:5%;bottom:72px;display:flex;align-items:center;gap:16px;max-width:340px}.note-line{width:46px;height:1px;background:#d9c3a1}.hero-note small{letter-spacing:.18em;color:#d9c3a1}.hero-note p{margin:6px 0 0;font-family:"Noto Serif SC",serif}.scroll-hint{position:absolute;z-index:2;left:48px;bottom:24px;display:flex;align-items:center;gap:10px;color:rgba(255,255,255,.65);font-size:9px;letter-spacing:.24em;text-decoration:none;transform:rotate(-90deg);transform-origin:left center}.scroll-hint span{display:block;width:38px;height:1px;background:rgba(255,255,255,.5)}.booking-wrap{position:relative;z-index:3;max-width:1180px;margin:-34px auto 0;padding:0 30px}.booking-card{display:grid;grid-template-columns:1.3fr repeat(3,1fr) auto;align-items:center;gap:0;padding:18px 18px 18px 26px;background:#fffdf8;box-shadow:0 24px 60px rgba(49,40,31,.14)}.booking-intro,.booking-field{padding:4px 22px;border-right:1px solid #e7dfd3}.booking-intro span,.booking-field small{display:block;margin-bottom:7px;font-size:9px;letter-spacing:.17em;color:#917c68}.booking-intro strong{font-family:"Noto Serif SC",serif;font-size:17px}.booking-field :deep(.el-input__wrapper),.booking-field :deep(.el-select__wrapper){padding:0;background:transparent;box-shadow:none!important}.booking-btn{align-self:stretch;margin-left:18px;padding:0 26px;border:0;background:#33483b;color:#fff;cursor:pointer;font-weight:650}.booking-btn span{margin-left:18px}.section-shell{width:min(1180px,calc(100% - 60px));margin-inline:auto}.story{display:grid;grid-template-columns:.85fr 1.15fr;gap:100px;align-items:center;padding:145px 0}.story-copy h2,.section-title h2,.experience-copy h2{margin:0;font-family:"Noto Serif SC","Songti SC",serif;font-size:clamp(34px,4vw,52px);font-weight:500;line-height:1.35}.story-copy>p:last-of-type,.experience-copy>p:last-child{margin:28px 0 0;line-height:2;color:#756b61}.story-image{position:relative}.story-image img{width:100%;height:550px;object-fit:cover}.story-image span{position:absolute;right:-28px;bottom:32px;padding:18px 15px;background:#33483b;color:#fff;font-size:12px;letter-spacing:.18em;writing-mode:vertical-rl}.story-metrics{display:flex;gap:28px;margin-top:38px;padding-top:28px;border-top:1px solid #ded4c7}.story-metrics div{display:flex;flex-direction:column}.story-metrics strong{font-family:Georgia,serif;font-size:30px;color:#33483b}.story-metrics span{margin-top:4px;font-size:12px;color:#8f8378}.stay-section{padding:115px 0 125px;background:#e9ddc8}.section-title{display:flex;align-items:flex-end;justify-content:space-between;margin-bottom:45px}.section-title a{padding-bottom:8px;color:#5e554d;text-decoration:none;border-bottom:1px solid #a4774f}.room-feature{display:grid;grid-template-columns:1.45fr .75fr;min-height:540px}.room-feature>img{width:100%;height:100%;object-fit:cover}.room-panel{display:flex;flex-direction:column;justify-content:center;padding:55px;background:#33483b;color:#fff}.room-panel>p{font-size:10px;letter-spacing:.24em;color:#c8b18e}.room-panel h3{margin:10px 0 20px;font-family:"Noto Serif SC",serif;font-size:34px;font-weight:500}.room-panel>span{line-height:1.9;color:rgba(255,255,255,.72)}.room-panel ul{margin:28px 0;padding:0;list-style:none}.room-panel li{padding:9px 0;border-bottom:1px solid rgba(255,255,255,.12);color:rgba(255,255,255,.8)}.room-panel a{align-self:flex-start;margin-top:12px;color:#fff;text-decoration:none;border-bottom:1px solid #b99a6b;padding-bottom:5px}.room-panel b{margin-left:35px}.experience-grid{display:grid;grid-template-columns:.72fr 1.28fr;gap:80px;align-items:center;padding:140px 0}.experience-grid img{width:100%;height:500px;object-fit:cover}.announcement-section{padding-bottom:100px}.section-title.compact{margin-bottom:30px}.announcement-list{display:flex;flex-direction:column;gap:18px}.announcement-row{display:grid;grid-template-columns:minmax(220px,.34fr) minmax(0,1fr);gap:28px;align-items:start;padding:30px 34px;background:#fffdf8;border-top:2px solid #a4774f;box-shadow:0 16px 36px rgba(49,40,31,.08)}.announcement-meta small{display:block;color:#9b8c7d}.announcement-meta h3{margin:12px 0 0;font-size:26px;line-height:1.35}.announcement-content{display:-webkit-box;overflow:hidden;-webkit-line-clamp:5;-webkit-box-orient:vertical}.announcement-content.expanded{display:block;overflow:visible}.announcement-content p{margin:0 0 14px;color:#756b61;line-height:2;text-indent:2em}.announcement-content p:first-child{text-indent:0;font-weight:500;color:#4f443a}.announcement-content p:last-child{margin-bottom:0}.announcement-toggle{margin-top:16px;padding:0;border:0;background:transparent;color:#a4774f;cursor:pointer;font-size:14px;font-weight:650}.announcement-toggle:hover{color:#8f623f}.health-pill{position:fixed;z-index:30;right:20px;bottom:20px;padding:8px 13px;border-radius:30px;background:#d9c7a9;color:#55483b;font-size:12px}.health-pill.failed{background:#7e4b3c;color:#fff}
@media(max-width:980px){.booking-card{grid-template-columns:1fr 1fr}.booking-intro{grid-column:1/-1}.booking-intro,.booking-field{border:0}.booking-btn{height:48px}.story,.experience-grid{grid-template-columns:1fr;gap:45px}.story{padding:100px 0}.room-feature{grid-template-columns:1fr}.room-panel{padding:40px}.story-image img,.experience-grid img{height:440px}}
@media(max-width:680px){.hero{min-height:720px}.hero-copy{padding:170px 22px 0}.hero h1{font-size:44px}.hero-note{right:20px;bottom:70px}.scroll-hint{display:none}.booking-wrap{padding:0 16px}.booking-card{grid-template-columns:1fr;padding:20px}.booking-field{padding:10px 0;border-bottom:1px solid #eee4d8}.booking-btn{margin:16px 0 0}.section-shell{width:calc(100% - 40px)}.story{padding:85px 0}.story-image img,.experience-grid img{height:330px}.story-image span{right:-10px}.story-metrics{gap:15px}.story-metrics strong{font-size:24px}.stay-section{padding:80px 0}.section-title{align-items:flex-start;gap:20px}.section-title a{font-size:12px}.experience-grid{padding:90px 0}.announcement-row{grid-template-columns:1fr;gap:18px;padding:24px 22px}.announcement-meta h3{font-size:22px}}
.hero picture{position:absolute;inset:0;width:100%;height:100%}.hero picture .hero-image{image-rendering:auto}.story-metrics strong{font-family:"Noto Serif SC",Georgia,serif;font-size:27px}
</style>
