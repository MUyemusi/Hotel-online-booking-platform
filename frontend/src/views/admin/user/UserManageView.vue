<template>
  <section class="admin-page">
    <div class="page-title"><div><span>USER ADMINISTRATION</span><h1>用户管理</h1><p>查询注册用户，维护账号状态并查看信用限制。</p></div><el-button @click="loadUsers">刷新数据</el-button></div>
    <div class="stat-row"><div><span>当前页账号</span><b>{{ users.length }}</b></div><div><span>正常账号</span><b>{{ enabledCount }}</b></div><div><span>受限预订</span><b>{{ bannedCount }}</b></div></div>
    <el-card shadow="never" class="panel">
      <el-form :inline="true" :model="query" class="filters" @submit.prevent="search">
        <el-form-item label="关键词"><el-input v-model.trim="query.keyword" clearable placeholder="用户名、昵称或手机号" @keyup.enter="search" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部状态" style="width:140px"><el-option label="正常" value="ENABLED"/><el-option label="停用" value="DISABLED"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
      <el-table :data="users" v-loading="loading" row-key="id" empty-text="未查询到用户">
        <el-table-column label="用户" min-width="190"><template #default="{row}"><div class="user-cell"><span>{{ (row.nickname || row.username).slice(0,1) }}</span><div><b>{{ row.nickname || '未设置昵称' }}</b><small>@{{ row.username }} · ID {{ row.id }}</small></div></div></template></el-table-column>
        <el-table-column prop="phone" label="手机号" width="135"/>
        <el-table-column label="账号类型" min-width="150"><template #default="{row}"><el-tag v-for="role in row.roles || []" :key="role" size="small" class="tag-gap">{{ roleName(role) }}</el-tag></template></el-table-column>
        <el-table-column label="信用状态" width="135"><template #default="{row}"><el-tag v-if="isBanned(row)" type="danger">限制预订</el-tag><span v-else>{{ row.consecutiveCancelCount || 0 }} 次取消</span></template></el-table-column>
        <el-table-column label="账号状态" width="105"><template #default="{row}"><el-tag :type="row.status==='ENABLED'?'success':'info'">{{ row.status==='ENABLED'?'正常':'停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right"><template #default="{row}"><el-button link :type="row.status==='ENABLED'?'danger':'success'" :disabled="isSelf(row)" @click="toggleStatus(row)">{{ row.status==='ENABLED'?'停用':'启用' }}</el-button><el-button link type="warning" @click="openCredit(row)">信用详情</el-button></template></el-table-column>
      </el-table>
      <div class="pager"><el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @current-change="loadUsers" @size-change="sizeChanged"/></div>
    </el-card>
    <el-drawer v-model="credit.visible" title="用户信用详情" size="560px">
      <div v-loading="credit.loading">
        <div class="credit-summary" :class="credit.summary.bookingAllowed?'ok':'ban'"><div><span>预订状态</span><b>{{ credit.summary.bookingAllowed?'允许预订':'限制预订' }}</b></div><div><span>连续取消</span><b>{{ credit.summary.consecutiveCancelCount || 0 }} / {{ credit.summary.cancellationLimit || 3 }}</b></div></div>
        <el-button v-if="credit.summary.banUntil || credit.summary.consecutiveCancelCount > 0" type="danger" plain @click="handleUnban">人工解除禁订</el-button>
        <el-divider>信用变更记录</el-divider>
        <el-timeline><el-timeline-item v-for="item in credit.records" :key="item.id" :timestamp="formatTime(item.createdAt)" placement="top"><el-card shadow="never"><b>{{ typeName(item.changeType) }}</b><p>{{ item.reason }}</p><small>{{ item.beforeCount ?? '-' }} → {{ item.afterCount ?? '-' }}，订单：{{ item.orderId || '-' }}</small></el-card></el-timeline-item></el-timeline>
        <el-empty v-if="!credit.records.length" description="暂无信用记录"/>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, getUserCredit, getUserCreditRecords, unbanUser, updateUserStatus } from '@/api/user'
import { getStoredUser } from '@/utils/auth'
const users=ref([]); const loading=ref(false); const query=reactive({keyword:'',status:''}); const page=reactive({current:1,size:10,total:0})
const credit=reactive({visible:false,user:null,summary:{},records:[],loading:false})
const enabledCount=computed(()=>users.value.filter(item=>item.status==='ENABLED').length); const bannedCount=computed(()=>users.value.filter(isBanned).length)
const normalizeRole=r=>(r||'').replace(/^ROLE_/,''); const roleName=r=>({USER:'住客',ADMIN:'管理员',FRONT_DESK:'前台员工',FRONTDESK:'前台员工'}[normalizeRole(r)]||r)
const isSelf=row=>Number(getStoredUser()?.userId)===Number(row.id); function isBanned(row){return row.banUntil&&new Date(row.banUntil)>new Date()}
const formatTime=v=>v?new Date(v).toLocaleString('zh-CN',{hour12:false}):'-'; const typeName=t=>({CANCEL:'主动取消',BAN:'限制预订',COMPLETE:'完成入住',UNBAN:'解除限制'}[t]||t)
async function loadUsers(){loading.value=true;try{const res=await getUsers({current:page.current,size:page.size,keyword:query.keyword||undefined,status:query.status||undefined});users.value=res.data.records||[];page.total=Number(res.data.total||0)}catch(e){ElMessage.error(e?.response?.data?.message||'用户列表加载失败')}finally{loading.value=false}}
function search(){page.current=1;loadUsers()} function reset(){query.keyword='';query.status='';search()} function sizeChanged(){page.current=1;loadUsers()}
async function toggleStatus(row){const next=row.status==='ENABLED'?'DISABLED':'ENABLED';await ElMessageBox.confirm(`确认${next==='ENABLED'?'启用':'停用'}账号“${row.username}”吗？`,'账号状态',{type:'warning'});try{await updateUserStatus(row.id,next);ElMessage.success('账号状态已更新');loadUsers()}catch(e){ElMessage.error(e?.response?.data?.message||'状态更新失败')}}
async function openCredit(row){credit.visible=true;credit.user=row;credit.loading=true;try{const [s,r]=await Promise.all([getUserCredit(row.id),getUserCreditRecords(row.id,{current:1,size:50})]);credit.summary=s.data||{};credit.records=r.data.records||[]}catch(e){ElMessage.error(e?.response?.data?.message||'信用信息加载失败')}finally{credit.loading=false}}
async function handleUnban(){try{const {value}=await ElMessageBox.prompt('请输入解除限制的原因','人工解除禁订',{inputPattern:/\S+/,inputErrorMessage:'原因不能为空'});await unbanUser(credit.user.id,value.trim());ElMessage.success('已解除禁订');await openCredit(credit.user);loadUsers()}catch(e){if(e!=='cancel'&&e!=='close')ElMessage.error(e?.response?.data?.message||'解除失败')}}
loadUsers()
</script>

<style scoped>
.admin-page{display:grid;gap:18px}.page-title{display:flex;align-items:center;justify-content:space-between;padding:25px 28px;border-radius:22px;background:linear-gradient(135deg,rgba(255,255,255,.9),rgba(248,242,232,.78));box-shadow:0 16px 38px rgba(45,56,48,.06)}.page-title span{color:#9a7652;font-size:10px;font-weight:700;letter-spacing:.18em}.page-title h1{margin:6px 0;color:#2c2416;font-size:27px}.page-title p{margin:0;color:#827568}.stat-row{display:grid;grid-template-columns:repeat(3,1fr);gap:14px}.stat-row>div{padding:18px 21px;border-radius:18px;background:rgba(255,255,255,.7)}.stat-row span,.stat-row b{display:block}.stat-row span{color:#8c7f70;font-size:12px}.stat-row b{margin-top:8px;color:#3b5545;font-size:25px}.panel{border:none;border-radius:22px;background:rgba(255,255,255,.78)}.user-cell{display:flex;align-items:center;gap:11px}.user-cell>span{width:36px;height:36px;display:grid;place-items:center;border-radius:12px;background:linear-gradient(135deg,#a4774f,#c39a70);color:#fff}.user-cell small{display:block;margin-top:3px;color:#9b8f82}.tag-gap{margin-right:4px}.pager{display:flex;justify-content:flex-end;margin-top:20px}.credit-summary{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:18px;padding:18px;border-radius:16px}.credit-summary.ok{background:#eef8e9}.credit-summary.ban{background:#fff0f0}.credit-summary span,.credit-summary b{display:block}.credit-summary span{margin-bottom:6px;color:#7d7166;font-size:12px}@media(max-width:760px){.stat-row{grid-template-columns:1fr}.page-title{align-items:flex-start;gap:18px}}
</style>
