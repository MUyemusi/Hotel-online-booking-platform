<template>
  <div class="login-page">
    <div class="login-visual">
      <img src="/images/eco/zhisu-login-hero.png" alt="智宿海滨酒店迎宾庭院" fetchpriority="high" />
      <div class="visual-overlay"></div>
      <div class="visual-badge">智宿 · 安全登录</div>
      <div class="visual-copy"><p>身份认证与访问控制</p><h1>每一次安心入住，<br />都从安全登录开始。</h1><span>统一身份认证 · 角色权限控制 · 账号安全保护</span></div>
    </div>

    <div class="login-glass">
      <div class="login-head">
        <span class="lg-brand">智宿</span>
        <p class="lg-sub">账号与权限中心</p>
      </div>

      <h2>欢迎回来</h2>
      <p class="lg-hint">请输入账号信息，系统将根据角色进入对应页面</p>

      <div class="role-guide" aria-label="账号角色说明">
        <div><strong>住客账号</strong><span>个人资料与信用</span></div>
        <div><strong>员工账号</strong><span>个人账号中心</span></div>
        <div><strong>管理员</strong><span>进入管理控制台</span></div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名或手机号" prop="username">
          <el-input v-model.trim="form.username" placeholder="请输入用户名或手机号" size="large" />
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

      <p class="security-note">账号权限由系统分配，注册账号默认为住客身份。</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { clearAuthSession, setAuthSession } from '@/utils/request'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function roleHome(user) {
  if (user?.roles?.includes('ADMIN')) return '/admin'
  if (user?.roles?.includes('FRONT_DESK')) return '/frontdesk'
  return '/'
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  clearAuthSession()
  try {
    const result = await login(form)
    if (result.code !== 'SUCCESS') throw new Error(result.message || '登录失败')
    setAuthSession(result.data)
    const user = result.data.user || {}
    ElMessage.success(`欢迎，${user.nickname || user.username || '用户'}`)
    const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/')
      ? route.query.redirect
      : roleHome(user)
    await router.replace(redirect)
  } catch (error) {
    clearAuthSession()
    ElMessage.error(error?.response?.data?.message || error.message || '登录失败，请检查用户名、手机号或密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page{min-height:100vh;display:grid;grid-template-columns:1.25fr .75fr;align-items:center;background:#f7f3ea}.login-visual{position:relative;height:100vh;min-height:680px;overflow:hidden}.login-visual img,.visual-overlay{position:absolute;inset:0;width:100%;height:100%}.login-visual img{object-fit:cover}.visual-overlay{background:linear-gradient(0deg,rgba(24,35,29,.68),rgba(24,35,29,.06) 70%),linear-gradient(90deg,rgba(24,35,29,.18),transparent)}.visual-badge{position:absolute;z-index:2;top:34px;left:42px;padding:9px 14px;border:1px solid rgba(255,255,255,.35);border-radius:30px;color:#fff;font-size:10px;letter-spacing:.18em;background:rgba(33,49,40,.2);backdrop-filter:blur(8px)}.visual-copy{position:absolute;z-index:2;left:7%;bottom:9%;color:#fff}.visual-copy p{font-size:10px;letter-spacing:.3em;color:#dbc5a3}.visual-copy h1{margin:18px 0;font-family:"Noto Serif SC",serif;font-size:clamp(36px,4vw,58px);font-weight:500;line-height:1.35}.visual-copy span{color:rgba(255,255,255,.7)}
.login-glass{position:relative;width:min(420px,calc(100% - 50px));margin:auto;padding:44px 40px;background:rgba(255,253,248,.84);backdrop-filter:blur(20px);border:1px solid rgba(255,255,255,.9);border-radius:8px;box-shadow:0 24px 64px rgba(44,36,22,.12)}.login-head{text-align:center;margin-bottom:32px}.lg-brand{font-size:32px;font-weight:700;color:#302a24;text-decoration:none;letter-spacing:.08em}.lg-sub{margin:4px 0 0;font-size:10px;letter-spacing:.28em;color:#a4774f}.login-glass h2{text-align:center;margin:0 0 6px;font-size:24px;color:#302a24}.lg-hint{text-align:center;margin:0 0 28px;color:#9b8c7d;font-size:14px}.login-btn{width:100%;margin-top:8px;border-radius:8px}.login-btn :deep(span){font-size:16px;letter-spacing:.12em}.lg-register{text-align:center;margin-top:20px;font-size:13px;color:#9b8c7d}.lg-register a{color:#496454;text-decoration:none;font-weight:600}.lg-test-accounts{margin-top:24px;padding:16px;background:rgba(164,119,79,.06);border:1px solid rgba(164,119,79,.12);border-radius:8px}.test-title{margin:0 0 10px;font-size:11px;font-weight:650;color:#9b8c7d;text-transform:uppercase;letter-spacing:.1em}.test-row{display:flex;align-items:center;gap:10px;padding:4px 0;font-size:13px}.test-role{display:inline-block;width:56px;padding:2px 8px;border-radius:5px;background:rgba(73,100,84,.12);color:#496454;font-size:11px;font-weight:600;text-align:center}.test-row code{color:#5c5040}
@media(max-width:960px){.login-page{display:flex;padding:30px;background:linear-gradient(rgba(31,47,38,.58),rgba(31,47,38,.58)),url('/images/eco/zhisu-coastal-hero.webp') center/cover}.login-visual{display:none}.login-glass{width:min(430px,100%)}}
.login-visual img{object-position:center;image-rendering:auto}.login-glass{width:min(440px,calc(100% - 50px));padding:42px;background:rgba(255,253,248,.95);border-radius:16px;box-shadow:0 24px 64px rgba(44,36,22,.14)}.lg-hint{margin-bottom:18px;font-size:13px}.role-guide{display:grid;grid-template-columns:repeat(3,1fr);gap:7px;margin:0 0 24px}.role-guide div{padding:9px 7px;border:1px solid #e6dccd;border-radius:9px;background:#fbf7ef;text-align:center}.role-guide strong{display:block;color:#3f5949;font-size:12px}.role-guide span{display:block;margin-top:3px;color:#9b8c7d;font-size:10px;line-height:1.3}.security-note{margin:22px 0 0;padding-top:16px;border-top:1px solid #e8dfd3;text-align:center;color:#9b8c7d;font-size:11px}
@media(max-width:960px){.login-page{background:linear-gradient(rgba(31,47,38,.5),rgba(31,47,38,.5)),url('/images/eco/zhisu-login-hero.png') center/cover}.login-glass{width:min(440px,100%)}}
@media(max-width:520px){.login-page{padding:18px}.login-glass{padding:32px 24px}.role-guide{grid-template-columns:1fr}.role-guide div{display:flex;justify-content:space-between;align-items:center}.role-guide span{margin:0}}
</style>
