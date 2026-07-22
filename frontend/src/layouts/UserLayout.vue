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