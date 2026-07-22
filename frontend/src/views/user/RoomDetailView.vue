<template>
  <section v-loading="loading">
    <el-page-header @back="router.back()" content="房型详情" />
    <el-card v-if="room" class="detail">
      <img v-if="room.mainImageUrl" :src="room.mainImageUrl" :alt="room.name" />
      <div><h1>{{ room.name }}</h1><p>{{ room.description }}</p><p>{{ room.facilities?.join(' · ') }}</p>
      <el-descriptions :column="2" border><el-descriptions-item label="面积">{{ room.area }}㎡</el-descriptions-item><el-descriptions-item label="床型">{{ room.bedType }}</el-descriptions-item><el-descriptions-item label="可住人数">{{ room.maxGuests }}</el-descriptions-item><el-descriptions-item label="剩余房量">{{ room.availableRooms }}</el-descriptions-item></el-descriptions>
      <h2>总价 ￥{{ room.totalAmount }}</h2><el-button type="primary" size="large" @click="book">预订此房型</el-button></div>
    </el-card>
  </section>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchRoomTypes } from '@/api/roomTypes'
import { getAccessToken } from '@/utils/request'
const route=useRoute(),router=useRouter(),room=ref(),loading=ref(false)
const q={roomTypeId:Number(route.params.id),checkInDate:String(route.query.checkInDate||''),checkOutDate:String(route.query.checkOutDate||''),roomCount:Number(route.query.roomCount||1)}
async function load(){loading.value=true;try{const r=await searchRoomTypes(q);room.value=(r.data||[]).find(x=>x.id===q.roomTypeId);if(!room.value)throw new Error('该房型当前不可订')}catch(e){ElMessage.error(e.message)}finally{loading.value=false}}
function book(){const target={path:'/booking/confirm',query:q};getAccessToken()?router.push(target):router.push({path:'/login',query:{redirect:router.resolve(target).fullPath}})}
onMounted(load)
</script>
<style scoped>.detail{margin-top:24px}.detail :deep(.el-card__body){display:grid;grid-template-columns:minmax(300px,1fr) 1fr;gap:28px}.detail img{width:100%;max-height:420px;object-fit:cover;border-radius:14px}.detail h2{color:#dc2626}@media(max-width:760px){.detail :deep(.el-card__body){grid-template-columns:1fr}}</style>