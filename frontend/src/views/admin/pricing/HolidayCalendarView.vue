
<template>
  <div class="holiday-calendar-view">
    <div class="page-header">
      <div>
        <h1>节假日日历管理</h1>
        <p>维护法定节假日、调休工作日及价格规则预览。</p>
      </div>
      <div class="header-actions">
        <el-select v-model="filters.holidayYear" placeholder="全部年份" class="filter-select" @change="handleSearch">
          <el-option label="全部年份" :value="null" />
          <el-option label="2026" :value="2026" />
          <el-option label="2027" :value="2027" />
        </el-select>
        <el-select v-model="filters.dayType" placeholder="全部类型" class="filter-select" @change="handleSearch">
          <el-option label="全部类型" value="" />
          <el-option :label="displayValue('dayType', 'HOLIDAY')" value="HOLIDAY" />
          <el-option :label="displayValue('dayType', 'MAKEUP_WORKDAY')" value="MAKEUP_WORKDAY" />
        </el-select>
        <el-select v-model="filters.sourceStatus" placeholder="全部来源" class="filter-select" @change="handleSearch">
          <el-option label="全部来源" value="" />
          <el-option :label="displayValue('sourceStatus', 'OFFICIAL')" value="OFFICIAL" />
          <el-option :label="displayValue('sourceStatus', 'PROVISIONAL')" value="PROVISIONAL" />
        </el-select>
        <el-button :loading="loading" @click="loadData">重新加载</el-button>
        <el-button type="primary" @click="openCreateDialog">新增日期</el-button>
      </div>
    </div>

    <el-alert
      title="部分未来节假日数据为暂定日期，请在官方放假通知发布后及时更新。"
      type="warning"
      :closable="false"
      show-icon
    />

    <el-card shadow="never" class="table-card">
      <div class="preview-bar">
        <el-date-picker v-model="previewDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期预览" />
        <el-button @click="handlePreview">预览规则</el-button>
        <span v-if="previewResult" class="preview-result">
          {{ previewResult.date }} / {{ previewResult.priceSource }} / {{ previewResult.coefficient }} / {{ previewResult.description }}
        </span>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="businessDate" label="日期" width="120" />
        <el-table-column label="星期" width="120">
          <template #default="scope">{{ getWeekday(scope.row.businessDate) }}</template>
        </el-table-column>
        <el-table-column prop="holidayName" label="节假日名称" min-width="220" />
        <el-table-column label="日期类型" width="170">
          <template #default="scope">
            <el-tag :type="scope.row.dayType === 'HOLIDAY' ? 'danger' : 'primary'">
              {{ displayValue('dayType', scope.row.dayType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="coefficient" label="价格系数" width="100" />
        <el-table-column label="数据来源" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.sourceStatus === 'OFFICIAL' ? 'success' : 'warning'">
              {{ displayValue('sourceStatus', scope.row.sourceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ENABLED' ? 'success' : 'info'">
              {{ displayValue('status', scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button link :type="scope.row.status === 'ENABLED' ? 'warning' : 'success'" @click="handleToggleStatus(scope.row)">
                {{ scope.row.status === 'ENABLED' ? '停用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增节假日日期' : '编辑节假日日期'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="日期" prop="businessDate">
          <el-date-picker v-model="form.businessDate" type="date" value-format="YYYY-MM-DD" class="full-width" />
        </el-form-item>
        <el-form-item label="节假日名称" prop="holidayName">
          <el-input v-model="form.holidayName" maxlength="100" show-word-limit />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="节假日类型" prop="holidayCode">
            <el-select v-model="form.holidayCode" class="full-width">
              <el-option :label="displayValue('holidayCode', 'MID_AUTUMN')" value="MID_AUTUMN" />
              <el-option :label="displayValue('holidayCode', 'NATIONAL_DAY')" value="NATIONAL_DAY" />
              <el-option :label="displayValue('holidayCode', 'NEW_YEAR')" value="NEW_YEAR" />
              <el-option :label="displayValue('holidayCode', 'SPRING_FESTIVAL')" value="SPRING_FESTIVAL" />
              <el-option :label="displayValue('holidayCode', 'QINGMING')" value="QINGMING" />
              <el-option :label="displayValue('holidayCode', 'LABOR_DAY')" value="LABOR_DAY" />
              <el-option :label="displayValue('holidayCode', 'DRAGON_BOAT')" value="DRAGON_BOAT" />
              <el-option :label="displayValue('holidayCode', 'CUSTOM')" value="CUSTOM" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期类型" prop="dayType">
            <el-select v-model="form.dayType" class="full-width">
              <el-option :label="displayValue('dayType', 'HOLIDAY')" value="HOLIDAY" />
              <el-option :label="displayValue('dayType', 'MAKEUP_WORKDAY')" value="MAKEUP_WORKDAY" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="价格系数" prop="coefficient">
            <el-input-number v-model="form.coefficient" :min="1" :step="0.05" :precision="2" class="full-width" />
          </el-form-item>
          <el-form-item label="年份" prop="holidayYear">
            <el-input-number v-model="form.holidayYear" :min="2026" :max="2035" :step="1" class="full-width" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="数据来源" prop="sourceStatus">
            <el-select v-model="form.sourceStatus" class="full-width">
              <el-option :label="displayValue('sourceStatus', 'OFFICIAL')" value="OFFICIAL" />
              <el-option :label="displayValue('sourceStatus', 'PROVISIONAL')" value="PROVISIONAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" class="full-width">
              <el-option :label="displayValue('status', 'ENABLED')" value="ENABLED" />
              <el-option :label="displayValue('status', 'DISABLED')" value="DISABLED" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSystemConfig } from '@/api/systemconfig/systemConfig'
import { displayValue, weekdayName } from '@/utils/adminDisplay'
import {
  createHolidayCalendar,
  deleteHolidayCalendar,
  getHolidayCalendar,
  previewHolidayRule,
  updateHolidayCalendar,
  updateHolidayCalendarStatus
} from '@/api/pricing/holidayCalendar'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref()
const rows = ref([])
const previewDate = ref('2026-10-03')
const previewResult = ref(null)
const editingId = ref(null)

const filters = reactive({
  holidayYear: null,
  dayType: '',
  sourceStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const createDefaultForm = () => ({
  businessDate: '',
  holidayName: '',
  holidayCode: 'MID_AUTUMN',
  dayType: 'HOLIDAY',
  coefficient: 1.2,
  holidayYear: 2026,
  sourceStatus: 'OFFICIAL',
  status: 'ENABLED',
  remark: ''
})

const form = reactive(createDefaultForm())

const rules = {
  businessDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  holidayName: [{ required: true, message: '请输入节假日名称', trigger: 'blur' }],
  holidayCode: [{ required: true, message: '请选择节假日类型', trigger: 'change' }],
  dayType: [{ required: true, message: '请选择日期类型', trigger: 'change' }],
  coefficient: [{ required: true, message: '请输入价格系数', trigger: 'change' }],
  holidayYear: [{ required: true, message: '请选择年份', trigger: 'change' }],
  sourceStatus: [{ required: true, message: '请选择数据来源', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function extractResult(result, fallbackMessage) {
  if (result.code !== 'SUCCESS') {
    throw new Error(result.message || fallbackMessage)
  }
  return result.data
}

function resetForm() {
  Object.assign(form, createDefaultForm())
  editingId.value = null
}

async function loadData() {
  loading.value = true
  try {
    const result = await getHolidayCalendar({
      page: pagination.page,
      size: pagination.size,
      holidayYear: filters.holidayYear ?? undefined,
      dayType: filters.dayType || undefined,
      sourceStatus: filters.sourceStatus || undefined
    })
    const data = extractResult(result, '加载节假日日历失败')
    rows.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载节假日日历失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handlePageChange(page) {
  pagination.page = page
  loadData()
}

async function handlePreview() {
  if (!previewDate.value) {
    ElMessage.warning('请先选择预览日期')
    return
  }
  try {
    previewResult.value = extractResult(await previewHolidayRule(previewDate.value), '预览价格规则失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '预览价格规则失败')
  }
}

async function openCreateDialog() {
  dialogMode.value = 'create'
  resetForm()
  try {
    const result = await getSystemConfig('DEFAULT_HOLIDAY_COEFFICIENT')
    if (result.code === 'SUCCESS') form.coefficient = Number(result.data.configValue)
  } catch (error) {
    // 保留页面默认值，避免配置暂不可用时阻断新增
  }
  dialogVisible.value = true
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, createDefaultForm(), {
    businessDate: row.businessDate,
    holidayName: row.holidayName,
    holidayCode: row.holidayCode,
    dayType: row.dayType,
    coefficient: Number(row.coefficient),
    holidayYear: row.holidayYear,
    sourceStatus: row.sourceStatus,
    status: row.status,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      ...form,
      coefficient: Number(form.coefficient).toFixed(2)
    }
    if (dialogMode.value === 'create') {
      extractResult(await createHolidayCalendar(payload), '新增节假日日期失败')
      ElMessage.success('节假日日期已新增')
    } else {
      extractResult(await updateHolidayCalendar(editingId.value, payload), '更新节假日日期失败')
      ElMessage.success('节假日日期已更新')
    }
    dialogVisible.value = false
    await loadData()
    await handlePreview()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  try {
    const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    extractResult(await updateHolidayCalendarStatus(row.id, nextStatus), '更新状态失败')
    ElMessage.success(nextStatus === 'ENABLED' ? '记录已启用' : '记录已停用')
    await loadData()
    await handlePreview()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '更新状态失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.businessDate} 吗？`, '删除确认', { type: 'warning' })
    const result = await deleteHolidayCalendar(row.id)
    if (result.code !== 'SUCCESS') {
      throw new Error(result.message || '删除节假日日期失败')
    }
    ElMessage.success('节假日日期已删除')
    await loadData()
    await handlePreview()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    ElMessage.error(error?.response?.data?.message || error?.message || '删除节假日日期失败')
  }
}

function getWeekday(dateString) {
  const date = new Date(dateString)
  return weekdayName(dateString)
}

loadData()
handlePreview()
</script>

<style scoped>
.holiday-calendar-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  color: #1f2937;
}

.page-header p {
  margin: 0;
  color: #6b7280;
}

.header-actions,
.dialog-actions,
.table-actions,
.preview-bar {
  display: flex;
  gap: 12px;
}

.header-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.table-card {
  border-radius: 20px;
}

.filter-select {
  width: 160px;
}

.preview-bar {
  margin-bottom: 18px;
  align-items: center;
  flex-wrap: wrap;
}

.preview-result {
  color: #92400e;
  font-weight: 600;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.full-width {
  width: 100%;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .filter-select {
    width: 100%;
  }
}
</style>
