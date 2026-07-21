<template>
  <div class="login-page">
    <div class="login-bg"></div>

    <div class="login-glass">
      <div class="login-head">
        <router-link to="/" class="lg-brand">智宿</router-link>
        <p class="lg-sub">ZHISU HOTEL</p>
      </div>

      <h2>欢迎回来</h2>
      <p class="lg-hint">登录您的账号以继续</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large" />
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
          登 录
        </el-button>
      </el-form>

      <p class="lg-register">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </p>

      <div class="lg-test-accounts">
        <p class="test-title">测试账号</p>
        <div class="test-row">
          <span class="test-role">管理员</span>
          <code>admin / admin123</code>
        </div>
        <div class="test-row">
          <span class="test-role">前台</span>
          <code>frontdesk / front123</code>
        </div>
        <div class="test-row">
          <span class="test-role">住客</span>
          <code>guest / guest123</code>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth/index'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const formRef = ref()

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    if (res.code === 'SUCCESS') {
      const data = res.data
      window.sessionStorage.setItem('zhisu_token', data.token)
      window.sessionStorage.setItem('zhisu_user', JSON.stringify({
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        roles: data.roles
      }))

      ElMessage.success(`欢迎，${data.nickname || data.username}`)

      // 根据角色跳转
      const roles = data.roles || []
      const redirect = route.query.redirect
      if (redirect) {
        router.push(redirect)
      } else if (roles.includes('ROLE_ADMIN') || roles.includes('ADMIN')) {
        router.push('/admin')
      } else if (roles.includes('ROLE_FRONT_DESK') || roles.includes('FRONT_DESK')) {
        router.push('/admin')
      } else {
        router.push('/')
      }
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, #fef9f3 0%, #f5efe6 40%, #ede4d8 100%);
}

.login-bg {
  position: absolute; inset: 0;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(200,148,92,0.10), transparent 50%),
    radial-gradient(ellipse at 70% 60%, rgba(200,148,92,0.06), transparent 50%);
  pointer-events: none;
}

.login-glass {
  position: relative;
  width: 420px;
  padding: 44px 40px;
  background: rgba(255,255,255,0.65);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.7);
  border-radius: 28px;
  box-shadow: 0 24px 64px rgba(44,36,22,0.08);
}

.login-head { text-align: center; margin-bottom: 32px; }
.lg-brand { font-size: 32px; font-weight: 700; color: #2c2416; text-decoration: none; letter-spacing: 0.08em; }
.lg-sub { margin: 4px 0 0; font-size: 10px; letter-spacing: 0.28em; color: #c8945c; }

.login-glass h2 { text-align: center; margin: 0 0 6px; font-size: 24px; color: #2c2416; }
.lg-hint { text-align: center; margin: 0 0 28px; color: #a69888; font-size: 14px; }

.login-btn { width: 100%; margin-top: 8px; border-radius: 14px; }
.login-btn :deep(span) { font-size: 16px; letter-spacing: 0.12em; }

.lg-register { text-align: center; margin-top: 20px; font-size: 13px; color: #a69888; }
.lg-register a { color: #c8945c; text-decoration: none; font-weight: 600; }

.lg-test-accounts {
  margin-top: 24px; padding: 16px;
  background: rgba(200,148,92,0.06); border: 1px solid rgba(200,148,92,0.12);
  border-radius: 14px;
}
.test-title { margin: 0 0 10px; font-size: 11px; font-weight: 650; color: #a69888; text-transform: uppercase; letter-spacing: 0.1em; }
.test-row { display: flex; align-items: center; gap: 10px; padding: 4px 0; font-size: 13px; }
.test-role { display: inline-block; width: 56px; padding: 2px 8px; border-radius: 5px; background: rgba(200,148,92,0.12); color: #c8945c; font-size: 11px; font-weight: 600; text-align: center; }
.test-row code { color: #5c5040; }
</style>
