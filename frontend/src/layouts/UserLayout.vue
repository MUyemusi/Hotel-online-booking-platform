<template>
  <div class="shell">
    <header class="nav">
      <strong @click="router.push('/')">智宿酒店</strong>
      <nav>
        <el-button link @click="router.push('/rooms')">查找房型</el-button>
        <el-button link @click="router.push('/orders')">我的订单</el-button>
        <span>{{ user?.nickname || user?.username }}</span>
        <el-button type="danger" plain @click="signOut">退出</el-button>
      </nav>
    </header>
    <main><router-view /></main>
  <div class="user-layout" :class="{ 'is-scrolled': scrolled }">
    <!-- 顶部导航 -->
    <header class="top-nav" :class="{ 'nav-scrolled': scrolled || !immersiveHeader }">
      <div class="nav-inner">
        <router-link to="/" class="brand">
          <span class="brand-zh">智宿</span>
          <span class="brand-en">COASTAL ECO RETREAT</span>
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
          <router-link v-if="isGuest" to="/account/profile" class="nav-item" active-class="nav-active">
            <span>个人中心</span>
            <small>Account</small>
          </router-link>
          <router-link v-if="isGuest" to="/account/credit" class="nav-item" active-class="nav-active">
            <span>信用中心</span>
            <small>Credit</small>
          </router-link>
          <router-link v-if="workspaceRoute" :to="workspaceRoute" class="nav-item workspace-entry">
            <span>{{ workspaceLabel }}</span>
            <small>Workspace</small>
          </router-link>
        </nav>

        <div class="nav-actions">
          <template v-if="!loggedIn">
            <router-link to="/register" class="register-link">注册住客账号</router-link>
            <router-link to="/login" class="btn-outline">登录</router-link>
          </template>
          <el-dropdown v-else trigger="click" @command="handleCommand">
            <button class="user-button"><span class="user-dot">{{ userInitial }}</span><span class="user-copy"><b>{{ userName }}</b><small>{{ roleLabel }}</small></span></button>
            <template #dropdown><el-dropdown-menu><el-dropdown-item command="account">个人中心</el-dropdown-item><el-dropdown-item command="password">修改密码</el-dropdown-item><el-dropdown-item v-if="isGuest" command="credit">信用中心</el-dropdown-item><el-dropdown-item v-if="isAdmin" command="admin">进入管理员后台</el-dropdown-item><el-dropdown-item v-if="isFrontdesk" command="frontdesk">进入前台工作台</el-dropdown-item><el-dropdown-item divided command="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
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
          <p>向海而居，与自然相逢</p>
        </div>
      </div>
    </footer>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from '@/api/auth'
import { clearAuthSession, getCurrentUser } from '@/utils/request'
const router = useRouter()
const user = ref(getCurrentUser())
async function signOut() {
  try { await logout() } finally { clearAuthSession(); await router.replace('/') }
}
</script>
<style scoped>
.shell{min-height:100vh;background:#f8fafc}.nav{height:68px;padding:0 5vw;display:flex;align-items:center;justify-content:space-between;background:#fff;border-bottom:1px solid #e2e8f0}.nav strong{cursor:pointer;font-size:21px;color:#0f766e}.nav nav{display:flex;align-items:center;gap:12px}main{max-width:1200px;margin:auto;padding:28px 20px}
</style>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { logout } from '@/api/auth'
import { clearSession, getStoredUser, getToken, hasRole } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const scrolled = ref(false)
const transitionName = ref('slide-left')
const loggedIn = computed(() => Boolean(getToken()))
const storedUser = computed(() => getStoredUser() || {})
const userName = computed(() => storedUser.value.nickname || storedUser.value.username || '我的账号')
const userInitial = computed(() => userName.value.slice(0, 1))
const isAdmin = computed(() => hasRole('ADMIN'))
const isFrontdesk = computed(() => hasRole('FRONT_DESK', 'FRONTDESK') && !isAdmin.value)
const isGuest = computed(() => loggedIn.value && !isAdmin.value && !isFrontdesk.value)
const roleLabel = computed(() => isAdmin.value ? '系统管理员' : isFrontdesk.value ? '酒店前台' : '住客账号')
const workspaceRoute = computed(() => isAdmin.value ? '/admin' : isFrontdesk.value ? '/frontdesk' : '')
const workspaceLabel = computed(() => isAdmin.value ? '管理后台' : isFrontdesk.value ? '前台工作台' : '')
const immersiveHeader = computed(() => route.path === '/' || route.path === '/rooms' || route.path.startsWith('/rooms/'))

async function handleCommand(command) {
  if (command === 'account') return router.push('/account/profile')
  if (command === 'password') return router.push('/account/password')
  if (command === 'credit') return router.push('/account/credit')
  if (command === 'admin') return router.push('/admin')
  if (command === 'frontdesk') return router.push('/frontdesk')
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出当前账号吗？', '退出登录', { type: 'warning' })
    try { await logout() } catch { /* JWT 无状态，服务异常时仍允许本地退出 */ }
    clearSession()
    router.replace('/login')
  }
}

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
  --cream: #f7f3ea;
  --cream-dark: #e9ddc8;
  --warm-white: #fffdf8;
  --card-bg: rgba(255, 255, 255, 0.72);
  --card-border: rgba(255, 255, 255, 0.55);
  --glass-bg: rgba(255, 255, 255, 0.48);
  --glass-border: rgba(255, 255, 255, 0.65);
  --text-primary: #302a24;
  --text-secondary: #6d6258;
  --text-muted: #9b8c7d;
  --accent: #a4774f;
  --accent-light: #c2a477;
  --accent-soft: rgba(164, 119, 79, 0.12);
  --accent-glow: rgba(164, 119, 79, 0.18);
  --sage: #496454;
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
  background: #f7f3ea;
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
  color: #fff;
  letter-spacing: 0.06em;
}

.brand-en {
  font-size: 10px;
  font-weight: 400;
  color: rgba(255,255,255,.72);
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.nav-links { display: flex; gap: 36px; }

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: rgba(255,255,255,.86);
  font-size: 15px;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-item small {
  font-size: 10px;
  font-weight: 400;
  color: rgba(255,255,255,.55);
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.nav-item:hover,
.nav-active { color: #fff; }

.nav-active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  width: 20px;
  height: 3px;
  background: #d9c3a1;
  border-radius: 3px;
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  padding: 10px 28px;
  border: 1px solid rgba(255,255,255,.5);
  border-radius: 50px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
  background: rgba(255,255,255,.08);
  backdrop-filter: blur(6px);
}

.btn-outline:hover {
  border-color: #a4774f;
  background: #a4774f;
  color: #fff;
  box-shadow: var(--shadow-glow);
}
.user-button { display:flex;align-items:center;gap:9px;padding:7px 15px 7px 8px;border:1px solid rgba(255,255,255,.35);border-radius:30px;background:rgba(255,255,255,.1);color:#fff;cursor:pointer; }
.user-dot { width:30px;height:30px;display:grid;place-items:center;border-radius:50%;background:#a4774f;color:#fff;font-weight:700; }
.user-copy{display:flex;flex-direction:column;align-items:flex-start;line-height:1.2}.user-copy b{font-size:13px}.user-copy small{font-size:9px;color:rgba(255,255,255,.62);letter-spacing:.06em}.register-link{color:rgba(255,255,255,.84);font-size:13px;text-decoration:none}.register-link:hover{color:#fff}.nav-actions{display:flex;align-items:center;gap:16px}.workspace-entry span{color:#ead4ad}

.nav-scrolled .brand-zh { color: var(--text-primary); }
.nav-scrolled .brand-en { color: var(--accent); }
.nav-scrolled .nav-item { color: var(--text-secondary); }
.nav-scrolled .nav-item small { color: var(--text-muted); }
.nav-scrolled .nav-item:hover,.nav-scrolled .nav-active { color: var(--accent); }
.nav-scrolled .btn-outline { color: var(--accent); border-color: rgba(164,119,79,.4); background: rgba(255,255,255,.45); }
.nav-scrolled .user-button { color: #5c5040; border-color: rgba(164,119,79,.25); background: rgba(255,255,255,.6); }
.nav-scrolled .user-copy small{color:#9b8c7d}.nav-scrolled .register-link{color:#6d6258}.nav-scrolled .workspace-entry span{color:#496454}

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
  background: #33483b;
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
.site-footer .brand-zh { color: #fff; }
.site-footer .brand-en { color: #cdb897; }
.footer-info p { margin: 4px 0; color: rgba(255,255,255,.58); font-size: 13px; }

@media (max-width: 768px) {
  .nav-inner, .footer-inner { padding: 0 20px; }
  .nav-links { gap: 20px; }
  .nav-item { font-size: 13px; }
  .nav-item small { display: none; }
  .footer-inner { flex-direction: column; gap: 16px; text-align: center; }
  .footer-info { text-align: center; }
}
</style>
