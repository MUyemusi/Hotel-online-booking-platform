
<template>
  <div class="system-config-view">
    <div class="page-header">
      <div>
        <h1>系统参数管理</h1>
        <p>统一维护预订、订单、信用、前台、库存、内容与价格相关的基础配置。</p>
      </div>
      <div class="header-actions">
        <el-select v-model="groupFilter" placeholder="全部分组" class="filter-select" @change="loadConfigs">
          <el-option label="全部" value="" />
          <el-option label="预订" value="BOOKING" />
          <el-option label="订单" value="ORDER" />
          <el-option label="信用" value="CREDIT" />
          <el-option label="前台" value="FRONTDESK" />
          <el-option label="库存" value="INVENTORY" />
          <el-option label="内容" value="CONTENT" />
          <el-option label="价格" value="PRICING" />
        </el-select>
        <el-button :loading="loading" @click="loadConfigs">重新加载</el-button>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="configs" v-loading="loading" stripe>
        <el-table-column prop="configName" label="参数名称" min-width="180" />
        <el-table-column prop="configKey" label="参数键" min-width="200" />
        <el-table-column label="参数值" min-width="140">
          <template #default="scope">
            <span>{{ scope.row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="参数类型" width="110"><template #default="{ row }">{{ displayValue('valueType', row.valueType) }}</template></el-table-column>
        <el-table-column label="参数分组" width="120"><template #default="{ row }">{{ displayValue('configGroup', row.configGroup) }}</template></el-table-column>
        <el-table-column prop="description" label="参数说明" min-width="240" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="修改系统参数" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="参数名称">
          <el-input :model-value="currentConfig?.configName || ''" disabled />
        </el-form-item>

        <el-form-item label="参数键">
          <el-input :model-value="currentConfig?.configKey || ''" disabled />
        </el-form-item>

        <el-form-item label="参数说明">
          <el-input :model-value="currentConfig?.description || ''" type="textarea" :rows="3" disabled />
        </el-form-item>

        <el-form-item label="新参数值" prop="configValue">
          <el-input-number
            v-if="currentConfig?.valueType === 'INTEGER'"
            v-model="form.integerValue"
            :min="0"
            :step="1"
            class="full-width"
          />
          <el-input-number
            v-else-if="currentConfig?.valueType === 'DECIMAL'"
            v-model="form.decimalValue"
            :min="0.01"
            :step="0.01"
            :precision="2"
            class="full-width"
          />
          <el-time-picker
            v-else-if="currentConfig?.valueType === 'TIME'"
            v-model="form.timeValue"
            value-format="HH:mm"
            format="HH:mm"
            placeholder="请选择时间"
            class="full-width"
          />
          <el-input
            v-else
            v-model="form.stringValue"
            maxlength="500"
            show-word-limit
          />
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
import { ElMessage } from 'element-plus'
import { displayValue } from '@/utils/adminDisplay'
import {
  getSystemConfig,
  getSystemConfigs,
  updateSystemConfig
} from '@/api/systemconfig/systemConfig'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const configs = ref([])
const currentConfig = ref(null)
const groupFilter = ref('')

const form = reactive({
  stringValue: '',
  integerValue: 0,
  decimalValue: 1,
  timeValue: '',
  configValue: ''
})

const rules = {
  configValue: [
    {
      validator: (_, __, callback) => {
        const value = buildConfigValue()
        if (value === '' || value == null) {
          callback(new Error('请输入参数值'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

function extractResult(result, fallbackMessage) {
  if (result.code !== 'SUCCESS') {
    throw new Error(result.message || fallbackMessage)
  }
  return result.data
}

function resetForm() {
  form.stringValue = ''
  form.integerValue = 0
  form.decimalValue = 1
  form.timeValue = ''
  form.configValue = ''
}

async function loadConfigs() {
  loading.value = true
  try {
    const result = await getSystemConfigs(groupFilter.value || undefined)
    configs.value = extractResult(result, '加载系统参数失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载系统参数失败')
  } finally {
    loading.value = false
  }
}

async function openEditDialog(row) {
  try {
    resetForm()
    const result = await getSystemConfig(row.configKey)
    currentConfig.value = extractResult(result, '加载参数详情失败')
    fillFormByType(currentConfig.value)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载参数详情失败')
  }
}

function fillFormByType(config) {
  const value = config.configValue
  if (config.valueType === 'INTEGER') {
    form.integerValue = Number(value)
  } else if (config.valueType === 'DECIMAL') {
    form.decimalValue = Number(value)
  } else if (config.valueType === 'TIME') {
    form.timeValue = value
  } else {
    form.stringValue = value
  }
}

function buildConfigValue() {
  if (!currentConfig.value) {
    return ''
  }
  if (currentConfig.value.valueType === 'INTEGER') {
    return String(form.integerValue)
  }
  if (currentConfig.value.valueType === 'DECIMAL') {
    return Number(form.decimalValue).toFixed(2)
  }
  if (currentConfig.value.valueType === 'TIME') {
    return form.timeValue
  }
  return form.stringValue?.trim() || ''
}

async function handleSubmit() {
  form.configValue = buildConfigValue()
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || !currentConfig.value) {
    return
  }

  submitting.value = true
  try {
    const result = await updateSystemConfig(currentConfig.value.configKey, {
      configValue: form.configValue
    })
    extractResult(result, '保存系统参数失败')
    ElMessage.success('系统参数保存成功')
    dialogVisible.value = false
    await loadConfigs()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '保存系统参数失败')
  } finally {
    submitting.value = false
  }
}

loadConfigs()
</script>

<style scoped>
.system-config-view {
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
.dialog-actions {
  display: flex;
  gap: 12px;
}

.filter-select {
  width: 180px;
}

.table-card {
  border-radius: 20px;
}

.full-width {
  width: 100%;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .filter-select {
    width: 100%;
  }
}
</style>
