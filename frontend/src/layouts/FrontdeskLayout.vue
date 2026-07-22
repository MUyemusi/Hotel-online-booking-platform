<template>
  <div class="frontdesk-layout">
    <header><strong>智宿酒店前台工作台</strong><span>{{ user?.nickname || user?.username }}</span><el-button plain type="danger" @click="signOut">退出</el-button></header>
    <main><el-result icon="success" title="前台身份验证成功" sub-title="现有入住、退房和房态接口已受 FRONT_DESK / ADMIN 权限保护。" /></main>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from '@/api/auth'
import { clearAuthSession, getCurrentUser } from '@/utils/request'
const router=useRouter(), user=ref(getCurrentUser())
async function signOut(){try{await logout()}finally{clearAuthSession();router.replace('/')}}
</script>
<style scoped>
.frontdesk-layout{min-height:100vh;background:#f1f5f9}.frontdesk-layout header{height:68px;padding:0 5vw;display:flex;gap:20px;align-items:center;background:white}.frontdesk-layout header span{margin-left:auto}.frontdesk-layout main{padding:40px}
</style>