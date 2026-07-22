<template>
  <section>
    <div class="search-panel">
      <div><h1>查找可订房型</h1><p>价格已包含日期规则，提交订单时会再次校验。</p></div>
      <el-date-picker v-model="dates" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="入住日期" end-placeholder="离店日期" :disabled-date="disablePast" />
      <el-input-number v-model="roomCount" :min="1" :max="5" />
      <el-button type="primary" :loading="loading" @click="search">查询</el-button>
    </div>
    <el-empty v-if="!loading && !rooms.length" description="该日期暂无可订房型" />
    <div class="grid">
      <el-card v-for="room in rooms" :key="room.id" class="room-card">
        <img v-if="room.mainImageUrl" :src="room.mainImageUrl" :alt="room.name" />
        <div class="room-body">
          <h2>{{ room.name }}</h2>
          <p>{{ room.area }}㎡ · {{ room.bedType }} · 最多{{ room.maxGuests }}人</p>
          <p>{{ room.facilities?.join(' · ') || '基础设施齐全' }}</p>
          <strong>￥{{ room.totalAmount }} / {{ nights }}晚</strong><span>剩余 {{ room.availableRooms }} 间</span>
          <div><el-button @click="detail(room.id)">查看详情</el-button><el-button type="primary" @click="book(room.id)">立即预订</el-button></div>
        </div>
      </el-card>
    </div>
  </section>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchRoomTypes } from '@/api/roomTypes'
import { getAccessToken } from '@/utils/request'
const route=useRoute(),router=useRouter(),rooms=ref([]),loading=ref(false)
const tomorrow=()=>{const d=new Date();d.setDate(d.getDate()+1);return d.toISOString().slice(0,10)}
const nextDay=()=>{const d=new Date();d.setDate(d.getDate()+2);return d.toISOString().slice(0,10)}
const dates=ref([String(route.query.checkInDate||tomorrow()),String(route.query.checkOutDate||nextDay())])
const roomCount=ref(Number(route.query.roomCount||1))
const nights=computed(()=>Math.max(1,Math.round((new Date(dates.value[1])-new Date(dates.value[0]))/86400000)))
const disablePast=date=>date<new Date(new Date().toDateString())
function query(id){return {roomTypeId:id,checkInDate:dates.value[0],checkOutDate:dates.value[1],roomCount:roomCount.value}}
async function search(){if(!dates.value?.[0]||!dates.value?.[1])return;loading.value=true;try{const r=await searchRoomTypes({checkInDate:dates.value[0],checkOutDate:dates.value[1],roomCount:roomCount.value});if(r.code!=='SUCCESS')throw new Error(r.message);rooms.value=r.data||[];router.replace({path:'/rooms',query:query()})}catch(e){rooms.value=[];ElMessage.error(e?.response?.data?.message||e.message||'查询失败')}finally{loading.value=false}}
function detail(id){router.push({path:'/rooms/'+id,query:query(id)})}
function book(id){const target={path:'/booking/confirm',query:query(id)};if(!getAccessToken())router.push({path:'/login',query:{redirect:router.resolve(target).fullPath}});else router.push(target)}
onMounted(search)
</script>
<style scoped>
.search-panel{display:flex;align-items:end;gap:14px;flex-wrap:wrap;padding:24px;background:white;border-radius:20px;margin-bottom:24px}.search-panel>div:first-child{margin-right:auto}.search-panel h1{margin:0}.search-panel p{color:#64748b}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(290px,1fr));gap:20px}.room-card img{width:100%;height:190px;object-fit:cover}.room-body{display:grid;gap:10px}.room-body h2{margin:8px 0 0}.room-body p{margin:0;color:#64748b}.room-body strong{font-size:22px;color:#dc2626}.room-body span{color:#0f766e}.room-body div{display:flex;gap:10px}
</style>