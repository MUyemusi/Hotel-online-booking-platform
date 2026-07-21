<template>
  <div class="roomtype-manage-view">
    <div class="page-header">
      <div>
        <h1>房型管理</h1>
        <p>管理酒店房型的新增、编辑、上下架，以及图片和设施。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="fetchList">刷新</el-button>
        <el-button type="primary" @click="openCreate">新增房型</el-button>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="filter.keyword" placeholder="房型名称" clearable @clear="fetchList" @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable @change="fetchList">
            <el-option label="已上架" value="ONLINE" />
            <el-option label="已下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="房型名称" min-width="140" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="基础价格" width="120">
          <template #default="scope">¥{{ scope.row.basePrice }}</template>
        </el-table-column>
        <el-table-column prop="area" label="面积(m²)" width="90" />
        <el-table-column prop="bedType" label="床型" width="100" />
        <el-table-column prop="maxGuests" label="可住(人)" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ONLINE' ? 'success' : 'info'">
              {{ scope.row.status === 'ONLINE' ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="scope">
            <div class="table-actions">
              <el-button link type="primary" @click="openEdit(scope.row)">编辑</el-button>
              <el-button link type="warning" @click="openImages(scope.row)">图片</el-button>
              <el-button link type="info" @click="openFacilities(scope.row)">设施</el-button>
              <el-button
                v-if="scope.row.status !== 'ONLINE'"
                link type="success"
                @click="handleOnline(scope.row.id)"
              >上架</el-button>
              <el-button
                v-else
                link type="warning"
                @click="handleOffline(scope.row.id)"
              >下架</el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > filter.size">
        <el-pagination
          v-model:current-page="filter.page"
          :page-size="filter.size"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增房型' : '编辑房型'"
      width="640px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="房型名称" prop="name">
              <el-input v-model="form.name" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="如：豪华套房" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="基础价格" prop="basePrice">
              <el-input-number v-model="form.basePrice" :min="0" :precision="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="面积(m²)" prop="area">
              <el-input-number v-model="form.area" :min="1" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="可住人数" prop="maxGuests">
              <el-input-number v-model="form.maxGuests" :min="1" :max="10" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="床型" prop="bedType">
              <el-input v-model="form.bedType" placeholder="如：大床 1.8m" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="床数" prop="bedCount">
              <el-input-number v-model="form.bedCount" :min="1" :max="5" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="早餐">
              <el-switch v-model="form.hasBreakfast" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="窗户">
              <el-switch v-model="form.hasWindow" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="Wi-Fi">
              <el-switch v-model="form.hasWifi" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="空调">
              <el-switch v-model="form.hasAc" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="房型介绍" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ dialogMode === 'create' ? '创建' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 图片管理弹窗 -->
    <el-dialog v-model="imageDialogVisible" title="房型图片管理" width="720px" destroy-on-close @opened="loadImages">
      <div class="image-grid" v-if="imageList.length">
        <div v-for="img in imageList" :key="img.id" class="image-item">
          <el-image :src="img.imageUrl" fit="cover" class="img-thumb" :preview-src-list="[img.imageUrl]" preview-teleported />
          <div class="img-info">
            <el-tag size="small" :type="img.imageType === 'MAIN' ? 'warning' : 'info'">{{ img.imageType === 'MAIN' ? '主图' : '详情' }}</el-tag>
            <span>排序: {{ img.sortOrder }}</span>
          </div>
          <div class="img-actions">
            <el-button v-if="img.imageType !== 'MAIN'" link size="small" type="warning" @click="setMainImg(img.id)">设为主图</el-button>
            <el-button link size="small" type="danger" @click="deleteImg(img.id)">删除</el-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-hint">暂无图片，请添加</div>

      <div class="add-image-row">
        <el-input v-model="newImage.url" placeholder="图片URL" />
        <el-input-number v-model="newImage.sortOrder" :min="0" placeholder="排序" controls-position="right" />
        <el-button type="primary" :loading="addingImage" @click="addImage">添加图片</el-button>
      </div>
    </el-dialog>

    <!-- 设施管理弹窗 -->
    <el-dialog v-model="facilityDialogVisible" title="房型设施管理" width="520px" destroy-on-close @opened="loadFacilities">
      <div class="facility-list" v-if="facilityList.length">
        <div v-for="f in facilityList" :key="f.id" class="fac-row">
          <span class="fac-icon">{{ f.facilityIcon || '✦' }}</span>
          <span>{{ f.facilityName }}</span>
          <el-button link type="danger" @click="deleteFacility(f.id)">删除</el-button>
        </div>
      </div>
      <div v-else class="empty-hint">暂无设施</div>

      <div class="add-image-row" style="margin-top:16px;">
        <el-input v-model="newFacility.name" placeholder="设施名称" />
        <el-input v-model="newFacility.icon" placeholder="图标(emoji)" style="width:100px;" />
        <el-button type="primary" :loading="addingFacility" @click="addFacility">添加</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminRoomTypes, getAdminRoomTypeDetail,
  createRoomType, updateRoomType, deleteRoomType,
  onlineRoomType, offlineRoomType,
  getAdminRoomTypeImages, addRoomTypeImage, deleteRoomTypeImage, setMainImage,
  getAdminRoomTypeFacilities, addRoomTypeFacility, deleteRoomTypeFacility
} from '@/api/roomtype'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref()
const list = ref([])
const total = ref(0)
const editingId = ref(null)

const filter = reactive({ page: 1, size: 10, keyword: '', status: '' })

const defaultForm = () => ({
  name: '', category: '', basePrice: 0, area: 30, bedType: '大床 1.8m',
  bedCount: 1, maxGuests: 2, hasBreakfast: 1, hasWindow: 1, hasWifi: 1, hasAc: 1, description: ''
})

const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入房型名称', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基础价格', trigger: 'change' }]
}

function extractResult(res, msg) {
  if (res.code !== 'SUCCESS') throw new Error(res.message || msg)
  return res.data
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getAdminRoomTypes(filter)
    const data = extractResult(res, '加载失败')
    list.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.keyword = ''
  filter.status = ''
  filter.page = 1
  fetchList()
}

function openCreate() {
  dialogMode.value = 'create'
  Object.assign(form, defaultForm())
  editingId.value = null
  dialogVisible.value = true
}

function openEdit(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, defaultForm(), {
    name: row.name, category: row.category, basePrice: row.basePrice,
    area: row.area, bedType: row.bedType, bedCount: row.bedCount || 1,
    maxGuests: row.maxGuests, hasBreakfast: row.hasBreakfast ?? 1,
    hasWindow: row.hasWindow ?? 1, hasWifi: row.hasWifi ?? 1,
    hasAc: row.hasAc ?? 1, description: row.description || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await extractResult(await createRoomType({ ...form }), '创建失败')
      ElMessage.success('房型已创建')
    } else {
      await extractResult(await updateRoomType(editingId.value, { ...form }), '更新失败')
      ElMessage.success('房型已更新')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

async function handleOnline(id) {
  try {
    await extractResult(await onlineRoomType(id), '上架失败')
    ElMessage.success('已上架')
    fetchList()
  } catch (e) { ElMessage.error(e.message) }
}

async function handleOffline(id) {
  try {
    await extractResult(await offlineRoomType(id), '下架失败')
    ElMessage.success('已下架')
    fetchList()
  } catch (e) { ElMessage.error(e.message) }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除"${row.name}"吗？已产生订单的房型不可删除。`, '删除确认', { type: 'warning' })
    await extractResult(await deleteRoomType(row.id), '删除失败')
    ElMessage.success('已删除')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

// ===== 图片 =====
const imageDialogVisible = ref(false)
const imageRoomId = ref(null)
const imageList = ref([])
const addingImage = ref(false)
const newImage = reactive({ url: '', sortOrder: 0 })

function openImages(row) {
  imageRoomId.value = row.id
  imageDialogVisible.value = true
}

async function loadImages() {
  try {
    imageList.value = extractResult(await getAdminRoomTypeImages(imageRoomId.value), '加载图片失败') || []
  } catch (e) { ElMessage.error(e.message) }
}

async function addImage() {
  if (!newImage.url) { ElMessage.warning('请输入图片URL'); return }
  addingImage.value = true
  try {
    await extractResult(await addRoomTypeImage(imageRoomId.value, { imageUrl: newImage.url, imageType: 'DETAIL', sortOrder: newImage.sortOrder }), '添加失败')
    newImage.url = ''
    newImage.sortOrder = 0
    ElMessage.success('已添加')
    loadImages()
  } catch (e) { ElMessage.error(e.message) } finally { addingImage.value = false }
}

async function setMainImg(imageId) {
  try {
    await extractResult(await setMainImage(imageId), '设置失败')
    ElMessage.success('已设为主图')
    loadImages()
  } catch (e) { ElMessage.error(e.message) }
}

async function deleteImg(imageId) {
  try {
    await extractResult(await deleteRoomTypeImage(imageId), '删除失败')
    ElMessage.success('图片已删除')
    loadImages()
  } catch (e) { ElMessage.error(e.message) }
}

// ===== 设施 =====
const facilityDialogVisible = ref(false)
const facilityRoomId = ref(null)
const facilityList = ref([])
const addingFacility = ref(false)
const newFacility = reactive({ name: '', icon: '' })

function openFacilities(row) {
  facilityRoomId.value = row.id
  facilityDialogVisible.value = true
}

async function loadFacilities() {
  try {
    facilityList.value = extractResult(await getAdminRoomTypeFacilities(facilityRoomId.value), '加载设施失败') || []
  } catch (e) { ElMessage.error(e.message) }
}

async function addFacility() {
  if (!newFacility.name) { ElMessage.warning('请输入设施名称'); return }
  addingFacility.value = true
  try {
    await extractResult(await addRoomTypeFacility(facilityRoomId.value, { facilityName: newFacility.name, facilityIcon: newFacility.icon }), '添加失败')
    newFacility.name = ''
    newFacility.icon = ''
    ElMessage.success('已添加')
    loadFacilities()
  } catch (e) { ElMessage.error(e.message) } finally { addingFacility.value = false }
}

async function deleteFacility(facilityId) {
  try {
    await extractResult(await deleteRoomTypeFacility(facilityId), '删除失败')
    ElMessage.success('已删除')
    loadFacilities()
  } catch (e) { ElMessage.error(e.message) }
}

onMounted(() => fetchList())
</script>

<style scoped>
.roomtype-manage-view { display: flex; flex-direction: column; gap: 20px; }

.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.page-header h1 { margin: 0 0 8px; font-size: 28px; color: #1f2937; }
.page-header p { margin: 0; color: #6b7280; }
.header-actions, .table-actions { display: flex; gap: 4px; flex-wrap: wrap; }

.filter-card, .table-card { border-radius: 20px; }
.filter-form { display: flex; flex-wrap: wrap; gap: 8px; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }

.image-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 14px; margin-bottom: 20px; }
.image-item { border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden; padding: 8px; }
.img-thumb { width: 100%; height: 120px; border-radius: 8px; }
.img-info { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; font-size: 12px; }
.img-actions { display: flex; gap: 4px; margin-top: 4px; }

.add-image-row { display: flex; gap: 10px; align-items: center; }
.add-image-row > * { flex: 1; }

.facility-list { display: flex; flex-direction: column; gap: 8px; }
.fac-row { display: flex; align-items: center; gap: 10px; padding: 8px 12px; border: 1px solid #e5e7eb; border-radius: 8px; }
.fac-icon { font-size: 18px; }
.fac-row > span:nth-child(2) { flex: 1; }

.empty-hint { text-align: center; padding: 40px; color: #9ca3af; }

@media (max-width: 768px) {
  .page-header { flex-direction: column; }
}
</style>
