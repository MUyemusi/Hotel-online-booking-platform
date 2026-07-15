<template>
  <div class="hotel-info-view">
    <div class="page-header">
      <div>
        <h1>酒店信息管理</h1>
        <p>维护酒店展示信息，保存后前台与后台读取到的内容会同步更新。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="loadHotelInfo">重新加载</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </div>

    <el-card shadow="never" class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="hotel-form"
      >
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item label="酒店名称" prop="hotelName">
              <el-input v-model="form.hotelName" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="Logo地址" prop="logoUrl">
              <el-input v-model="form.logoUrl" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="酒店介绍" prop="introduction">
          <el-input v-model="form.introduction" type="textarea" :rows="4" />
        </el-form-item>

        <el-form-item label="酒店地址" prop="address">
          <el-input v-model="form.address" maxlength="255" show-word-limit />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" maxlength="30" show-word-limit />
        </el-form-item>

        <el-form-item label="酒店设施" prop="facilities">
          <el-input v-model="form.facilities" type="textarea" :rows="4" />
        </el-form-item>

        <el-form-item label="入住须知" prop="checkinNotice">
          <el-input v-model="form.checkinNotice" type="textarea" :rows="4" />
        </el-form-item>

        <el-form-item label="取消规则" prop="cancellationPolicy">
          <el-input v-model="form.cancellationPolicy" type="textarea" :rows="4" />
        </el-form-item>

        <el-form-item label="交通信息" prop="trafficInfo">
          <el-input v-model="form.trafficInfo" type="textarea" :rows="4" />
        </el-form-item>

        <el-form-item label="常见问题" prop="faqContent">
          <el-input v-model="form.faqContent" type="textarea" :rows="6" />
        </el-form-item>

        <div class="footer-actions">
          <el-button :loading="loading" @click="loadHotelInfo">重新加载</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminHotelInfo, updateHotelInfo } from '@/api/content/hotelInfo'

const formRef = ref()
const loading = ref(false)
const saving = ref(false)

const createDefaultForm = () => ({
  hotelName: '',
  logoUrl: '',
  introduction: '',
  address: '',
  contactPhone: '',
  facilities: '',
  checkinNotice: '',
  cancellationPolicy: '',
  trafficInfo: '',
  faqContent: ''
})

const form = reactive(createDefaultForm())

const rules = {
  hotelName: [
    { required: true, message: '请输入酒店名称', trigger: 'blur' },
    { max: 100, message: '酒店名称不能超过100个字符', trigger: 'blur' }
  ],
  logoUrl: [
    { max: 500, message: 'Logo地址不能超过500个字符', trigger: 'blur' }
  ],
  address: [
    { max: 255, message: '酒店地址不能超过255个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { max: 30, message: '联系电话不能超过30个字符', trigger: 'blur' }
  ]
}

function patchForm(data = {}) {
  Object.assign(form, createDefaultForm(), {
    hotelName: data.hotelName ?? '',
    logoUrl: data.logoUrl ?? '',
    introduction: data.introduction ?? '',
    address: data.address ?? '',
    contactPhone: data.contactPhone ?? '',
    facilities: data.facilities ?? '',
    checkinNotice: data.checkinNotice ?? '',
    cancellationPolicy: data.cancellationPolicy ?? '',
    trafficInfo: data.trafficInfo ?? '',
    faqContent: data.faqContent ?? ''
  })
}

async function loadHotelInfo() {
  loading.value = true
  try {
    const result = await getAdminHotelInfo()
    if (result.code !== 'SUCCESS') {
      throw new Error(result.message || '加载酒店信息失败')
    }
    patchForm(result.data)
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载酒店信息失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const result = await updateHotelInfo({ ...form })
    if (result.code !== 'SUCCESS') {
      throw new Error(result.message || '保存酒店信息失败')
    }
    patchForm(result.data)
    ElMessage.success('酒店信息保存成功')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '保存酒店信息失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadHotelInfo()
})
</script>

<style scoped>
.hotel-info-view {
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
.footer-actions {
  display: flex;
  gap: 12px;
}

.form-card {
  border-radius: 20px;
}

.hotel-form {
  width: 100%;
}

.footer-actions {
  justify-content: flex-end;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .header-actions,
  .footer-actions {
    width: 100%;
  }

  .header-actions :deep(.el-button),
  .footer-actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
