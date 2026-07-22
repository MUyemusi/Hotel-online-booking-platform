<template><section v-loading="loading"><h1>订单支付</h1><el-card v-if="order"><h2>{{ order.roomTypeName }}</h2><p>{{ order.checkInDate }} 至 {{ order.checkOutDate }}</p><h3>支付金额：￥{{ order.totalAmount }}</h3>
<el-radio-group v-model="method"><el-radio-button value="WECHAT">微信模拟支付</el-radio-button><el-radio-button value="ALIPAY">支付宝模拟支付</el-radio-button><el-radio-button value="BANK_CARD">银行卡模拟支付</el-radio-button></el-radio-group>
<div><el-button type="primary" size="large" :loading="paying" @click="pay">确认支付</el-button><el-button @click="router.push('/orders/'+order.id)">稍后支付</el-button></div></el-card></section></template>
<script setup>
import { onMounted,ref } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail } from '@/api/orders'
import { payOrder } from '@/api/payments'
const route=useRoute(),router=useRouter(),order=ref(),loading=ref(false),paying=ref(false),method=ref('WECHAT')
async function load(){loading.value=true;try{const r=await getOrderDetail(route.params.id);if(r.code!=='SUCCESS')throw new Error(r.message);order.value=r.data;if(r.data.paymentStatus==='PAID')router.replace('/orders/'+r.data.id)}catch(e){ElMessage.error(e?.response?.data?.message||e.message)}finally{loading.value=false}}
async function pay(){paying.value=true;try{const keyName='payment_key_'+order.value.id;let key=sessionStorage.getItem(keyName);if(!key){key=crypto.randomUUID();sessionStorage.setItem(keyName,key)}const r=await payOrder({orderId:order.value.id,paymentMethod:method.value,idempotencyKey:key});if(r.code!=='SUCCESS')throw new Error(r.message);sessionStorage.removeItem(keyName);ElMessage.success('支付成功');router.replace('/orders/'+order.value.id)}catch(e){ElMessage.error(e?.response?.data?.message||e.message||'支付失败')}finally{paying.value=false}}
onMounted(load)
</script>
<style scoped>section{max-width:680px;margin:auto}h3{font-size:25px;color:#dc2626}.el-radio-group{display:flex;margin:20px 0}.el-card div:last-child{margin-top:24px}</style>