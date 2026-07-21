<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero-bg-img"></div>
      <div class="hero-copy">
        <p class="eyebrow">Zhisu Hotel</p>
        <h1>住得舒适，也住得有仪式感</h1>
        <p class="hero-text">
          于繁华之外，寻一处雅居，让每一次停留都成为风景
        </p>
        <div class="hero-actions">
          <router-link to="/rooms">
            <el-button type="primary" size="large">立即预订</el-button>
          </router-link>
          <router-link to="/rooms">
            <el-button size="large" plain>查看房型</el-button>
          </router-link>
        </div>
      </div>

      <div class="hero-visual">
        <el-carousel
          v-if="banners.length"
          height="360px"
          indicator-position="outside"
          class="banner-carousel"
        >
          <el-carousel-item v-for="banner in banners" :key="banner.id">
            <a
              :href="banner.linkUrl || 'javascript:void(0)'"
              class="banner-card"
              :target="banner.linkUrl ? '_blank' : undefined"
              :rel="banner.linkUrl ? 'noreferrer' : undefined"
              @click.prevent="handleBannerClick(banner)"
            >
              <img :src="banner.imageUrl" :alt="banner.title" class="banner-image" />
              <div class="banner-overlay">
                <span class="banner-badge">已发布轮播图</span>
                <h2>{{ banner.title }}</h2>
              </div>
            </a>
          </el-carousel-item>
        </el-carousel>

        <div v-else class="banner-empty">
          <p>当前没有已发布的首页轮播图</p>
          <span>管理员发布后会在这里自动展示。</span>
        </div>
      </div>
    </section>

    <section class="announcement-section">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Announcement</p>
          <h2>酒店公告</h2>
        </div>
      </div>

      <div v-if="announcements.length" class="announcement-list">
        <article
          v-for="item in announcements"
          :key="item.id"
          class="announcement-card"
          :class="{ pinned: item.isTop }"
        >
          <div class="announcement-meta">
            <el-tag v-if="item.isTop" type="danger">置顶</el-tag>
            <span>{{ formatDateTime(item.publishTime) }}</span>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.content }}</p>
        </article>
      </div>
      <div v-else class="announcement-empty">
        当前暂无已发布公告。
      </div>
    </section>

    <section class="status-section">
      <el-card class="status-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>后端连接状态</span>
          </div>
        </template>
        <div v-if="loadingHealth" class="loading">加载中...</div>
        <div v-else-if="healthStatus" class="status success">
          <el-icon :size="40" color="#67c23a"><CircleCheck /></el-icon>
          <p>{{ healthStatus }}</p>
        </div>
        <div v-else class="status error">
          <el-icon :size="40" color="#f56c6c"><CircleClose /></el-icon>
          <p>后端连接失败</p>
          <el-button @click="loadHealthStatus" type="primary">重试</el-button>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { checkHealth as fetchHealth } from '@/api/health'
import { getPublicBanners } from '@/api/content/banner'
import { getPublicAnnouncements } from '@/api/content/announcement'

const loadingHealth = ref(true)
const healthStatus = ref('')
const banners = ref([])
const announcements = ref([])

function extractResult(result, fallbackMessage) {
  if (result.code !== 'SUCCESS') {
    throw new Error(result.message || fallbackMessage)
  }
  return result.data
}

const loadHealthStatus = async () => {
  loadingHealth.value = true
  try {
    const result = await fetchHealth()
    if (result.code === 'SUCCESS') {
      healthStatus.value = '后端连接成功: ' + result.data
    } else {
      healthStatus.value = ''
      ElMessage.error(result.message || '连接后端失败')
    }
  } catch (error) {
    healthStatus.value = ''
    ElMessage.error(error?.message || '连接后端失败')
  } finally {
    loadingHealth.value = false
  }
}

async function loadBanners() {
  try {
    const result = await getPublicBanners()
    banners.value = extractResult(result, '加载轮播图失败')
  } catch (error) {
    banners.value = []
    ElMessage.error(error?.response?.data?.message || error?.message || '加载轮播图失败')
  }
}

async function loadAnnouncements() {
  try {
    const result = await getPublicAnnouncements()
    announcements.value = extractResult(result, '加载公告失败')
  } catch (error) {
    announcements.value = []
    ElMessage.error(error?.response?.data?.message || error?.message || '加载公告失败')
  }
}

function handleBannerClick(banner) {
  if (banner.linkUrl) {
    window.open(banner.linkUrl, '_blank', 'noopener,noreferrer')
  }
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

onMounted(() => {
  loadHealthStatus()
  loadBanners()
  loadAnnouncements()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  padding: 32px;
  background: linear-gradient(180deg, #fef9f3 0%, #f5efe6 30%, #ede4d8 100%);
}

.hero-bg-img {
  position: absolute;
  inset: 0;
  background: url('/images/bg/hero-bg-2.jpg') center/cover no-repeat;
  opacity: 0.3;
  z-index: 0;
}

.hero {
  display: grid;
  grid-template-columns: minmax(280px, 420px) 1fr;
  gap: 28px;
  align-items: center;
}

.eyebrow {
  margin: 0 0 12px;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
}

.hero-copy h1 {
  margin: 0;
  font-size: clamp(34px, 4vw, 56px);
  line-height: 1.05;
  color: #0f172a;
}

.hero-text {
  margin: 18px 0 0;
  font-size: 16px;
  line-height: 1.7;
  color: #475569;
}

.hero-actions {
  display: flex;
  gap: 14px;
  margin-top: 28px;
}

.hero-visual {
  min-width: 0;
}

.banner-carousel,
.banner-empty {
  border-radius: 28px;
  overflow: hidden;
  background: #dbeafe;
  box-shadow: 0 26px 60px rgba(15, 23, 42, 0.14);
}

.banner-card {
  position: relative;
  display: block;
  width: 100%;
  height: 360px;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  inset: auto 0 0 0;
  padding: 24px;
  background: linear-gradient(180deg, transparent, rgba(15, 23, 42, 0.76));
  color: #fff;
}

.banner-overlay h2 {
  margin: 12px 0 0;
  font-size: 28px;
}

.banner-badge {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  font-size: 12px;
}

.banner-empty {
  height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #475569;
}

.announcement-section {
  margin-top: 36px;
}

.section-heading h2 {
  margin: 0;
  font-size: 30px;
  color: #0f172a;
}

.announcement-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
  margin-top: 16px;
}

.announcement-card {
  padding: 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
}

.announcement-card.pinned {
  border: 1px solid rgba(239, 68, 68, 0.24);
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #64748b;
  font-size: 13px;
}

.announcement-card h3 {
  margin: 14px 0 10px;
  font-size: 20px;
  color: #111827;
}

.announcement-card p {
  margin: 0;
  color: #475569;
  line-height: 1.7;
  white-space: pre-wrap;
}

.announcement-empty {
  margin-top: 16px;
  padding: 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.76);
  color: #64748b;
}

.status-section {
  margin-top: 32px;
}

.status-card {
  max-width: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status,
.loading {
  padding: 20px;
  text-align: center;
}

@media (max-width: 900px) {
  .home-page {
    padding: 20px;
  }

  .hero {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    flex-direction: column;
  }
}
</style>
