
<template>
  <section class="staff-page">
    <div class="page-title"><div><span>员工权限管理</span><h1>员工账号和角色管理</h1><p>从现有账号中识别员工与管理员，并维护其角色和启停状态。</p></div><el-button @click="load">刷新数据</el-button></div>
    <el-alert title="当前后端没有单独的新建员工接口，本页只管理数据库中已存在的账号，不会自动编造前台账号。" type="info" :closable="false" show-icon />
    <el-card shadow="never" class="panel">
      <div class="toolbar"><el-input v-model.trim="keyword" clearable placeholder="搜索用户名、昵称或手机号" style="max-width:330px"/><span>共 {{ staff.length }} 个员工/管理员账号</span></div>
      <el-table :data="filteredStaff" v-loading="loading" empty-text="当前数据库没有员工账号">
        <el-table-column label="员工账号" min-width="210"><template #default="{row}"><div class="staff-cell"><span>{{ (row.nickname||row.username).slice(0,1) }}</span><div><b>{{ row.nickname||'未设置昵称' }}</b><small>@{{ row.username }} · {{ row.phone||'未填写手机号' }}</small></div></div></template></el-table-column>
        <el-table-column label="当前角色" min-width="180"><template #default="{row}"><el-tag v-for="role in row.roles||[]" :key="role" :type="roleType(role)" class="tag">{{ roleName(role) }}</el-tag></template></el-table-column>
        <el-table-column label="账号状态" width="110"><template #default="{row}"><el-tag :type="row.status==='ENABLED'?'success':'info'">{{ row.status==='ENABLED'?'正常':'停用' }}</el-tag></template></el-table-column>
        <el-table-column label="最近登录" width="180"><template #default="{row}">{{ formatTime(row.lastLoginAt) }}</template></el-table-column>
        <el-table-column label="操作" width="165"><template #default="{row}"><el-button link type="primary" :disabled="isSelf(row)" @click="editRoles(row)">分配角色</el-button><el-button link :type="row.status==='ENABLED'?'danger':'success'" :disabled="isSelf(row)" @click="toggleStatus(row)">{{ row.status==='ENABLED'?'停用':'启用' }}</el-button></template></el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialog.visible" title="分配员工角色" width="480px">
      <div class="dialog-user"><span>{{ (dialog.user?.nickname||dialog.user?.username||'员').slice(0,1) }}</span><div><b>{{ dialog.user?.nickname||dialog.user?.username }}</b><small>@{{ dialog.user?.username }}</small></div></div>
      <el-checkbox-group v-model="dialog.roles" class="role-options"><el-checkbox-button value="USER">住客</el-checkbox-button><el-checkbox-button value="FRONT_DESK">前台员工</el-checkbox-button><el-checkbox-button value="ADMIN">管理员</el-checkbox-button></el-checkbox-group>
      <el-alert title="角色权限由后端再次校验；不能修改当前登录管理员自己的角色。" type="warning" :closable="false" />
      <template #footer><el-button @click="dialog.visible=false">取消</el-button><el-button type="primary" :loading="dialog.saving" @click="saveRoles">保存角色</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserRoles, updateUserStatus } from '@/api/user'
import { getStoredUser } from '@/utils/auth'
const users=ref([]); const loading=ref(false); const keyword=ref(''); const dialog=reactive({visible:false,user:null,roles:[],saving:false})
const normalize=r=>(r||'').replace(/^ROLE_/,'').replace('FRONTDESK','FRONT_DESK'); const isStaff=row=>(row.roles||[]).map(normalize).some(role=>role==='ADMIN'||role==='FRONT_DESK')
const staff=computed(()=>users.value.filter(isStaff)); const filteredStaff=computed(()=>{const key=keyword.value.toLowerCase();return !key?staff.value:staff.value.filter(row=>[row.username,row.nickname,row.phone].some(value=>(value||'').toLowerCase().includes(key)))})
const roleName=r=>({USER:'住客',ADMIN:'管理员',FRONT_DESK:'前台员工'}[normalize(r)]||r); const roleType=r=>normalize(r)==='ADMIN'?'danger':normalize(r)==='FRONT_DESK'?'warning':''
const isSelf=row=>Number(getStoredUser()?.userId)===Number(row.id); const formatTime=v=>v?new Date(v).toLocaleString('zh-CN',{hour12:false}):'暂无记录'
async function load(){loading.value=true;try{const res=await getUsers({current:1,size:100});users.value=res.data.records||[]}catch(e){ElMessage.error(e?.response?.data?.message||'员工账号加载失败')}finally{loading.value=false}}
function editRoles(row){if(isSelf(row))return ElMessage.warning('不能修改当前登录账号自己的角色');dialog.user=row;dialog.roles=(row.roles||[]).map(normalize);dialog.visible=true}
async function saveRoles(){if(!dialog.roles.length)return ElMessage.warning('请至少保留一个角色');dialog.saving=true;try{await updateUserRoles(dialog.user.id,dialog.roles);ElMessage.success('员工角色已更新');dialog.visible=false;load()}catch(e){ElMessage.error(e?.response?.data?.message||'角色更新失败')}finally{dialog.saving=false}}
async function toggleStatus(row){const next=row.status==='ENABLED'?'DISABLED':'ENABLED';await ElMessageBox.confirm(`确认${next==='ENABLED'?'启用':'停用'}员工账号“${row.username}”吗？`,'账号状态',{type:'warning'});try{await updateUserStatus(row.id,next);ElMessage.success('账号状态已更新');load()}catch(e){ElMessage.error(e?.response?.data?.message||'状态更新失败')}}
load()
</script>

<style scoped>
.staff-page{display:grid;gap:18px}.page-title{display:flex;align-items:center;justify-content:space-between;padding:25px 28px;border-radius:22px;background:linear-gradient(135deg,rgba(255,255,255,.9),rgba(238,244,239,.8));box-shadow:0 16px 38px rgba(45,56,48,.06)}.page-title span{color:#547061;font-size:10px;font-weight:700;letter-spacing:.18em}.page-title h1{margin:6px 0;color:#2c2416;font-size:27px}.page-title p{margin:0;color:#827568}.panel{border:none;border-radius:22px;background:rgba(255,255,255,.78)}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:18px;color:#887c6f;font-size:13px}.staff-cell,.dialog-user{display:flex;align-items:center;gap:11px}.staff-cell>span,.dialog-user>span{width:38px;height:38px;display:grid;place-items:center;border-radius:12px;background:#496454;color:#fff}.staff-cell small,.dialog-user small{display:block;margin-top:3px;color:#9b8f82}.tag{margin-right:5px}.dialog-user{margin-bottom:22px}.role-options{display:flex;margin-bottom:20px}@media(max-width:760px){.page-title,.toolbar{align-items:flex-start;gap:15px}.toolbar{flex-direction:column}}
</style>
