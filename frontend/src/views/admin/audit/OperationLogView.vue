<template>
  <section class="audit-page">
    <div class="page-title"><div><h1>操作日志</h1><p>追踪重要写操作的执行人、结果、来源与耗时。</p></div><el-button @click="load">刷新</el-button></div>
    <el-card shadow="never" class="panel">
      <el-form :inline="true" :model="query" class="filters">
        <el-form-item label="操作人 ID"><el-input-number v-model="query.operatorId" :min="1" controls-position="right" style="width:130px"/></el-form-item>
        <el-form-item label="模块"><el-select v-model="query.module" clearable filterable placeholder="全部" style="width:130px"><el-option v-for="x in modules" :key="x" :label="moduleName(x)" :value="x"/></el-select></el-form-item>
        <el-form-item label="动作"><el-select v-model="query.action" clearable placeholder="全部" style="width:120px"><el-option label="新增" value="CREATE"/><el-option label="更新" value="UPDATE"/><el-option label="删除" value="DELETE"/><el-option label="登录" value="LOGIN"/><el-option label="退出" value="LOGOUT"/></el-select></el-form-item>
        <el-form-item label="结果"><el-select v-model="query.result" clearable placeholder="全部" style="width:110px"><el-option label="成功" value="SUCCESS"/><el-option label="失败" value="FAILED"/></el-select></el-form-item>
        <el-form-item label="时间"><el-date-picker v-model="query.range" type="datetimerange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DDTHH:mm:ss" style="width:350px"/></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
      <el-table :data="logs" v-loading="loading" row-key="id" empty-text="暂无操作日志">
        <el-table-column prop="id" label="ID" width="70"/>
        <el-table-column label="时间" width="175"><template #default="{row}">{{ formatTime(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作人" width="90"><template #default="{row}">{{ row.operatorId || '访客' }}</template></el-table-column>
        <el-table-column label="模块" width="110"><template #default="{row}">{{ moduleName(row.module) }}</template></el-table-column>
        <el-table-column label="动作" width="95"><template #default="{row}"><el-tag effect="plain">{{ actionName(row.action) }}</el-tag></template></el-table-column>
        <el-table-column label="请求" min-width="250"><template #default="{row}"><span class="method">{{ row.requestMethod }}</span><span class="uri">{{ row.requestUri }}</span></template></el-table-column>
        <el-table-column label="结果" width="90"><template #default="{row}"><el-tag :type="row.result==='SUCCESS'?'success':'danger'">{{ row.result==='SUCCESS'?'成功':'失败' }}</el-tag></template></el-table-column>
        <el-table-column prop="ipAddress" label="IP 地址" width="145" show-overflow-tooltip/>
        <el-table-column label="详情" width="85" fixed="right"><template #default="{row}"><el-button link type="primary" @click="showDetail(row.id)">查看</el-button></template></el-table-column>
      </el-table>
      <div class="pager"><el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @current-change="load" @size-change="sizeChanged"/></div>
    </el-card>
    <el-dialog v-model="detail.visible" title="操作日志详情" width="650px">
      <el-descriptions v-if="detail.data" :column="2" border>
        <el-descriptions-item label="日志 ID">{{ detail.data.id }}</el-descriptions-item><el-descriptions-item label="操作人">{{ detail.data.operatorId || '访客/未登录' }}</el-descriptions-item>
        <el-descriptions-item label="模块/动作">{{ moduleName(detail.data.module) }} / {{ actionName(detail.data.action) }}</el-descriptions-item><el-descriptions-item label="结果">{{ detail.data.result }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.data.requestMethod }}</el-descriptions-item><el-descriptions-item label="IP 地址">{{ detail.data.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ detail.data.requestUri }}</el-descriptions-item><el-descriptions-item label="记录时间" :span="2">{{ formatTime(detail.data.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="执行详情" :span="2"><pre>{{ detail.data.detail || '-' }}</pre></el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getOperationLog, getOperationLogs } from '@/api/audit'
const logs=ref([]);const loading=ref(false);const query=reactive({operatorId:undefined,module:'',action:'',result:'',range:[]});const page=reactive({current:1,size:10,total:0});const detail=reactive({visible:false,data:null})
const modules=['AUTH','USERS','CREDIT','ROOM_TYPES','INVENTORY','BOOKING','PAYMENT','ROOM','FRONTDESK','PRICING','CONTENT','SYSTEM_CONFIG'];const moduleName=x=>({AUTH:'认证',USERS:'用户',CREDIT:'信用',ROOM_TYPES:'房型',INVENTORY:'库存',BOOKING:'订单',PAYMENT:'支付',ROOM:'房间',FRONTDESK:'前台',PRICING:'价格',CONTENT:'内容',SYSTEM_CONFIG:'系统参数'}[x]||x);const actionName=x=>({CREATE:'新增',UPDATE:'更新',DELETE:'删除',LOGIN:'登录',LOGOUT:'退出'}[x]||x);const formatTime=v=>v?new Date(v).toLocaleString('zh-CN',{hour12:false}):'-'
async function load(){loading.value=true;try{const params={current:page.current,size:page.size,operatorId:query.operatorId||undefined,module:query.module||undefined,action:query.action||undefined,result:query.result||undefined,startTime:query.range?.[0],endTime:query.range?.[1]};const res=await getOperationLogs(params);logs.value=res.data.records||[];page.total=Number(res.data.total||0)}catch(e){ElMessage.error(e?.response?.data?.message||'操作日志加载失败')}finally{loading.value=false}}
function search(){page.current=1;load()}function reset(){Object.assign(query,{operatorId:undefined,module:'',action:'',result:'',range:[]});search()}function sizeChanged(){page.current=1;load()}
async function showDetail(id){try{const res=await getOperationLog(id);detail.data=res.data;detail.visible=true}catch(e){ElMessage.error(e?.response?.data?.message||'日志详情加载失败')}}
load()
</script>

<style scoped>
.audit-page{display:grid;gap:18px}.page-title{display:flex;align-items:center;justify-content:space-between;padding:24px;border-radius:22px;background:rgba(255,255,255,.72)}h1{margin:0 0 6px;color:#2c2416;font-size:24px}.page-title p{margin:0;color:#827568}.panel{border:none;border-radius:22px;background:rgba(255,255,255,.76)}.filters{padding:4px 0 10px}.method{display:inline-block;min-width:52px;margin-right:8px;color:#a66f3d;font-weight:700}.uri{color:#5f554a}.pager{display:flex;justify-content:flex-end;margin-top:20px}pre{margin:0;white-space:pre-wrap;word-break:break-word;font-family:inherit;color:#5f554a}
</style>
