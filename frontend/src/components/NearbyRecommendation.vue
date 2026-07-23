<template>
  <section class="nearby-section section-shell">
    <div class="section-heading">
      <div>
        <p class="eyebrow">EXPLORE SANYA</p>
        <h2>周边游玩推荐</h2>
        <p class="subtitle">
          从亚龙湾出发，发现三亚值得体验的自然风光与特色景点
        </p>
      </div>

      <div class="category-tabs">
        <button
            v-for="category in categories"
            :key="category"
            type="button"
            :class="{ active: selectedCategory === category }"
            @click="selectedCategory = category"
        >
          {{ category }}
        </button>
      </div>
    </div>

    <div class="place-grid">
      <article
          v-for="(place, index) in displayedPlaces"
          :key="place.name"
          class="place-card"
      >
        <div class="place-cover">
          <img
              :src="place.image"
              :alt="place.name"
              loading="lazy"
              @error="handleImageError"
          />

          <div class="image-mask"></div>

          <span v-if="index === 0" class="recommend-tag">
            距离最近
          </span>

          <span class="category-tag">
            {{ place.category }}
          </span>

          <div class="cover-title">
            <small>NO. {{ String(index + 1).padStart(2, '0') }}</small>
            <h3>{{ place.name }}</h3>
          </div>
        </div>

        <div class="place-content">
          <div class="distance-row">
            <span>距酒店直线距离</span>
            <strong>{{ formatDistance(place.distance) }}</strong>
          </div>

          <p class="description">
            {{ place.description }}
          </p>

          <div class="place-meta">
            <span>⏱ 建议游玩 {{ place.duration }}</span>
            <span>✦ {{ place.suitable }}</span>
          </div>

          <a
              :href="createNavigationUrl(place)"
              target="_blank"
              rel="noopener noreferrer"
              class="route-link"
          >
            <span>高德地图查看路线</span>
            <b>→</b>
          </a>
        </div>
      </article>
    </div>

    <p class="data-note">
      景点距离根据酒店与景点经纬度估算，实际路线和时间以地图导航为准。
    </p>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  latitude: {
    type: Number,
    default: null
  },
  longitude: {
    type: Number,
    default: null
  }
})

const selectedCategory = ref('全部')

const categories = [
  '全部',
  '自然风光',
  '人文景观',
  '休闲街区'
]

// 三亚亚龙湾周边景点数据
// 图片使用在线旅游风景展示图，不需要下载到项目中
const places = [
  {
    name: '亚龙湾中心广场',
    category: '休闲街区',
    latitude: 18.2298,
    longitude: 109.6418,
    description:
        '位于亚龙湾核心区域，可漫步海滨、观赏图腾景观，感受轻松舒适的热带度假氛围。',
    duration: '1—2小时',
    suitable: '海滨漫步',
    image:
        'https://images.unsplash.com/photo-1510414842594-a61c69b5ae57?auto=format&fit=crop&w=1200&q=85'
  },
  {
    name: '亚龙湾沙滩',
    category: '自然风光',
    latitude: 18.2255,
    longitude: 109.6385,
    description:
        '拥有绵长细腻的沙滩和清澈海水，适合散步、看海、拍照以及体验热带滨海风光。',
    duration: '2—3小时',
    suitable: '沙滩休闲',
    image:
        'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=85'
  },
  {
    name: '亚龙湾海底世界',
    category: '自然风光',
    latitude: 18.23144,
    longitude: 109.65713,
    description:
        '可体验潜水、海底观光和多种水上项目，近距离感受亚龙湾丰富而神秘的海洋生态。',
    duration: '3—4小时',
    suitable: '海洋体验',
    image:
        'https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=1200&q=85'
  },
  {
    name: '亚龙湾热带天堂森林公园',
    category: '自然风光',
    latitude: 18.2516,
    longitude: 109.6608,
    description:
        '融合热带雨林、山海景观和观景栈道，登上高处可以俯瞰亚龙湾壮丽的海岸全景。',
    duration: '半天',
    suitable: '雨林观光',
    image:
        'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=1200&q=85'
  },
  {
    name: '博后村',
    category: '人文景观',
    latitude: 18.2674,
    longitude: 109.6398,
    description:
        '具有黎族文化特色的美丽乡村，汇集特色民宿、田园景观、美食和当地生活体验。',
    duration: '2—3小时',
    suitable: '乡村体验',
    image:
        'https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=1200&q=85'
  },
  {
    name: '太阳湾路',
    category: '自然风光',
    latitude: 18.2035,
    longitude: 109.6208,
    description:
        '一条依山临海的滨海景观公路，一侧青山、一侧碧海，沿途拥有多个优美观景位置。',
    duration: '1—2小时',
    suitable: '沿海观景',
    image:
        'https://images.unsplash.com/photo-1494783367193-149034c05e8f?auto=format&fit=crop&w=1200&q=85'
  }
]

// 后台没有配置坐标时，默认使用三亚亚龙湾中心坐标
const hotelLatitude = computed(() => {
  const latitude = Number(props.latitude)

  return Number.isFinite(latitude) && latitude !== 0
      ? latitude
      : 18.232
})

const hotelLongitude = computed(() => {
  const longitude = Number(props.longitude)

  return Number.isFinite(longitude) && longitude !== 0
      ? longitude
      : 109.642
})

function toRadians(value) {
  return value * Math.PI / 180
}

// 使用Haversine公式计算两个经纬度之间的直线距离
function calculateDistance(latitude, longitude) {
  const earthRadius = 6371

  const latitudeDifference = toRadians(
      latitude - hotelLatitude.value
  )

  const longitudeDifference = toRadians(
      longitude - hotelLongitude.value
  )

  const result =
      Math.sin(latitudeDifference / 2) ** 2 +
      Math.cos(toRadians(hotelLatitude.value)) *
      Math.cos(toRadians(latitude)) *
      Math.sin(longitudeDifference / 2) ** 2

  return earthRadius * 2 * Math.atan2(
      Math.sqrt(result),
      Math.sqrt(1 - result)
  )
}

const displayedPlaces = computed(() => {
  return places
      .filter(place => {
        return selectedCategory.value === '全部' ||
            place.category === selectedCategory.value
      })
      .map(place => ({
        ...place,
        distance: calculateDistance(
            place.latitude,
            place.longitude
        )
      }))
      .sort((first, second) => {
        return first.distance - second.distance
      })
})

function formatDistance(distance) {
  if (distance < 1) {
    return `${Math.round(distance * 1000)}m`
  }

  return `${distance.toFixed(1)}km`
}

function createNavigationUrl(place) {
  const from =
      `${hotelLongitude.value},${hotelLatitude.value},智宿酒店`

  const to =
      `${place.longitude},${place.latitude},${place.name}`

  return (
      'https://uri.amap.com/navigation' +
      `?from=${encodeURIComponent(from)}` +
      `&to=${encodeURIComponent(to)}` +
      '&mode=car' +
      '&policy=1' +
      '&src=zhisu-hotel' +
      '&coordinate=gaode' +
      '&callnative=0'
  )
}

// 在线图片无法加载时显示备用图，避免卡片出现空白
function handleImageError(event) {
  event.target.onerror = null

  event.target.src =
      'data:image/svg+xml;charset=UTF-8,' +
      encodeURIComponent(`
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="1200"
        height="700"
        viewBox="0 0 1200 700"
      >
        <defs>
          <linearGradient
            id="background"
            x1="0"
            y1="0"
            x2="1"
            y2="1"
          >
            <stop offset="0%" stop-color="#9fc9c1"/>
            <stop offset="55%" stop-color="#5f978e"/>
            <stop offset="100%" stop-color="#33483b"/>
          </linearGradient>
        </defs>

        <rect
          width="1200"
          height="700"
          fill="url(#background)"
        />

        <circle
          cx="950"
          cy="150"
          r="75"
          fill="#f5ddb2"
          opacity="0.9"
        />

        <path
          d="M0 520 Q250 420 470 520 T920 500 T1200 490 V700 H0 Z"
          fill="#e6d1ad"
        />

        <path
          d="M0 570 Q260 500 500 570 T930 550 T1200 545 V700 H0 Z"
          fill="#87c4cf"
          opacity="0.9"
        />

        <text
          x="600"
          y="320"
          text-anchor="middle"
          fill="#ffffff"
          font-size="54"
          font-family="Arial"
          letter-spacing="8"
        >
          EXPLORE SANYA
        </text>

        <text
          x="600"
          y="380"
          text-anchor="middle"
          fill="#ffffff"
          font-size="25"
          font-family="Arial"
          opacity="0.82"
        >
          Tropical Coast · Island Holiday
        </text>
      </svg>
    `)
}
</script>

<style scoped>
.nearby-section {
  padding: 35px 0 120px;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 30px;
  margin-bottom: 42px;
}

.eyebrow {
  margin: 0 0 18px;
  color: #8c6d4f;
  font-size: 11px;
  font-weight: 650;
  letter-spacing: 0.3em;
}

h2 {
  margin: 0;
  color: #302a24;
  font-family: "Noto Serif SC", "Songti SC", serif;
  font-size: clamp(34px, 4vw, 52px);
  font-weight: 500;
}

.subtitle {
  margin: 14px 0 0;
  color: #756b61;
  font-size: 14px;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-tabs button {
  padding: 9px 16px;
  border: 1px solid #d8cbbb;
  border-radius: 30px;
  background: transparent;
  color: #756b61;
  cursor: pointer;
  font-family: inherit;
  transition:
      color 0.25s,
      border-color 0.25s,
      background 0.25s;
}

.category-tabs button:hover,
.category-tabs button.active {
  border-color: #33483b;
  background: #33483b;
  color: #ffffff;
}

.place-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.place-card {
  overflow: hidden;
  border: 1px solid rgba(168, 145, 118, 0.16);
  border-radius: 16px;
  background: #fffdf8;
  box-shadow: 0 14px 38px rgba(53, 43, 32, 0.08);
  transition:
      transform 0.3s,
      box-shadow 0.3s;
}

.place-card:hover {
  transform: translateY(-7px);
  box-shadow: 0 22px 50px rgba(53, 43, 32, 0.15);
}

.place-cover {
  position: relative;
  height: 235px;
  overflow: hidden;
  background: #c5d4ce;
}

.place-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.65s ease;
}

.place-card:hover .place-cover img {
  transform: scale(1.07);
}

.image-mask {
  position: absolute;
  inset: 0;
  background:
      linear-gradient(
          to bottom,
          rgba(20, 28, 23, 0.04) 28%,
          rgba(19, 29, 23, 0.78) 100%
      );
  pointer-events: none;
}

.recommend-tag,
.category-tag {
  position: absolute;
  z-index: 2;
  top: 15px;
  padding: 7px 11px;
  border-radius: 20px;
  font-size: 10px;
  backdrop-filter: blur(8px);
}

.recommend-tag {
  left: 15px;
  background: #a4774f;
  color: #ffffff;
}

.category-tag {
  right: 15px;
  background: rgba(255, 253, 248, 0.86);
  color: #50463b;
}

.cover-title {
  position: absolute;
  z-index: 2;
  right: 22px;
  bottom: 20px;
  left: 22px;
  color: #ffffff;
}

.cover-title small {
  font-size: 9px;
  letter-spacing: 0.18em;
  opacity: 0.8;
}

.cover-title h3 {
  margin: 7px 0 0;
  font-family: "Noto Serif SC", "Songti SC", serif;
  font-size: 23px;
  font-weight: 600;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.place-content {
  padding: 23px;
}

.distance-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee4d8;
  color: #958577;
  font-size: 11px;
}

.distance-row strong {
  color: #33483b;
  font-family: Georgia, serif;
  font-size: 20px;
  font-weight: 600;
}

.description {
  min-height: 68px;
  margin: 17px 0;
  color: #756b61;
  font-size: 13px;
  line-height: 1.75;
}

.place-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 19px;
}

.place-meta span {
  padding: 6px 9px;
  border-radius: 5px;
  background: #f0e8dc;
  color: #746556;
  font-size: 11px;
}

.route-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid #eee4d8;
  color: #33483b;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.route-link b {
  font-size: 20px;
  font-weight: 400;
  transition: transform 0.2s;
}

.route-link:hover {
  color: #a4774f;
}

.route-link:hover b {
  transform: translateX(5px);
}

.data-note {
  margin: 22px 0 0;
  color: #9a8d80;
  font-size: 11px;
  text-align: right;
}

@media (max-width: 980px) {
  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .place-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .nearby-section {
    padding: 20px 0 80px;
  }

  .place-grid {
    grid-template-columns: 1fr;
  }

  .place-cover {
    height: 230px;
  }

  .description {
    min-height: auto;
  }

  .category-tabs button {
    padding: 8px 13px;
    font-size: 12px;
  }

  .data-note {
    line-height: 1.6;
    text-align: left;
  }
}
</style>