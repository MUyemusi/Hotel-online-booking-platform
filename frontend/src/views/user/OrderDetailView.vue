
<template><section v-loading="loading"><el-page-header @back="router.push('/orders')" content="订单详情" />
<el-card v-if="order" class="detail"><div class="heading"><div><h1>{{ order.roomTypeName }}</h1><p>订单号 {{ order.orderNo }}</p></div><el-tag size="large">{{ statusText(order.orderStatus) }}</el-tag></div>
<el-alert v-if="order.stayType === 'EARLY_MORNING'" title="凌晨入住：当日中午退房，按前一晚价格计费" type="info" :closable="false" show-icon/><el-descriptions :column="2" border><el-descriptions-item label="入住日期">{{ order.checkInDate }}</el-descriptions-item><el-descriptions-item label="离店日期">{{ order.checkOutDate }}</el-descriptions-item><el-descriptions-item label="房间数">{{ order.roomCount }}</el-descriptions-item><el-descriptions-item label="入住人数">{{ order.guestCount }}</el-descriptions-item><el-descriptions-item label="联系电话">{{ order.phone }}</el-descriptions-item><el-descriptions-item label="总金额">￥{{ order.totalAmount }}</el-descriptions-item></el-descriptions>
<h3>每日价格</h3><el-table :data="order.dailyPrices||[]"><el-table-column prop="priceDate" label="日期"/><el-table-column prop="dailyPrice" label="价格"/></el-table>
<div class="actions"><el-button v-if="order.orderStatus==='PENDING_PAYMENT'" type="primary" @click="router.push('/orders/'+order.id+'/pay')">去支付</el-button><el-button v-if="canCancel" type="danger" plain @click="cancel">取消订单</el-button></div>
</el-card></section></template>
<script setup>
import { computed,onMounted,ref } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { ElMessage,ElMessageBox } from 'element-plus'
import { cancelOrder,getOrderDetail } from '@/api/orders'
const route=useRoute(),router=useRouter(),order=ref(),loading=ref(false)
const labels={PENDING_PAYMENT:'待支付',PENDING_CHECK_IN:'待入住',CHECKED_IN:'已入住',COMPLETED:'已完成',CANCELLED:'已取消',CLOSED:'已关闭'}
const statusText=s=>labels[s]||s
const canCancel=computed(()=>['PENDING_PAYMENT','PENDING_CHECK_IN'].includes(order.value?.orderStatus))
async function load(){loading.value=true;try{const r=await getOrderDetail(route.params.id);if(r.code!=='SUCCESS')throw new Error(r.message);order.value=r.data}catch(e){ElMessage.error(e?.response?.data?.message||e.message)}finally{loading.value=false}}
async function cancel(){const {value}=await ElMessageBox.prompt('请输入取消原因','取消订单',{inputPattern:/\S+/,inputErrorMessage:'取消原因不能为空'});const r=await cancelOrder(order.value.id,value);if(r.code!=='SUCCESS')throw new Error(r.message);ElMessage.success('订单已取消');await load()}
onMounted(load)
</script>
<style scoped>.detail{margin-top:24px}.heading{display:flex;justify-content:space-between}.heading p{color:#64748b}.actions{margin-top:22px;display:flex;gap:12px}</style>
