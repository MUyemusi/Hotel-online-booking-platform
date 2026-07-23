
<template><section><div class="title"><h1>我的订单</h1><el-select v-model="status" clearable placeholder="全部状态" @change="load"><el-option v-for="s in statuses" :key="s" :label="statusText(s)" :value="s"/></el-select></div>
<el-empty v-if="!loading&&!records.length" description="暂无订单" />
<el-card v-for="order in records" :key="order.id" class="order" @click="router.push('/orders/'+order.id)">
  <div><strong>{{ order.roomTypeName }}</strong><p v-if="order.stayType === 'EARLY_MORNING'">{{ order.checkInDate }} 凌晨入住 · 当日中午退房 · {{ order.roomCount }}间</p><p v-else>{{ order.checkInDate }} 至 {{ order.checkOutDate }} · {{ order.roomCount }}间</p></div>
  <div><el-tag>{{ statusText(order.orderStatus) }}</el-tag><h3>￥{{ order.totalAmount }}</h3></div>
</el-card>
<el-pagination v-if="total>size" v-model:current-page="page" :page-size="size" :total="total" @current-change="load"/>
</section></template>
<script setup>
import { onMounted,ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyOrders } from '@/api/orders'
const router=useRouter(),records=ref([]),loading=ref(false),status=ref(''),page=ref(1),size=10,total=ref(0)
const statuses=['PENDING_PAYMENT','PENDING_CHECK_IN','CHECKED_IN','COMPLETED','CANCELLED','CLOSED']
const labels={PENDING_PAYMENT:'待支付',PENDING_CHECK_IN:'待入住',CHECKED_IN:'已入住',COMPLETED:'已完成',CANCELLED:'已取消',CLOSED:'已关闭'}
const statusText=s=>labels[s]||s
async function load(){loading.value=true;try{const r=await getMyOrders({status:status.value||undefined,page:page.value,size});if(r.code!=='SUCCESS')throw new Error(r.message);records.value=r.data.records||[];total.value=r.data.total||0}catch(e){ElMessage.error(e?.response?.data?.message||e.message)}finally{loading.value=false}}
onMounted(load)
</script>
<style scoped>.title{display:flex;justify-content:space-between;align-items:center}.order{margin:14px 0;cursor:pointer}.order :deep(.el-card__body){display:flex;justify-content:space-between}.order p{color:#64748b}.order h3{color:#dc2626;text-align:right}</style>
