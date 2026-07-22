<template>
  <div class="rule-view">
    <div class="page-header">
      <div><h1>价格规则管理</h1><p>按日期范围、适用房型和优先级维护价格系数；数字越大优先级越高。</p></div>
      <el-button type="primary" @click="openCreate">新增规则</el-button>
    </div>
    <el-card shadow="never">
      <div class="filters">
        <el-select v-model="filters.status" clearable placeholder="全部状态" @change="loadRules">
          <el-option label="启用" value="ENABLED"/><el-option label="停用" value="DISABLED"/>
        </el-select>
        <el-select v-model="filters.roomTypeId" clearable placeholder="全部房型" @change="loadRules">
          <el-option v-for="room in roomTypes" :key="room.id" :label="room.name" :value="room.id"/>
        </el-select>
        <el-button :loading="loading" @click="loadRules">刷新</el-button>
      </div>
      <el-table :data="records" stripe empty-text="暂无价格规则">
        <el-table-column prop="ruleName" label="规则名称" min-width="150"/>
        <el-table-column label="日期范围" min-width="210"><template #default="s">{{ s.row.startDate }} 至 {{ s.row.endDate }}</template></el-table-column>
        <el-table-column label="适用房型" min-width="140"><template #default="s">{{ roomTypeName(s.row.roomTypeId) }}</template></el-table-column>
        <el-table-column prop="coefficient" label="系数" width="90"/>
        <el-table-column prop="priority" label="优先级" width="90"/>
        <el-table-column label="状态" width="90"><template #default="s"><el-tag :type="s.row.status === 'ENABLED' ? 'success' : 'info'">{{ s.row.status === 'ENABLED' ? '启用' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="220" fixed="right"><template #default="s">
          <el-button link type="primary" @click="openEdit(s.row)">编辑</el-button>
          <el-button link :type="s.row.status === 'ENABLED' ? 'warning' : 'success'" @click="toggle(s.row)">{{ s.row.status === 'ENABLED' ? '停用' : '启用' }}</el-button>
          <el-button link type="danger" @click="remove(s.row)">删除</el-button>
        </template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="filters.page" :page-size="filters.size" :total="total" layout="prev, pager, next, total" @current-change="loadRules"/>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑价格规则' : '新增价格规则'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="规则名称" prop="ruleName"><el-input v-model="form.ruleName" maxlength="100"/></el-form-item>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="开始日期" prop="startDate"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" class="full"/></el-form-item></el-col><el-col :span="12"><el-form-item label="结束日期" prop="endDate"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" class="full"/></el-form-item></el-col></el-row>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="适用房型"><el-select v-model="form.roomTypeId" clearable placeholder="全部房型" class="full"><el-option v-for="room in roomTypes" :key="room.id" :label="room.name" :value="room.id"/></el-select></el-form-item></el-col><el-col :span="6"><el-form-item label="价格系数" prop="coefficient"><el-input-number v-model="form.coefficient" :min="0.01" :step="0.05" :precision="2" class="full"/></el-form-item></el-col><el-col :span="6"><el-form-item label="优先级" prop="priority"><el-input-number v-model="form.priority" :min="0" :max="9999" class="full"/></el-form-item></el-col></el-row>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio value="ENABLED">启用</el-radio><el-radio value="DISABLED">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" maxlength="255"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBasePrices } from '@/api/pricing/basePrice'
import { createPriceRule, deletePriceRule, getPriceRules, updatePriceRule, updatePriceRuleStatus } from '@/api/pricing/priceRule'
const loading=ref(false), submitting=ref(false), dialogVisible=ref(false), editingId=ref(null), formRef=ref()
const records=ref([]), roomTypes=ref([]), total=ref(0)
const filters=reactive({page:1,size:10,status:'',roomTypeId:null})
const empty=()=>({ruleName:'',startDate:'',endDate:'',roomTypeId:null,coefficient:1.2,priority:100,status:'ENABLED',remark:''})
const form=reactive(empty())
const rules={ruleName:[{required:true,message:'请输入规则名称',trigger:'blur'}],startDate:[{required:true,message:'请选择开始日期',trigger:'change'}],endDate:[{required:true,message:'请选择结束日期',trigger:'change'}],coefficient:[{required:true,message:'请输入价格系数',trigger:'change'}],priority:[{required:true,message:'请输入优先级',trigger:'change'}]}
const dataOf=(r,msg)=>{if(r.code!=='SUCCESS')throw new Error(r.message||msg);return r.data}
async function loadRooms(){const d=dataOf(await getBasePrices({page:1,size:1000}),'加载房型失败');roomTypes.value=d.records||[]}
async function loadRules(){loading.value=true;try{const d=dataOf(await getPriceRules({...filters}),'加载规则失败');records.value=d.records||[];total.value=d.total||0}catch(e){ElMessage.error(e?.response?.data?.message||e.message)}finally{loading.value=false}}
function roomTypeName(id){if(!id)return '全部房型';return roomTypes.value.find(x=>x.id===id)?.name||`房型 ${id}`}
function openCreate(){editingId.value=null;Object.assign(form,empty());dialogVisible.value=true}
function openEdit(row){editingId.value=row.id;Object.assign(form,empty(),row);dialogVisible.value=true}
async function submit(){if(!await formRef.value.validate().catch(()=>false))return;if(form.endDate<form.startDate){ElMessage.warning('结束日期不能早于开始日期');return}submitting.value=true;try{const payload={...form};editingId.value?await updatePriceRule(editingId.value,payload):await createPriceRule(payload);ElMessage.success('价格规则保存成功');dialogVisible.value=false;await loadRules()}catch(e){ElMessage.error(e?.response?.data?.message||e.message)}finally{submitting.value=false}}
async function toggle(row){try{await updatePriceRuleStatus(row.id,row.status==='ENABLED'?'DISABLED':'ENABLED');await loadRules()}catch(e){ElMessage.error(e?.response?.data?.message||e.message)}}
async function remove(row){await ElMessageBox.confirm(`确定删除“${row.ruleName}”吗？`,'删除确认',{type:'warning'});try{await deletePriceRule(row.id);ElMessage.success('已删除');await loadRules()}catch(e){ElMessage.error(e?.response?.data?.message||e.message)}}
onMounted(async()=>{try{await loadRooms()}catch(e){ElMessage.error(e.message)}await loadRules()})
</script>
<style scoped>
.rule-view{display:flex;flex-direction:column;gap:20px}.page-header{display:flex;justify-content:space-between;align-items:flex-start;gap:16px}.page-header h1{margin:0 0 8px;font-size:28px}.page-header p{margin:0;color:#64748b}.filters{display:flex;gap:12px;margin-bottom:16px}.el-pagination{margin-top:16px;justify-content:flex-end}.full{width:100%}@media(max-width:700px){.page-header,.filters{flex-direction:column}.filters>*{width:100%}}
</style>
