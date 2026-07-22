<template>
  <div class="desk-layout">
    <aside class="desk-sidebar">
      <router-link to="/frontdesk" class="brand"><strong>智宿</strong><span>FRONT DESK</span></router-link>
      <div class="identity"><span>{{ initial }}</span><div><strong>{{ displayName }}</strong><small>酒店前台账号</small></div></div>
      <nav>
        <router-link to="/frontdesk" class="active"><b>工作台</b><small>今日接待与业务入口</small></router-link>
        <router-link to="/"><b>酒店前台网站</b><small>查看住客端展示</small></router-link>
      </nav>
      <button class="logout" @click="signOut">退出前台账号</button>
    </aside>
    <section class="desk-main">
      <header><div><span>FRONT DESK CONSOLE</span><h1>前台接待工作区</h1></div><div class="role-chip">前台专属页面</div></header>
      <main><router-view/></main>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { clearSession, getStoredUser } from '@/utils/auth'
import { logout } from '@/api/auth'

const router = useRouter()
const user = getStoredUser() || {}
const displayName = computed(() => user.nickname || user.username || '前台人员')
const initial = computed(() => displayName.value.slice(0, 1))

async function signOut() {
  await ElMessageBox.confirm('确认退出前台工作区吗？', '退出登录')
  try { await logout() } catch { /* 服务异常时仍允许本地退出 */ }
  clearSession()
  router.replace('/login')
}
</script>

<style scoped>
.desk-layout{min-height:100vh;display:grid;grid-template-columns:250px 1fr;background:#f3efe6;color:#302a24}.desk-sidebar{position:sticky;top:0;height:100vh;display:flex;flex-direction:column;padding:28px 22px;background:linear-gradient(180deg,#263c35,#345448);color:#fff}.brand{display:flex;flex-direction:column;color:#fff;text-decoration:none}.brand strong{font-family:"Noto Serif SC",serif;font-size:27px;letter-spacing:.12em}.brand span{margin-top:3px;color:#cbb892;font-size:9px;letter-spacing:.28em}.identity{display:flex;align-items:center;gap:12px;margin:32px 0;padding:16px 0;border-block:1px solid rgba(255,255,255,.1)}.identity>span{width:42px;height:42px;display:grid;place-items:center;border-radius:50%;background:#a4774f;font-weight:700}.identity div{display:flex;flex-direction:column}.identity small{margin-top:3px;color:rgba(255,255,255,.55);font-size:11px}nav{display:flex;flex-direction:column;gap:8px}nav a{display:flex;flex-direction:column;padding:13px 14px;border-radius:10px;color:rgba(255,255,255,.74);text-decoration:none}nav a:hover,nav a.active{background:rgba(255,255,255,.1);color:#fff}nav small{margin-top:4px;color:rgba(255,255,255,.44);font-size:10px}.logout{margin-top:auto;padding:12px;border:1px solid rgba(255,255,255,.16);border-radius:9px;background:transparent;color:rgba(255,255,255,.72);cursor:pointer}.desk-main{min-width:0;padding:28px 34px}.desk-main>header{display:flex;align-items:center;justify-content:space-between;margin-bottom:24px}.desk-main>header span{color:#9a7652;font-size:10px;letter-spacing:.22em}.desk-main>header h1{margin:5px 0 0;font-family:"Noto Serif SC",serif;font-size:30px;font-weight:600}.role-chip{padding:9px 14px;border:1px solid #cbd9d0;border-radius:30px;background:#e8f0eb;color:#456253;font-size:12px}main{padding:0}
@media(max-width:850px){.desk-layout{grid-template-columns:1fr}.desk-sidebar{position:relative;height:auto}.identity{margin:20px 0}nav{flex-direction:row}.logout{margin-top:20px}.desk-main{padding:22px}.role-chip{display:none}}
</style>
