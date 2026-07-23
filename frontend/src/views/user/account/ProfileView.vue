<template>
  <section class="account-page">
    <div class="page-head">
      <div><span class="eyebrow">个人档案</span><h1>个人中心</h1><p>查看账号状态并维护个人资料</p></div>
      <div class="avatar">{{ avatarText }}</div>
    </div>

    <div class="account-grid" v-loading="loading">
      <el-card shadow="never" class="glass-card">
        <template #header><div class="card-title"><span>基本资料</span><el-tag type="success" effect="light">{{ statusText }}</el-tag></div></template>
        <el-form ref="profileRef" :model="profile" :rules="profileRules" label-position="top">
          <el-form-item label="用户名"><el-input :model-value="user.username" disabled /></el-form-item>
          <el-form-item label="昵称" prop="nickname"><el-input v-model.trim="profile.nickname" maxlength="50" show-word-limit /></el-form-item>
          <el-form-item label="手机号" prop="phone"><el-input v-model.trim="profile.phone" maxlength="11" /></el-form-item>
          <el-form-item label="邮箱" prop="email"><el-input v-model.trim="profile.email" maxlength="100" placeholder="选填" /></el-form-item>
          <el-form-item label="头像地址" prop="avatarUrl"><el-input v-model.trim="profile.avatarUrl" maxlength="500" placeholder="选填，填写图片 URL" /></el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
        </el-form>
      </el-card>

      <el-card shadow="never" class="glass-card status-card">
        <template #header><div class="card-title"><span>账号概览</span><span class="safe-label">安全信息</span></div></template>
        <div class="status-hero"><span>{{ avatarText }}</span><div><b>{{ user.nickname || user.username }}</b><small>@{{ user.username }}</small></div></div>
        <div class="meta"><span>账号角色</span><div><el-tag v-for="role in user.roles || []" :key="role" class="role-tag">{{ roleName(role) }}</el-tag></div></div>
        <div class="meta"><span>最近登录</span><strong>{{ formatTime(user.lastLoginAt) }}</strong></div>
        <div class="meta"><span>注册时间</span><strong>{{ formatTime(user.createdAt) }}</strong></div>
        <div class="quick-entry-list">
          <router-link to="/account/orders" class="quick-entry">
            <div>
              <strong>我的订单</strong>
              <small>随时查看全部订单状态、支付与入住进度</small>
            </div>
            <span>→</span>
          </router-link>
          <router-link to="/account/password" class="quick-entry alt">
            <div>
              <strong>账号安全</strong>
              <small>修改密码并维护登录安全</small>
            </div>
            <span>→</span>
          </router-link>
        </div>
      </el-card>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentUser, updateCurrentUser } from '@/api/user'
import { getStoredUser } from '@/utils/auth'

const profileRef = ref()
const loading = ref(false); const saving = ref(false)
const user = reactive({})
const profile = reactive({ nickname: '', phone: '', email: '', avatarUrl: '' })
const avatarText = computed(() => (user.nickname || user.username || '住').slice(0, 1))
const statusText = computed(() => user.status === 'ENABLED' ? '账号正常' : '账号停用')
const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

function fill(data) {
  Object.assign(user, data)
  Object.assign(profile, { nickname: data.nickname || '', phone: data.phone || '', email: data.email || '', avatarUrl: data.avatarUrl || '' })
}
async function load() { loading.value = true; try { const res = await getCurrentUser(); fill(res.data) } catch (e) { ElMessage.error(e?.response?.data?.message || '个人资料加载失败') } finally { loading.value = false } }
async function saveProfile() {
  if (!await profileRef.value.validate().catch(() => false)) return
  saving.value = true
  try {
    const res = await updateCurrentUser({ ...profile, email: profile.email || null, avatarUrl: profile.avatarUrl || null })
    fill(res.data)
    const stored = getStoredUser() || {}; stored.nickname = res.data.nickname; window.sessionStorage.setItem('zhisu_user', JSON.stringify(stored))
    ElMessage.success('个人资料已保存')
  } catch (e) { ElMessage.error(e?.response?.data?.message || '保存失败') } finally { saving.value = false }
}
const formatTime = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '暂无记录'
const roleName = role => ({ USER: '住客', ADMIN: '管理员', FRONT_DESK: '前台员工', FRONTDESK: '前台员工' }[role.replace(/^ROLE_/, '')] || role)
onMounted(load)
</script>

<style scoped>
.account-page { max-width: 1120px; margin: 0 auto; padding: 124px 24px 56px; }
.page-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 26px; }
.eyebrow { color: #c8945c; font-size: 11px; letter-spacing: .22em; }
h1 { margin: 6px 0; color: #2c2416; font-size: 34px; } .page-head p { margin: 0; color: #8f8170; }
.avatar { width: 72px; height: 72px; display: grid; place-items: center; border-radius: 24px; color: #fff; font-size: 28px; font-weight: 700; background: linear-gradient(135deg,#c8945c,#9d6939); box-shadow: 0 16px 32px rgba(176,120,64,.24); }
.account-grid { display: grid; grid-template-columns: 1.15fr .85fr; gap: 24px; }
.glass-card { border: 1px solid rgba(255,255,255,.7); border-radius: 22px; background: rgba(255,255,255,.7); }
.card-title { display: flex; justify-content: space-between; align-items: center; font-weight: 700; color: #332a20; }
.safe-label { color: #9b8d7e; font-size: 12px; font-weight: 400; }.status-hero{display:flex;align-items:center;gap:13px;margin-bottom:24px;padding:18px;border-radius:17px;background:linear-gradient(135deg,#f0e8da,#faf7f0)}.status-hero>span{width:48px;height:48px;display:grid;place-items:center;border-radius:15px;background:#496454;color:#fff;font-weight:700}.status-hero b,.status-hero small{display:block}.status-hero small{margin-top:4px;color:#938676}.quick-entry-list{display:flex;flex-direction:column;gap:12px;margin-top:24px}.quick-entry{display:flex;align-items:center;justify-content:space-between;gap:14px;padding:16px 18px;border-radius:16px;background:#496454;color:#fff;text-decoration:none;box-shadow:0 14px 26px rgba(73,100,84,.16)}.quick-entry.alt{background:linear-gradient(135deg,#efe3d1,#f8f3eb);color:#3f342a;box-shadow:none;border:1px solid rgba(164,119,79,.18)}.quick-entry strong,.quick-entry small{display:block}.quick-entry small{margin-top:5px;color:rgba(255,255,255,.78)}.quick-entry.alt small{color:#8f8170}.quick-entry span{font-size:20px;font-weight:700}
.meta { display: flex; justify-content: space-between; gap: 16px; margin: 14px 0; color: #8f8170; font-size: 13px; }.meta strong { color: #54483a; }.role-tag { margin-left: 6px; }
@media (max-width: 800px) { .account-page { padding: 108px 16px 40px; } .account-grid { grid-template-columns: 1fr; } }
</style>
