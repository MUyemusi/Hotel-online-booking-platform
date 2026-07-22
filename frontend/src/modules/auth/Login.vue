<template>
  <div class="auth-page"><el-card class="auth-card">
    <h1>登录智宿酒店</h1><p>同一入口支持住客、前台和管理员账号</p>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
      <el-form-item label="账号" prop="username"><el-input v-model="form.username" autocomplete="username" /></el-form-item>
      <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password autocomplete="current-password" /></el-form-item>
      <el-button type="primary" size="large" class="full" :loading="loading" @click="submit">登录</el-button>
    </el-form>
    <div class="links"><el-button link @click="router.push('/register')">注册住客账号</el-button><el-button link @click="router.push('/')">返回首页</el-button></div>
  </el-card></div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { clearAuthSession, setAuthSession } from '@/utils/request'
const route=useRoute(), router=useRouter(), formRef=ref(), loading=ref(false)
const form=reactive({username:'',password:''})
const rules={username:[{required:true,message:'请输入账号',trigger:'blur'}],password:[{required:true,message:'请输入密码',trigger:'blur'}]}
function roleHome(user){if(user.roles?.includes('ADMIN'))return '/admin';if(user.roles?.includes('FRONT_DESK'))return '/frontdesk';return '/rooms'}
async function submit(){
  if(!await formRef.value.validate().catch(()=>false))return
  loading.value=true;clearAuthSession()
  try{
    const result=await login(form)
    if(result.code!=='SUCCESS')throw new Error(result.message||'登录失败')
    setAuthSession(result.data)
    const redirect=typeof route.query.redirect==='string'&&route.query.redirect.startsWith('/')?route.query.redirect:roleHome(result.data.user)
    ElMessage.success('登录成功');await router.replace(redirect)
  }catch(e){clearAuthSession();ElMessage.error(e?.response?.data?.message||e.message||'账号或密码错误')}finally{loading.value=false}
}
</script>
<style scoped>
.auth-page{min-height:100vh;display:grid;place-items:center;padding:24px;background:linear-gradient(135deg,#ecfeff,#eef2ff)}.auth-card{width:min(430px,100%);border-radius:22px}.auth-card h1{text-align:center;margin:4px 0 8px}.auth-card p{text-align:center;color:#64748b;margin-bottom:24px}.full{width:100%}.links{display:flex;justify-content:space-between;margin-top:16px}
</style>