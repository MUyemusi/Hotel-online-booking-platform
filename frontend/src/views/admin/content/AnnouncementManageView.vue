<template>
  <div class="announcement-manage-view">
    <div class="page-header">
      <div>
        <h1>酒店公告管理</h1>
        <p>集中维护酒店公告草稿、发布状态和前台展示顺序。</p>
      </div>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="全部状态" class="filter-select">
          <el-option label="全部" value="ALL" />
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已下架" value="OFFLINE" />
        </el-select>
        <el-button :loading="loading" @click="loadAnnouncements">重新加载</el-button>
        <el-button type="primary" @click="openCreateDialog">新增公告</el-button>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="filteredAnnouncements" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="240" />

        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ statusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="置顶" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isTop ? 'danger' : 'info'">
              {{ scope.row.isTop ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.publishTime) }}
          </template>
        </el-table-column>

        <el-table-column label="更新时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.updatedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button
                v-if="scope.row.status !== 'PUBLISHED'"
                link
                type="success"
                @click="handlePublish(scope.row)"
              >{{ scope.row.status === 'OFFLINE' ? '重新发布' : '发布' }}</el-button>
              <el-button
                v-if="scope.row.status === 'PUBLISHED'"
                link
                type="warning"
                @click="handleOffline(scope.row)"
              >下架</el-button>
              <el-button
                v-if="scope.row.status !== 'PUBLISHED'"
                link
                type="danger"
                @click="handleDelete(scope.row)"
              >删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增公告' : '编辑公告'"
      width="760px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" maxlength="150" show-word-limit />
        </el-form-item>

        <el-form-item label="公告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>

        <el-form-item label="是否置顶" prop="top">
          <el-switch v-model="form.top" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ dialogMode === 'create' ? '保存草稿' : '保存修改' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAnnouncement,
  deleteAnnouncement,
  getAdminAnnouncements,
  offlineAnnouncement,
  publishAnnouncement,
  updateAnnouncement
} from '@/api/content/announcement'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref()
const announcements = ref([])
const editingId = ref(null)
const statusFilter = ref('ALL')

const createDefaultForm = () => ({
  title: '',
  content: '',
  top: false
})

const form = reactive(createDefaultForm())

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 150, message: '公告标题不能超过150个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ],
  top: [
    { required: true, message: '请选择是否置顶', trigger: 'change' }
  ]
}

const filteredAnnouncements = computed(() => {
  if (statusFilter.value === 'ALL') {
    return announcements.value
  }
  return announcements.value.filter(item => item.status === statusFilter.value)
})

function resetForm() {
  Object.assign(form, createDefaultForm())
  editingId.value = null
}

function fillForm(data) {
  Object.assign(form, createDefaultForm(), {
    title: data.title ?? '',
    content: data.content ?? '',
    top: Boolean(data.isTop)
  })
}

function extractResult(result, fallbackMessage) {
  if (result.code !== 'SUCCESS') {
    throw new Error(result.message || fallbackMessage)
  }
  return result.data
}

async function loadAnnouncements() {
  loading.value = true
  try {
    const result = await getAdminAnnouncements()
    announcements.value = extractResult(result, '加载公告失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载公告失败')
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
      await extractResult(await createAnnouncement({ ...form }), '创建公告失败')
      ElMessage.success('公告草稿已保存')
    } else {
      await extractResult(await updateAnnouncement(editingId.value, { ...form }), '更新公告失败')
      ElMessage.success('公告更新成功')
    }
    dialogVisible.value = false
    await loadAnnouncements()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

async function handlePublish(row) {
  try {
    await extractResult(await publishAnnouncement(row.id), '发布公告失败')
    ElMessage.success('公告已发布')
    await loadAnnouncements()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '发布公告失败')
  }
}

async function handleOffline(row) {
  try {
    await extractResult(await offlineAnnouncement(row.id), '下架公告失败')
    ElMessage.success('公告已下架')
    await loadAnnouncements()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '下架公告失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除公告“${row.title}”吗？`, '删除确认', {
      type: 'warning'
    })
    const result = await deleteAnnouncement(row.id)
    if (result.code !== 'SUCCESS') {
      throw new Error(result.message || '删除公告失败')
    }
    ElMessage.success('公告已删除')
    await loadAnnouncements()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    ElMessage.error(error?.response?.data?.message || error?.message || '删除公告失败')
  }
}

function statusLabel(status) {
  if (status === 'PUBLISHED') return '已发布'
  if (status === 'OFFLINE') return '已下架'
  return '草稿'
}

function statusTagType(status) {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'OFFLINE') return 'warning'
  return 'info'
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcement-manage-view {
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

.filter-select {
  width: 140px;
}

.table-card {
  border-radius: 20px;
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
