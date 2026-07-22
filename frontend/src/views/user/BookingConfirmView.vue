<template><section v-loading="loading"><h1>确认预订</h1>
  <el-card v-if="room"><h2>{{ room.name }}</h2><p>{{ checkInDate }} 至 {{ checkOutDate }}，{{ roomCount }}间</p><h3>应付总额：￥{{ room.totalAmount }}</h3></el-card>
  <el-card class="form-card"><el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
    <el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" /></el-form-item>
    <el-form-item label="入住人数" prop="guestCount"><el-input-number v-model="form.guestCount" :min="1" :max="room?.maxGuests*roomCount||8" /></el-form-item>
    <el-form-item label="入住人姓名" prop="guestName"><el-input v-model="form.guestName" /></el-form-item>
    <el-form-item label="身份证号"><el-input v-model="form.idNumber" /></el-form-item>
    <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
    <el-button type="primary" size="large" :loading="submitting" @click="submit">提交订单</el-button>
  </el-form></el-card>
</section></template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchRoomTypes } from '@/api/roomTypes'
import { createOrder } from '@/api/orders'
import { normalizeBookingQuery, toBookingQuery } from '@/utils/bookingQuery'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const room = ref()
const loading = ref(false)
const submitting = ref(false)
const booking = normalizeBookingQuery(route.query)
const roomTypeId = booking.roomTypeId
const checkInDate = booking.checkInDate
const checkOutDate = booking.checkOutDate
const roomCount = booking.roomCount
const form = reactive({ phone: '', guestCount: booking.guests, guestName: '', idNumber: '', remark: '' })
const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  guestName: [{ required: true, message: '请输入入住人', trigger: 'blur' }],
  guestCount: [{ required: true, message: '请输入人数', trigger: 'change' }]
}
function returnToRooms() {
  return router.replace({ path: '/rooms', query: toBookingQuery(booking) })
}
async function load() {
  if (!roomTypeId || !checkInDate || !checkOutDate) {
    ElMessage.error('预订参数不完整，请重新选择房型和日期')
    return returnToRooms()
  }
  loading.value = true
  try {
    const response = await searchRoomTypes({ checkInDate, checkOutDate, roomCount, guests: form.guestCount })
    if (response.code !== 'SUCCESS') throw new Error(response.message)
    room.value = (response.data || []).find(item => item.id === roomTypeId)
    if (!room.value) throw new Error('房型库存或价格已变化，请重新选择')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e.message || '报价失败')
    returnToRooms()
  } finally { loading.value = false }
}
async function submit() {
  if (!await formRef.value.validate().catch(() => false)) return
  submitting.value = true
  try {
    const latest = await searchRoomTypes({ checkInDate, checkOutDate, roomCount, guests: form.guestCount })
    const latestRoom = (latest.data || []).find(item => item.id === roomTypeId)
    if (!latestRoom) throw new Error('房型库存或价格已变化，请重新选择')
    room.value = latestRoom
    const payload = {
      roomTypeId, checkInDate, checkOutDate, roomCount, guestCount: form.guestCount,
      phone: form.phone, idempotencyKey: crypto.randomUUID(), remark: form.remark,
      guests: [{ guestName: form.guestName, guestPhone: form.phone, idType: form.idNumber ? 'ID_CARD' : null, idNumber: form.idNumber || null, isMain: true }]
    }
    const response = await createOrder(payload)
    if (response.code !== 'SUCCESS') throw new Error(response.message)
    ElMessage.success('订单创建成功')
    router.replace(`/orders/${response.data.id}/pay`)
  } catch (e) { ElMessage.error(e?.response?.data?.message || e.message || '下单失败') } finally { submitting.value = false }
}
onMounted(load)
</script>
<style scoped>section{max-width:780px;margin:auto}.form-card{margin-top:18px}h3{color:#dc2626}</style>