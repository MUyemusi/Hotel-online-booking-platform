<template>
  <div class="user-layout" :class="{ 'is-scrolled': scrolled }">
    <!-- 顶部导航 -->
    <header class="top-nav" :class="{ 'nav-scrolled': scrolled }">
      <div class="nav-inner">
        <router-link to="/" class="brand">
          <span class="brand-zh">智宿</span>
          <span class="brand-en">ZHISU HOTEL</span>
        </router-link>

        <nav class="nav-links">
          <router-link to="/" class="nav-item" active-class="nav-active">
            <span>首页</span>
            <small>Home</small>
          </router-link>
          <router-link to="/rooms" class="nav-item" active-class="nav-active">
            <span>客房鉴赏</span>
            <small>Rooms</small>
          </router-link>
        </nav>

        <div class="nav-actions">
          <router-link to="/login" class="btn-outline">登录</router-link>
        </div>
      </div>
    </header>

    <!-- 页面舞台 -->
    <main class="page-stage">
      <router-view v-slot="{ Component, route }">
        <transition
          :name="transitionName"
          mode="out-in"
        >
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="footer-glow"></div>
      <div class="footer-inner">
        <div class="footer-brand">
          <span class="brand-zh">智宿酒店</span>
          <span class="brand-en">ZHISU HOTEL</span>
        </div>
        <div class="footer-info">
          <p>© 2026 智宿酒店在线预订与运营管理系统</p>
          <p>为每一位宾客，呈献至臻体验</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const scrolled = ref(false)
const transitionName = ref('slide-left')

window.addEventListener('scroll', () => {
  scrolled.value = window.scrollY > 60
}, { passive: true })

router.beforeEach((to, from) => {
  const toDepth = to.path.split('/').length
  const fromDepth = from.path.split('/').length
  transitionName.value = toDepth >= fromDepth ? 'slide-left' : 'slide-right'
})
</script>

<style>
/* ========== 全局设计令牌 ========== */
.user-layout {
  --cream: #fef9f3;
  --cream-dark: #f5efe6;
  --warm-white: #fdfaf7;
  --card-bg: rgba(255, 255, 255, 0.72);
  --card-border: rgba(255, 255, 255, 0.55);
  --glass-bg: rgba(255, 255, 255, 0.48);
  --glass-border: rgba(255, 255, 255, 0.65);
  --text-primary: #2c2416;
  --text-secondary: #6b5e4f;
  --text-muted: #a69888;
  --accent: #c8945c;
  --accent-light: #dbb882;
  --accent-soft: rgba(200, 148, 92, 0.12);
  --accent-glow: rgba(200, 148, 92, 0.18);
  --sage: #8b9d83;
  --sage-soft: rgba(139, 157, 131, 0.10);
  --shadow-sm: 0 2px 12px rgba(44, 36, 22, 0.06);
  --shadow-md: 0 8px 32px rgba(44, 36, 22, 0.08);
  --shadow-lg: 0 20px 60px rgba(44, 36, 22, 0.10);
  --shadow-glow: 0 0 60px rgba(200, 148, 92, 0.10);
  --radius-xl: 28px;
  --radius-lg: 20px;
  --radius-md: 14px;
  --radius-sm: 10px;
  --transition-smooth: 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);

  min-height: 100vh;
  background: linear-gradient(180deg, #fef9f3 0%, #f5efe6 30%, #ede4d8 100%);
  color: var(--text-primary);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ========== 顶部导航 ========== */
.top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 18px 0;
  transition: all var(--transition-smooth);
}

.nav-scrolled {
  background: rgba(254, 249, 243, 0.78);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: var(--shadow-sm);
}

.nav-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  flex-direction: column;
  text-decoration: none;
  line-height: 1.15;
}

.brand-zh {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.06em;
}

.brand-en {
  font-size: 10px;
  font-weight: 400;
  color: var(--accent);
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.nav-links { display: flex; gap: 36px; }

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-item small {
  font-size: 10px;
  font-weight: 400;
  color: var(--text-muted);
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.nav-item:hover,
.nav-active { color: var(--accent); }

.nav-active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  width: 20px;
  height: 3px;
  background: var(--accent);
  border-radius: 3px;
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  padding: 10px 28px;
  border: 1.5px solid rgba(200, 148, 92, 0.35);
  border-radius: 50px;
  color: var(--accent);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(6px);
}

.btn-outline:hover {
  border-color: var(--accent);
  background: var(--accent);
  color: #fff;
  box-shadow: var(--shadow-glow);
}

/* ========== 页面切换动画 ========== */
.page-stage { position: relative; overflow: hidden; }

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.slide-left-enter-from { opacity: 0; transform: translateX(50px); }
.slide-left-leave-to { opacity: 0; transform: translateX(-50px); }

.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.slide-right-enter-from { opacity: 0; transform: translateX(-50px); }
.slide-right-leave-to { opacity: 0; transform: translateX(50px); }

/* ========== 页脚 ========== */
.site-footer {
  margin-top: 80px;
  padding: 48px 0;
  position: relative;
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(255, 255, 255, 0.5);
}

.footer-glow {
  position: absolute;
  top: -60px;
  left: 50%;
  transform: translateX(-50%);
  width: 400px;
  height: 80px;
  background: radial-gradient(circle, var(--accent-glow), transparent 70%);
  pointer-events: none;
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-brand .brand-zh { font-size: 20px; }
.footer-info { text-align: right; }
.footer-info p { margin: 4px 0; color: var(--text-muted); font-size: 13px; }

@media (max-width: 768px) {
  .nav-inner, .footer-inner { padding: 0 20px; }
  .nav-links { gap: 20px; }
  .nav-item { font-size: 13px; }
  .nav-item small { display: none; }
  .footer-inner { flex-direction: column; gap: 16px; text-align: center; }
  .footer-info { text-align: center; }
}
</style>
