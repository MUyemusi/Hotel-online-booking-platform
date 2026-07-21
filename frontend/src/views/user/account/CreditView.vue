<template>
  <section class="credit-page">
    <div class="page-head"><span class="eyebrow">CREDIT CENTER</span><h1>信用中心</h1><p>良好的履约记录，让每次入住更从容</p></div>
    <div class="summary-grid" v-loading="loading">
      <div class="summary-card" :class="summary.bookingAllowed ? 'allowed' : 'blocked'">
        <span>预订状态</span><strong>{{ summary.bookingAllowed ? '可以正常预订' : '暂时限制预订' }}</strong><small>{{ summary.bookingAllowed ? '信用状态良好' : `限制至 ${formatTime(summary.banUntil)}` }}</small>
      </div>
      <div class="summary-card"><span>连续主动取消</span><strong>{{ summary.consecutiveCancelCount || 0 }} <em>次</em></strong><small>达到 {{ summary.cancellationLimit || 3 }} 次将触发临时限制</small></div>
      <div class="summary-card"><span>剩余安全次数</span><strong>{{ remaining }} <em>次</em></strong><small>正常完成入住后连续次数将重置</small></div>
    </div>
    <el-card shadow="never" class="records-card">
      <template #header><div class="records-head"><div><b>信用变更记录</b><p>记录取消、禁订、完成入住与解除限制</p></div><el-button @click="loadAll">刷新</el-button></div></template>
      <el-table :data="records" v-loading="recordsLoading" empty-text="暂无信用变更记录">
        <el-table-column label="类型" width="110"><template #default="{ row }"><el-tag :type="tagType(row.changeType)">{{ typeName(row.changeType) }}</el-tag></template></el-table-column>
        <el-table-column label="次数变化" width="110"><template #default="{ row }">{{ row.beforeCount ?? '-' }} → {{ row.afterCount ?? '-' }}</template></el-table-column>
        <el-table-column prop="orderId" label="关联订单" width="120"><template #default="{ row }">{{ row.orderId || '-' }}</template></el-table-column>
        <el-table-column prop="reason" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column label="发生时间" width="180"><template #default="{ row }">{{ formatTime(row.createdAt) }}</template></el-table-column>
      </el-table>
      <div class="pager"><el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total" layout="total, prev, pager, next" @current-change="loadRecords" /></div>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyCredit, getMyCreditRecords } from '@/api/user'
const summary = reactive({}); const records = ref([]); const page = reactive({ current: 1, size: 10, total: 0 })
const loading = ref(false); const recordsLoading = ref(false)
const remaining = computed(() => Math.max((summary.cancellationLimit || 3) - (summary.consecutiveCancelCount || 0), 0))
const formatTime = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-'
const typeName = type => ({ CANCEL: '主动取消', BAN: '限制预订', COMPLETE: '完成入住', UNBAN: '解除限制' }[type] || type)
const tagType = type => ({ CANCEL: 'warning', BAN: 'danger', COMPLETE: 'success', UNBAN: 'info' }[type] || '')
async function loadSummary() { loading.value = true; try { const res = await getMyCredit(); Object.assign(summary, res.data) } catch (e) { ElMessage.error(e?.response?.data?.message || '信用信息加载失败') } finally { loading.value = false } }
async function loadRecords() { recordsLoading.value = true; try { const res = await getMyCreditRecords({ current: page.current, size: page.size }); records.value = res.data.records || []; page.total = Number(res.data.total || 0) } catch (e) { ElMessage.error(e?.response?.data?.message || '信用记录加载失败') } finally { recordsLoading.value = false } }
function loadAll() { loadSummary(); loadRecords() }
onMounted(loadAll)
</script>

<style scoped>
.credit-page { max-width: 1160px; margin: 0 auto; padding: 24px 12px; }.page-head { margin-bottom: 26px; }.eyebrow { color:#c8945c; font-size:11px; letter-spacing:.22em; }h1{margin:6px 0;color:#2c2416;font-size:34px}.page-head p{margin:0;color:#8f8170}.summary-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:18px;margin-bottom:24px}.summary-card{padding:25px;border:1px solid rgba(255,255,255,.75);border-radius:22px;background:rgba(255,255,255,.68);box-shadow:0 12px 30px rgba(44,36,22,.06)}.summary-card span,.summary-card small{display:block;color:#918270;font-size:13px}.summary-card strong{display:block;margin:12px 0 8px;color:#3c3126;font-size:24px}.summary-card em{font-size:14px;font-style:normal}.summary-card.allowed{border-left:5px solid #67c23a}.summary-card.blocked{border-left:5px solid #f56c6c}.records-card{border:1px solid rgba(255,255,255,.7);border-radius:22px;background:rgba(255,255,255,.72)}.records-head{display:flex;justify-content:space-between;align-items:center}.records-head p{margin:5px 0 0;color:#9a8d80;font-size:12px}.pager{display:flex;justify-content:flex-end;margin-top:20px}@media(max-width:760px){.credit-page{padding:12px 2px}.summary-grid{grid-template-columns:1fr}}
.credit-page{padding-top:124px;padding-bottom:56px}
@media(max-width:760px){.credit-page{padding:108px 16px 40px}}
</style>
