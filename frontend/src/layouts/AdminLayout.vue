<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="brand">
        <h1>智宿酒店</h1>
        <p>后台管理</p>
      </div>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div>
          <span class="admin-role">ADMIN CONSOLE · 管理员专属</span>
          <h2>管理员控制台</h2>
        </div>

        <el-dropdown @command="handleCommand">
          <button class="admin-user">
            <span>{{ initial }}</span>
            {{ displayName }}
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="home">酒店首页</el-dropdown-item>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { logout } from '@/api/auth'
import { clearSession, getStoredUser } from '@/utils/auth'

const router = useRouter()
const user = getStoredUser() || {}
const displayName = computed(() => user.nickname || user.username || '管理员')
const initial = computed(() => displayName.value.slice(0, 1))

async function handleCommand(command) {
  if (command === 'home') return router.push('/')
  if (command === 'profile') return router.push('/account/profile')
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出管理后台吗？', '退出登录', { type: 'warning' })
    try {
      await logout()
    } catch {
      // JWT 无状态，后端暂不可用时仍允许清除本地登录状态。
    }
    clearSession()
    router.replace('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
  background: #f4f0e8;
}

.admin-sidebar {
  padding: 28px 20px;
  background: linear-gradient(180deg, #263b30 0%, #33483b 100%);
  color: #fff;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

.brand h1 {
  margin: 0 0 6px;
  font-size: 24px;
  color: #fff;
}

.brand p {
  margin: 0;
  color: rgba(255, 255, 255, 0.5);
}

.admin-main {
  min-width: 0;
  padding: 24px;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-radius: 24px;
  background: rgba(255, 253, 248, 0.88);
  backdrop-filter: blur(10px);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.admin-header h2 {
  margin: 5px 0 0;
  font-size: 24px;
  color: #111827;
}

.admin-role {
  color: #9a7652;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 7px 15px 7px 8px;
  border: 1px solid rgba(73, 100, 84, 0.2);
  border-radius: 30px;
  background: #fff;
  color: #5c5040;
  cursor: pointer;
}

.admin-user span {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #496454;
  color: #fff;
  font-weight: 700;
}

.admin-content {
  min-height: calc(100vh - 136px);
}

@media (max-width: 900px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    min-height: 92px;
  }

  .admin-main {
    padding: 16px;
  }
}
</style>
