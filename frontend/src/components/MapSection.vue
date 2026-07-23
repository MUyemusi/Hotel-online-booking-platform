<template>
  <section class="map-section section-shell">
    <div class="section-title">
      <div>
        <p class="eyebrow dark">酒店位置</p>
        <h2>酒店位置</h2>
        <p class="subtitle" v-if="address">{{ address }}</p>
      </div>
    </div>

    <div class="map-card">
      <div class="map-frame" :class="{ 'has-coords': latitude }">
        <div class="map-static">
          <div class="map-deco">
            <div class="deco-water"></div>
            <div class="deco-land"></div>
            <div class="deco-road"></div>
            <div class="deco-pin">
              <svg width="36" height="48" viewBox="0 0 24 32" fill="none">
                <path d="M12 0C5.4 0 0 5.4 0 12c0 9 12 20 12 20s12-11 12-20C24 5.4 18.6 0 12 0z" fill="#a4774f"/>
                <circle cx="12" cy="11" r="5" fill="#fff"/>
              </svg>
              <div class="pin-pulse"></div>
            </div>
          </div>
          <div class="map-label">
            <strong>{{ address || '三亚亚龙湾国家旅游度假区' }}</strong>
            <span v-if="latitude">经纬度 {{ latitude.toFixed(4) }}, {{ longitude.toFixed(4) }}</span>
          </div>
          <div class="map-actions">
            <a v-if="amapUrl" :href="amapUrl" target="_blank" class="map-btn primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="10" r="3"/><path d="M12 2v4m0 16v-6m-5.66-8.34L9 10m6 0 2.66-2.34M2 12h4m16 0h-4"/></svg>
              高德地图查看
            </a>
            <a v-if="baiduMapUrl" :href="baiduMapUrl" target="_blank" class="map-btn">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M7 17L17 7"/><path d="M7 7h10v10"/></svg>
              百度地图查看
            </a>
          </div>
        </div>
      </div>
      <div class="map-info">
        <div class="info-item">
          <span class="info-icon">◉</span>
          <div>
            <small>酒店地址</small>
            <strong>{{ address || '请在后台配置酒店地址' }}</strong>
          </div>
        </div>
        <div class="info-item" v-if="contactPhone">
          <span class="info-icon">✆</span>
          <div>
            <small>联系电话</small>
            <strong>{{ contactPhone }}</strong>
          </div>
        </div>
        <div class="info-item" v-if="trafficInfo">
          <span class="info-icon">☍</span>
          <div>
            <small>交通指南</small>
            <strong>{{ trafficInfo }}</strong>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  latitude:  { type: Number, default: null },
  longitude: { type: Number, default: null },
  address:      { type: String, default: '' },
  contactPhone: { type: String, default: '' },
  trafficInfo:  { type: String, default: '' }
})

const amapUrl = computed(() => {
  if (props.latitude == null || props.longitude == null) return null
  return `https://uri.amap.com/marker?position=${props.longitude},${props.latitude}&name=${encodeURIComponent(props.address || '酒店位置')}&callnative=1`
})

const baiduMapUrl = computed(() => {
  if (props.latitude == null || props.longitude == null) return null
  return `https://api.map.baidu.com/marker?location=${props.latitude},${props.longitude}&title=${encodeURIComponent(props.address || '酒店位置')}&content=${encodeURIComponent(props.address || '')}&output=html`
})
</script>

<style scoped>
.map-section {
  padding: 110px 0;
}

.eyebrow {
  margin: 0 0 18px;
  font-size: 11px;
  font-weight: 650;
  letter-spacing: 0.3em;
  color: #8c6d4f;
}

.section-title h2 {
  margin: 0;
  font-family: "Noto Serif SC", "Songti SC", serif;
  font-size: clamp(34px, 4vw, 52px);
  font-weight: 500;
  line-height: 1.35;
}

.subtitle {
  margin: 14px 0 0;
  color: #756b61;
  font-size: 14px;
}

/* ---------- 地图卡片 ---------- */
.map-card {
  display: grid;
  grid-template-columns: 1.35fr 0.75fr;
  gap: 0;
  margin-top: 40px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 56px rgba(45, 38, 28, 0.1);
}

/* ---------- 左侧静态地图 ---------- */
.map-frame {
  position: relative;
  min-height: 420px;
}

.map-static {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 30px;
  background: #e8ddd0;
}

/* 装饰性地图 */
.map-deco {
  position: relative;
  width: 220px;
  height: 180px;
  border-radius: 50% 50% 4px 50%;
  overflow: hidden;
  margin-bottom: 20px;
  transform: rotate(-3deg);
  box-shadow: 0 6px 30px rgba(90, 70, 45, 0.18);
}

.deco-water {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 45%;
  background: linear-gradient(180deg, #8ec8d8 0%, #5a9fb5 40%, #3d7a96 100%);
}

.deco-land {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 58%;
  background: linear-gradient(180deg, #6b9e5a 0%, #8db87a 50%, #a8c898 100%);
  clip-path: polygon(0% 100%, 0% 10%, 25% 5%, 50% 15%, 72% 0%, 100% 8%, 100% 100%);
}

.deco-road {
  position: absolute;
  top: 58%; left: -5%; right: -5%;
  height: 6px;
  background: #d4c5aa;
  border-radius: 2px;
  z-index: 2;
}

.deco-pin {
  position: absolute;
  top: 38%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 3;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.25));
}

.pin-pulse {
  position: absolute;
  top: 50%; left: 50%;
  width: 60px; height: 60px;
  margin: -30px 0 0 -30px;
  border-radius: 50%;
  background: rgba(164, 119, 79, 0.2);
  animation: pulse 2s ease-out infinite;
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 0.8; }
  100% { transform: scale(2.2); opacity: 0; }
}

.map-label {
  text-align: center;
  margin-bottom: 18px;
}

.map-label strong {
  display: block;
  font-size: 15px;
  color: #3b3328;
  font-weight: 600;
}

.map-label span {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: #9b8d7c;
}

.map-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.map-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 22px;
  border-radius: 10px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.25s;
  border: 1px solid #cfbea8;
  color: #5e4e3a;
  background: rgba(255,255,255,0.6);
}

.map-btn:hover {
  background: rgba(255,255,255,0.9);
  border-color: #a4774f;
  color: #a4774f;
}

.map-btn.primary {
  background: #a4774f;
  border-color: #a4774f;
  color: #fff;
}

.map-btn.primary:hover {
  background: #8f623f;
  border-color: #8f623f;
}

/* ---------- 右侧信息栏 ---------- */
.map-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
  padding: 38px 34px;
  background: #33483b;
  color: #fff;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.info-icon {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 14px;
}

.info-item small {
  display: block;
  margin-bottom: 3px;
  font-size: 10px;
  letter-spacing: 0.12em;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
}

.info-item strong {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.9);
}

/* ---------- 响应式 ---------- */
@media (max-width: 860px) {
  .map-section { padding: 80px 0; }
  .map-card { grid-template-columns: 1fr; }
  .map-frame { min-height: 340px; }
  .map-info { padding: 30px 24px; }
}
</style>
