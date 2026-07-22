<template>
  <div class="register-page">
    <router-link to="/login" class="back-home">← 返回登录</router-link>
    <div class="register-card">
      <span class="brand">智宿</span>
      <p class="brand-en">ACCOUNT REGISTRATION</p>
      <h2>创建住客账号</h2>
      <p class="hint">注册账号默认为住客角色，员工和管理员角色由系统管理员分配</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="用户名" prop="username">
            <el-input v-model.trim="form.username" maxlength="50" placeholder="4—50 个字符" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model.trim="form.nickname" maxlength="50" placeholder="怎么称呼您" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model.trim="form.phone" maxlength="11" placeholder="请输入 11 位手机号" />
          </el-form-item>
          <el-form-item label="邮箱（选填）" prop="email">
            <el-input v-model.trim="form.email" maxlength="100" placeholder="name@example.com" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="至少 6 个字符" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" />
          </el-form-item>
        </div>
        <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="handleRegister">立即注册</el-button>
      </el-form>
      <p class="login-link">已有账号？<router-link to="/login">返回登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', phone: '', email: '', password: '', confirmPassword: '' })
const validateConfirm = (_rule, value, callback) => value === form.password ? callback() : callback(new Error('两次输入的密码不一致'))
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 4, max: 50, message: '长度为 4—50 个字符', trigger: 'blur' }],
  nickname: [{ max: 50, message: '昵称不能超过 50 个字符', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 64, message: '长度为 6—64 个字符', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }]
}

async function handleRegister() {
  if (!await formRef.value.validate().catch(() => false)) return
  loading.value = true
  try {
    const { confirmPassword, ...payload } = form
    const res = await register({ ...payload, nickname: payload.nickname || null, email: payload.email || null })
    if (res.code !== 'SUCCESS') {
      ElMessage.error(res.message || '注册失败，请检查填写的信息')
      return
    }

    ElMessage.success('注册成功，请登录')
    router.replace('/login')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '注册失败，请稍后重试')
  } finally { loading.value = false }
}
</script>

<style scoped>
.register-page { min-height: 100vh; display: grid; place-items: center; position:relative; padding: 50px 20px; background: linear-gradient(90deg,rgba(35,48,40,.70),rgba(35,48,40,.28)),url('/images/eco/zhisu-courtyard.png') center/cover fixed; }
.back-home{position:absolute;left:34px;top:28px;color:rgba(255,255,255,.9);text-decoration:none;font-size:13px}.register-card { width: min(760px, 100%); padding: 40px; border: 1px solid rgba(255,255,255,.82); border-radius: 8px; background: rgba(255,253,248,.9); backdrop-filter: blur(22px); box-shadow: 0 28px 80px rgba(28,35,30,.24); }
.brand { display: block; text-align: center; color: #2c2416; font-size: 30px; font-weight: 700; letter-spacing: .08em; text-decoration: none; }
.brand-en { margin: 2px 0 24px; text-align: center; color: #a4774f; font-size: 10px; letter-spacing: .28em; }
h2 { margin: 0; text-align: center; color: #2c2416; }
.hint { margin: 8px 0 28px; text-align: center; color: #a69888; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; column-gap: 24px; }
.submit-btn { width: 100%; border-radius: 8px; }
.login-link { margin: 20px 0 0; text-align: center; color: #8d8072; font-size: 14px; }
.login-link a { color: #496454; text-decoration: none; font-weight: 600; }
@media (max-width: 640px) { .register-card { padding: 30px 22px; } .form-grid { grid-template-columns: 1fr; } }
</style>
