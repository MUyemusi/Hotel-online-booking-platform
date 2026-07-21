<template>
  <section class="password-page">
    <div class="page-head"><span>ACCOUNT SECURITY</span><h1>修改密码</h1><p>验证原密码后更新登录凭据，保护账号安全。</p></div>
    <div class="security-grid">
      <el-card shadow="never" class="password-card">
        <el-alert title="修改成功后当前登录会失效，请使用新密码重新登录。" type="info" :closable="false" show-icon />
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="password-form">
          <el-form-item label="原密码" prop="oldPassword"><el-input v-model="form.oldPassword" type="password" show-password size="large" placeholder="请输入当前密码" /></el-form-item>
          <el-form-item label="新密码" prop="newPassword"><el-input v-model="form.newPassword" type="password" show-password size="large" placeholder="8—64 个字符" /></el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password size="large" placeholder="再次输入新密码" /></el-form-item>
          <div class="actions"><el-button @click="formRef.resetFields()">重新填写</el-button><el-button type="primary" :loading="saving" @click="submit">确认修改</el-button></div>
        </el-form>
      </el-card>
      <aside class="tips"><div class="lock">✓</div><h2>安全建议</h2><ul><li>使用至少 8 个字符的密码</li><li>建议同时包含字母、数字和符号</li><li>不要与其他网站共用同一密码</li><li>请勿向他人透露登录密码</li></ul></aside>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { changePassword } from '@/api/user'
import { clearSession } from '@/utils/auth'
const router = useRouter(); const formRef = ref(); const saving = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const confirmPassword = (_rule, value, callback) => value === form.newPassword ? callback() : callback(new Error('两次输入的新密码不一致'))
const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 8, max: 64, message: '长度为 8—64 个字符', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }, { validator: confirmPassword, trigger: 'blur' }]
}
async function submit() {
  if (!await formRef.value.validate().catch(() => false)) return
  await ElMessageBox.confirm('修改后需要重新登录，确认继续吗？', '确认修改密码', { type: 'warning' })
  saving.value = true
  try { await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword }); clearSession(); ElMessage.success('密码修改成功，请重新登录'); router.replace('/login') }
  catch (e) { ElMessage.error(e?.response?.data?.message || '密码修改失败') }
  finally { saving.value = false }
}
</script>

<style scoped>
.password-page{max-width:1050px;margin:0 auto;padding:24px 12px}.page-head span{color:#a4774f;font-size:10px;font-weight:700;letter-spacing:.2em}.page-head h1{margin:7px 0;color:#2c2416;font-size:34px}.page-head p{margin:0 0 26px;color:#897c6d}.security-grid{display:grid;grid-template-columns:1fr 310px;gap:24px}.password-card{border:1px solid rgba(255,255,255,.8);border-radius:22px;background:rgba(255,255,255,.78)}.password-form{margin-top:24px}.actions{display:flex;justify-content:flex-end;gap:10px}.tips{padding:30px;border-radius:22px;background:linear-gradient(155deg,#334c3d,#263a30);color:#fff;box-shadow:0 18px 45px rgba(38,59,48,.18)}.lock{width:48px;height:48px;display:grid;place-items:center;border-radius:16px;background:#a4774f;font-size:22px}.tips h2{margin:22px 0 14px}.tips ul{padding-left:20px;color:rgba(255,255,255,.72);line-height:2}@media(max-width:760px){.password-page{padding:12px 2px}.security-grid{grid-template-columns:1fr}}
.password-page{padding-top:124px;padding-bottom:56px}
@media(max-width:760px){.password-page{padding:108px 16px 40px}}
</style>
