<template>
  <section class="nearby-section section-shell">
    <div class="section-heading">
      <div>
        <p class="eyebrow">EXPLORE NEARBY</p>
        <h2>周边游玩推荐</h2>
        <p class="subtitle">根据酒店位置，为您推荐附近值得体验的景点</p>
      </div>

      <div class="category-tabs">
        <button
            v-for="item in categories"
            :key="item"
            :class="{ active: selectedCategory === item }"
            @click="selectedCategory = item"
        >
          {{ item }}
        </button>
      </div>
    </div>

    <div class="place-grid">
      <article
          v-for="(place, index) in displayedPlaces"
          :key="place.name"
          class="place-card"
      >
        <div class="place-cover" :class="`theme-${place.theme}`">
          <span class="place-icon">{{ place.icon }}</span>
          <span v-if="index === 0" class="recommend-tag">距离最近</span>
          <span class="category-tag">{{ place.category }}</span>
        </div>

        <div class="place-content">
          <div class="place-title">
            <div>
              <small>NO. {{ String(index + 1).padStart(2, '0') }}</small>
              <h3>{{ place.name }}</h3>
            </div>

            <strong>{{ formatDistance(place.distance) }}</strong>
          </div>

          <p>{{ place.description }}</p>

          <div class="place-meta">
            <span>建议游玩 {{ place.duration }}</span>
            <span>{{ place.suitable }}</span>
          </div>

          <a
              :href="createNavigationUrl(place)"
              target="_blank"
              rel="noopener noreferrer"
              class="route-link"
          >
            高德地图查看路线
            <span>→</span>
          </a>
        </div>
      </article>
    </div>

    <p class="data-note">
      推荐距离根据酒店与景点经纬度估算，实际路线和用时以地图导航为准。
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

const categories = ['全部', '人文景观', '自然风光', '休闲街区']

// 当前项目默认酒店坐标位于厦门，下面是用于课程项目演示的周边景点数据
const places = [
  {
    name: '厦门园林植物园',
    category: '自然风光',
    latitude: 24.454,
    longitude: 118.109,
    description: '山林、湖泊与特色植物园区相互交融，适合散步、拍照和亲近自然。',
    duration: '3—4小时',
    suitable: '自然观光',
    icon: '🌿',
    theme: 'forest'
  },
  {
    name: '南普陀寺',
    category: '人文景观',
    latitude: 24.4421,
    longitude: 118.0971,
    description: '闽南地区具有代表性的人文景观，建筑古朴，环境安静雅致。',
    duration: '1—2小时',
    suitable: '文化体验',
    icon: '🏯',
    theme: 'temple'
  },
  {
    name: '沙坡尾艺术西区',
    category: '休闲街区',
    latitude: 24.4388,
    longitude: 118.0836,
    description: '由老厦门港口街区发展而来的文艺空间，汇集展览、咖啡与特色小店。',
    duration: '2—3小时',
    suitable: '拍照休闲',
    icon: '🎨',
    theme: 'art'
  },
  {
    name: '鼓浪屿',
    category: '人文景观',
    latitude: 24.4471,
    longitude: 118.0679,
    description: '融合历史建筑、海岛风光与音乐文化，是体验厦门城市气质的经典目的地。',
    duration: '半天',
    suitable: '海岛漫游',
    icon: '🏝️',
    theme: 'island'
  },
  {
    name: '环岛路',
    category: '自然风光',
    latitude: 24.4319,
    longitude: 118.1245,
    description: '沿海而建的景观道路，可骑行、散步，在海风中欣赏厦门海岸风景。',
    duration: '2—4小时',
    suitable: '骑行观海',
    icon: '🌊',
    theme: 'ocean'
  },
  {
    name: '曾厝垵',
    category: '休闲街区',
    latitude: 24.4311,
    longitude: 118.1266,
    description: '具有闽南街巷特色的滨海社区，适合品尝小吃并感受轻松的夜间氛围。',
    duration: '2—3小时',
    suitable: '美食闲逛',
    icon: '🥢',
    theme: 'street'
  }
]

const hotelLatitude = computed(() =>
    Number.isFinite(props.latitude) ? props.latitude : 24.4798
)

const hotelLongitude = computed(() =>
    Number.isFinite(props.longitude) ? props.longitude : 118.0894
)

function toRadians(value) {
  return value * Math.PI / 180
}

// Haversine公式：根据两个经纬度计算直线距离
function calculateDistance(latitude, longitude) {
  const earthRadius = 6371
  const latitudeDifference = toRadians(latitude - hotelLatitude.value)
  const longitudeDifference = toRadians(longitude - hotelLongitude.value)

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
      .filter(place =>
          selectedCategory.value === '全部' ||
          place.category === selectedCategory.value
      )
      .map(place => ({
        ...place,
        distance: calculateDistance(place.latitude, place.longitude)
      }))
      .sort((first, second) => first.distance - second.distance)
})

function formatDistance(distance) {
  if (distance < 1) {
    return `${Math.round(distance * 1000)}m`
  }

  return `${distance.toFixed(1)}km`
}

function createNavigationUrl(place) {
  const from = `${hotelLongitude.value},${hotelLatitude.value},智宿酒店`
  const to = `${place.longitude},${place.latitude},${place.name}`

  return `https://uri.amap.com/navigation?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&mode=car&policy=1&src=zhisu-hotel&coordinate=gaode&callnative=0`
}
</script>

<style scoped>
.nearby-section {
  padding: 20px 0 120px;
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
  transition: 0.25s;
}

.category-tabs button:hover,
.category-tabs button.active {
  border-color: #33483b;
  background: #33483b;
  color: #fff;
}

.place-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22px;
}

.place-card {
  overflow: hidden;
  border-radius: 16px;
  background: #fffdf8;
  box-shadow: 0 14px 38px rgba(53, 43, 32, 0.08);
  transition: transform 0.25s, box-shadow 0.25s;
}

.place-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 46px rgba(53, 43, 32, 0.14);
}

.place-cover {
  position: relative;
  display: grid;
  height: 150px;
  place-items: center;
}

.place-icon {
  font-size: 54px;
  filter: drop-shadow(0 6px 9px rgba(0, 0, 0, 0.12));
}

.recommend-tag,
.category-tag {
  position: absolute;
  top: 14px;
  padding: 6px 10px;
  border-radius: 20px;
  font-size: 10px;
}

.recommend-tag {
  left: 14px;
  background: #a4774f;
  color: #fff;
}

.category-tag {
  right: 14px;
  background: rgba(255, 255, 255, 0.76);
  color: #50463b;
  backdrop-filter: blur(8px);
}

.theme-forest {
  background: linear-gradient(135deg, #b8d0ad, #73976a);
}

.theme-temple {
  background: linear-gradient(135deg, #ead1ad, #bc875d);
}

.theme-art {
  background: linear-gradient(135deg, #d8c9df, #9d86ad);
}

.theme-island {
  background: linear-gradient(135deg, #b9dce3, #669eae);
}

.theme-ocean {
  background: linear-gradient(135deg, #add7dd, #4d8fa4);
}

.theme-street {
  background: linear-gradient(135deg, #e6c5a4, #bd785d);
}

.place-content {
  padding: 24px;
}

.place-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 15px;
}

.place-title small {
  color: #a4774f;
  font-size: 9px;
  letter-spacing: 0.15em;
}

.place-title h3 {
  margin: 6px 0 0;
  color: #302a24;
  font-family: "Noto Serif SC", serif;
  font-size: 20px;
  font-weight: 600;
}

.place-title strong {
  flex-shrink: 0;
  color: #33483b;
  font-family: Georgia, serif;
  font-size: 18px;
}

.place-content > p {
  min-height: 68px;
  margin: 17px 0;
  color: #756b61;
  font-size: 13px;
  line-height: 1.75;
}

.place-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 19px;
}

.place-meta span {
  padding: 5px 9px;
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
}

.route-link:hover {
  color: #a4774f;
}

.data-note {
  margin: 22px 0 0;
  color: #9a8d80;
  font-size: 11px;
  text-align: right;
}

@media (max-width: 980px) {
  .place-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 680px) {
  .nearby-section {
    padding-bottom: 80px;
  }

  .place-grid {
    grid-template-columns: 1fr;
  }

  .place-content > p {
    min-height: auto;
  }
}
</style>