<template>
  <div class="banner-manage-view">
    <div class="page-header">
      <div>
        <h1>首页轮播图管理</h1>
        <p>统一管理首页轮播图的新增、排序、发布、下架与删除。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="loadBanners">重新加载</el-button>
        <el-button type="primary" @click="openCreateDialog">新增轮播图</el-button>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="banners" v-loading="loading" stripe>
        <el-table-column label="图片" width="140">
          <template #default="scope">
            <el-image
              v-if="scope.row.imageUrl"
              :src="scope.row.imageUrl"
              fit="cover"
              class="banner-thumb"
              :preview-src-list="[scope.row.imageUrl]"
              preview-teleported
            />
            <span v-else>暂无图片</span>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="180" />

        <el-table-column label="跳转地址" min-width="220">
          <template #default="scope">
            <span class="ellipsis">{{ scope.row.linkUrl || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="sortNo" label="排序" width="90" />

        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'PUBLISHED' ? 'success' : 'info'">
              {{ scope.row.status === 'PUBLISHED' ? '已发布' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="更新时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.updatedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button
                v-if="scope.row.status !== 'PUBLISHED'"
                link
                type="success"
                @click="handlePublish(scope.row)"
              >发布</el-button>
              <el-button
                v-else
                link
                type="warning"
                @click="handleOffline(scope.row)"
              >下架</el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增轮播图' : '编辑轮播图'"
      width="680px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="轮播图标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" maxlength="500" show-word-limit placeholder="请输入可访问的图片URL" />
          <el-upload
            :show-file-list="false"
            accept="image/jpeg,image/png,image/gif,image/webp"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
          >
            <el-button :loading="uploading">上传图片</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="跳转地址" prop="linkUrl">
          <el-input v-model="form.linkUrl" maxlength="500" show-word-limit placeholder="可选，留空表示不跳转" />
        </el-form-item>

        <el-form-item label="排序序号" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" :step="1" />
        </el-form-item>

        <el-form-item label="图片预览">
          <div class="preview-box">
            <el-image
              v-if="form.imageUrl"
              :src="form.imageUrl"
              fit="cover"
              class="preview-image"
              :preview-src-list="[form.imageUrl]"
              preview-teleported
            />
            <div v-else class="preview-placeholder">输入图片地址后可在这里预览</div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ dialogMode === 'create' ? '创建' : '保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadImage } from '@/api/content/upload'
import {
  createBanner,
  deleteBanner,
  getAdminBanners,
  offlineBanner,
  publishBanner,
  updateBanner
} from '@/api/content/banner'

const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref()
const banners = ref([])
const editingId = ref(null)

const createDefaultForm = () => ({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sortNo: 0
})

const form = reactive(createDefaultForm())
const maxImageUploadMb = 5

const rules = {
  title: [
    { required: true, message: '请输入轮播图标题', trigger: 'blur' },
    { max: 100, message: '标题不能超过100个字符', trigger: 'blur' }
  ],
  imageUrl: [
    { required: true, message: '请输入图片地址', trigger: 'blur' },
    { max: 500, message: '图片地址不能超过500个字符', trigger: 'blur' }
  ],
  linkUrl: [
    { max: 500, message: '跳转地址不能超过500个字符', trigger: 'blur' }
  ],
  sortNo: [
    { required: true, message: '请输入排序序号', trigger: 'change' },
    { type: 'number', min: 0, message: '排序序号不能小于0', trigger: 'change' }
  ]
}


function beforeImageUpload(file) {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WEBP 图片')
    return false
  }
  if (file.size > maxImageUploadMb * 1024 * 1024) {
    ElMessage.error(`图片不能超过 ${maxImageUploadMb}MB`)
    return false
  }
  return true
}

async function handleImageUpload(options) {
  uploading.value = true
  try {
    const result = await uploadImage(options.file)
    if (result.code !== 'SUCCESS') throw new Error(result.message || '上传失败')
    form.imageUrl = result.data.url
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '图片上传失败')
  } finally {
    uploading.value = false
  }
}
function resetForm() {
  Object.assign(form, createDefaultForm())
  editingId.value = null
}

function fillForm(data) {
  Object.assign(form, createDefaultForm(), {
    title: data.title ?? '',
    imageUrl: data.imageUrl ?? '',
    linkUrl: data.linkUrl ?? '',
    sortNo: data.sortNo ?? 0
  })
}

function extractResult(result, fallbackMessage) {
  if (result.code !== 'SUCCESS') {
    throw new Error(result.message || fallbackMessage)
  }
  return result.data
}

async function loadBanners() {
  loading.value = true
  try {
    const result = await getAdminBanners()
    banners.value = extractResult(result, '加载轮播图失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载轮播图失败')
  } finally {
    loading.value = false
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
  fillForm(row)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await extractResult(await createBanner({ ...form }), '创建轮播图失败')
      ElMessage.success('轮播图创建成功')
    } else {
      await extractResult(await updateBanner(editingId.value, { ...form }), '更新轮播图失败')
      ElMessage.success('轮播图更新成功')
    }
    dialogVisible.value = false
    await loadBanners()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

async function handlePublish(row) {
  try {
    await extractResult(await publishBanner(row.id), '发布轮播图失败')
    ElMessage.success('轮播图已发布')
    await loadBanners()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '发布轮播图失败')
  }
}

async function handleOffline(row) {
  try {
    await extractResult(await offlineBanner(row.id), '下架轮播图失败')
    ElMessage.success('轮播图已下架')
    await loadBanners()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '下架轮播图失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除轮播图“${row.title}”吗？`, '删除确认', {
      type: 'warning'
    })
    const result = await deleteBanner(row.id)
    if (result.code !== 'SUCCESS') {
      throw new Error(result.message || '删除轮播图失败')
    }
    ElMessage.success('轮播图已删除')
    await loadBanners()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    ElMessage.error(error?.response?.data?.message || error?.message || '删除轮播图失败')
  }
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

onMounted(() => {
  loadBanners()
})
</script>

<style scoped>
.banner-manage-view {
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
.table-actions {
  display: flex;
  gap: 12px;
}

.table-card {
  border-radius: 20px;
}

.banner-thumb {
  width: 100px;
  height: 56px;
  border-radius: 10px;
  background: #f3f4f6;
}

.preview-box {
  min-height: 180px;
  border: 1px dashed #d1d5db;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  max-height: 240px;
}

.preview-placeholder {
  color: #9ca3af;
}

.ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
