<template>
  <div class="auth-page"><el-card class="auth-card">
    <h1>注册住客账号</h1>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="账号" prop="username"><el-input v-model="form.username" /></el-form-item>
      <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
      <el-form-item label="昵称" prop="nickname"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
      <el-button class="full" type="primary" :loading="loading" @click="submit">创建账号</el-button>
    </el-form>
    <el-button link @click="router.push('/login')">已有账号，去登录</el-button>
  </el-card></div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'
const router=useRouter(),formRef=ref(),loading=ref(false)
const form=reactive({username:'',password:'',nickname:'',phone:'',email:''})
const rules={
 username:[{required:true,message:'请输入账号',trigger:'blur'},{min:3,max:50,message:'长度为3到50位',trigger:'blur'}],
 password:[{required:true,message:'请输入密码',trigger:'blur'},{min:8,max:72,message:'至少8位',trigger:'blur'}],
 nickname:[{required:true,message:'请输入昵称',trigger:'blur'}],
 phone:[{required:true,message:'请输入手机号',trigger:'blur'},{pattern:/^1[3-9]\d{9}$/,message:'手机号格式不正确',trigger:'blur'}],
 email:[{type:'email',message:'邮箱格式不正确',trigger:'blur'}]
}
async function submit(){if(!await formRef.value.validate().catch(()=>false))return;loading.value=true;try{const r=await register(form);if(r.code!=='SUCCESS')throw new Error(r.message);ElMessage.success('注册成功，请登录');router.replace('/login')}catch(e){ElMessage.error(e?.response?.data?.message||e.message||'注册失败')}finally{loading.value=false}}
</script>
<style scoped>
.auth-page{min-height:100vh;display:grid;place-items:center;padding:24px;background:linear-gradient(135deg,#ecfeff,#eef2ff)}.auth-card{width:min(480px,100%);border-radius:22px}.auth-card h1{text-align:center}.full{width:100%}
</style>