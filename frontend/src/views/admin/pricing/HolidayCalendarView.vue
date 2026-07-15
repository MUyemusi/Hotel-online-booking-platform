<template>
  <div class="holiday-calendar-view">
    <div class="page-header">
      <div>
        <h1>Holiday Calendar</h1>
        <p>Manage holiday dates, makeup workdays, and pricing rule previews.</p>
      </div>
      <div class="header-actions">
        <el-select v-model="filters.holidayYear" placeholder="All years" class="filter-select" @change="handleSearch">
          <el-option label="All years" :value="null" />
          <el-option label="2026" :value="2026" />
          <el-option label="2027" :value="2027" />
        </el-select>
        <el-select v-model="filters.dayType" placeholder="All types" class="filter-select" @change="handleSearch">
          <el-option label="All types" value="" />
          <el-option label="HOLIDAY" value="HOLIDAY" />
          <el-option label="MAKEUP_WORKDAY" value="MAKEUP_WORKDAY" />
        </el-select>
        <el-select v-model="filters.sourceStatus" placeholder="All sources" class="filter-select" @change="handleSearch">
          <el-option label="All sources" value="" />
          <el-option label="OFFICIAL" value="OFFICIAL" />
          <el-option label="PROVISIONAL" value="PROVISIONAL" />
        </el-select>
        <el-button :loading="loading" @click="loadData">Reload</el-button>
        <el-button type="primary" @click="openCreateDialog">Add Date</el-button>
      </div>
    </div>

    <el-alert
      title="Some 2027 records are provisional core statutory dates only and should be updated after official holiday notices are released."
      type="warning"
      :closable="false"
      show-icon
    />

    <el-card shadow="never" class="table-card">
      <div class="preview-bar">
        <el-date-picker v-model="previewDate" type="date" value-format="YYYY-MM-DD" placeholder="Select a date to preview" />
        <el-button @click="handlePreview">Preview Rule</el-button>
        <span v-if="previewResult" class="preview-result">
          {{ previewResult.date }} / {{ previewResult.priceSource }} / {{ previewResult.coefficient }} / {{ previewResult.description }}
        </span>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="businessDate" label="Date" width="120" />
        <el-table-column label="Weekday" width="120">
          <template #default="scope">{{ getWeekday(scope.row.businessDate) }}</template>
        </el-table-column>
        <el-table-column prop="holidayName" label="Name" min-width="220" />
        <el-table-column label="Type" width="170">
          <template #default="scope">
            <el-tag :type="scope.row.dayType === 'HOLIDAY' ? 'danger' : 'primary'">
              {{ scope.row.dayType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="coefficient" label="Coef." width="100" />
        <el-table-column label="Source" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.sourceStatus === 'OFFICIAL' ? 'success' : 'warning'">
              {{ scope.row.sourceStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Status" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ENABLED' ? 'success' : 'info'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="Remark" min-width="220" show-overflow-tooltip />
        <el-table-column label="Actions" width="220" fixed="right">
          <template #default="scope">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(scope.row)">Edit</el-button>
              <el-button link :type="scope.row.status === 'ENABLED' ? 'warning' : 'success'" @click="handleToggleStatus(scope.row)">
                {{ scope.row.status === 'ENABLED' ? 'Disable' : 'Enable' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)">Delete</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? 'Add Holiday Date' : 'Edit Holiday Date'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="Date" prop="businessDate">
          <el-date-picker v-model="form.businessDate" type="date" value-format="YYYY-MM-DD" class="full-width" />
        </el-form-item>
        <el-form-item label="Name" prop="holidayName">
          <el-input v-model="form.holidayName" maxlength="100" show-word-limit />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="Holiday Code" prop="holidayCode">
            <el-select v-model="form.holidayCode" class="full-width">
              <el-option label="MID_AUTUMN" value="MID_AUTUMN" />
              <el-option label="NATIONAL_DAY" value="NATIONAL_DAY" />
              <el-option label="NEW_YEAR" value="NEW_YEAR" />
              <el-option label="SPRING_FESTIVAL" value="SPRING_FESTIVAL" />
              <el-option label="QINGMING" value="QINGMING" />
              <el-option label="LABOR_DAY" value="LABOR_DAY" />
              <el-option label="DRAGON_BOAT" value="DRAGON_BOAT" />
              <el-option label="CUSTOM" value="CUSTOM" />
            </el-select>
          </el-form-item>
          <el-form-item label="Day Type" prop="dayType">
            <el-select v-model="form.dayType" class="full-width">
              <el-option label="HOLIDAY" value="HOLIDAY" />
              <el-option label="MAKEUP_WORKDAY" value="MAKEUP_WORKDAY" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="Coefficient" prop="coefficient">
            <el-input-number v-model="form.coefficient" :min="1" :step="0.05" :precision="2" class="full-width" />
          </el-form-item>
          <el-form-item label="Year" prop="holidayYear">
            <el-input-number v-model="form.holidayYear" :min="2026" :max="2035" :step="1" class="full-width" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="Source Status" prop="sourceStatus">
            <el-select v-model="form.sourceStatus" class="full-width">
              <el-option label="OFFICIAL" value="OFFICIAL" />
              <el-option label="PROVISIONAL" value="PROVISIONAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="Status" prop="status">
            <el-select v-model="form.status" class="full-width">
              <el-option label="ENABLED" value="ENABLED" />
              <el-option label="DISABLED" value="DISABLED" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="Remark" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-actions">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">Save</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
  businessDate: [{ required: true, message: 'Date is required', trigger: 'change' }],
  holidayName: [{ required: true, message: 'Name is required', trigger: 'blur' }],
  holidayCode: [{ required: true, message: 'Holiday code is required', trigger: 'change' }],
  dayType: [{ required: true, message: 'Day type is required', trigger: 'change' }],
  coefficient: [{ required: true, message: 'Coefficient is required', trigger: 'change' }],
  holidayYear: [{ required: true, message: 'Year is required', trigger: 'change' }],
  sourceStatus: [{ required: true, message: 'Source status is required', trigger: 'change' }],
  status: [{ required: true, message: 'Status is required', trigger: 'change' }]
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
    const data = extractResult(result, 'Failed to load holiday calendar')
    rows.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to load holiday calendar')
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
    ElMessage.warning('Please choose a date first')
    return
  }
  try {
    previewResult.value = extractResult(await previewHolidayRule(previewDate.value), 'Failed to preview rule')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to preview rule')
  }
}

function openCreateDialog() {
  dialogMode.value = 'create'
  resetForm()
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
      extractResult(await createHolidayCalendar(payload), 'Failed to create holiday date')
      ElMessage.success('Holiday date created')
    } else {
      extractResult(await updateHolidayCalendar(editingId.value, payload), 'Failed to update holiday date')
      ElMessage.success('Holiday date updated')
    }
    dialogVisible.value = false
    await loadData()
    await handlePreview()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to save')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  try {
    const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    extractResult(await updateHolidayCalendarStatus(row.id, nextStatus), 'Failed to update status')
    ElMessage.success(nextStatus === 'ENABLED' ? 'Record enabled' : 'Record disabled')
    await loadData()
    await handlePreview()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to update status')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`Delete ${row.businessDate}?`, 'Delete confirmation', { type: 'warning' })
    const result = await deleteHolidayCalendar(row.id)
    if (result.code !== 'SUCCESS') {
      throw new Error(result.message || 'Failed to delete holiday date')
    }
    ElMessage.success('Holiday date deleted')
    await loadData()
    await handlePreview()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to delete holiday date')
  }
}

function getWeekday(dateString) {
  const date = new Date(dateString)
  return ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][date.getDay()]
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