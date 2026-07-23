<template>
  <div class="frontdesk-workbench">
    <div class="page-header">
      <div>
        <h1>前台工作台</h1>
        <p>办理入住、退房及清洁管理</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="goToRoomMap">查看楼层房态</el-button>
        <div class="header-date">{{ todayText }}</div>
      </div>
    </div>

    <el-card shadow="never" class="workbench-card">
      <el-tabs v-model="activeTab">
        <!-- 入住办理 -->
        <el-tab-pane label="入住办理" name="checkin">
          <div class="checkin-flow">
            <el-steps :active="checkinStep" finish-status="success" align-center class="flow-steps">
              <el-step title="查询订单" />
              <el-step title="资格核验" />
              <el-step title="二维码核验" />
              <el-step title="选择房间" />
              <el-step title="确认入住" />
            </el-steps>

            <!-- 步骤1：查询订单 -->
            <div v-if="checkinStep === 0" class="step-content">
              <el-card shadow="never" class="search-card">
                <el-form :inline="true" :model="searchForm">
                  <el-form-item label="订单号">
                    <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
                  </el-form-item>
                  <el-form-item label="手机号">
                    <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="searchOrders" :loading="searchLoading">
                      查询
                    </el-button>
                    <el-button @click="resetSearch">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-card>

              <el-table
                :data="searchResults"
                v-loading="searchLoading"
                stripe
                style="margin-top: 16px"
              >
                <el-table-column prop="orderNo" label="订单号" width="160" />
                <el-table-column prop="contactName" label="联系人" width="100" />
                <el-table-column prop="maskedContactPhone" label="手机号" width="130" />
                <el-table-column prop="roomTypeName" label="房型" width="140" />
                <el-table-column prop="checkInDate" label="入住日期" width="120" />
                <el-table-column prop="checkOutDate" label="离店日期" width="120" />
                <el-table-column prop="roomCount" label="房间数" width="90" />
                <el-table-column label="订单状态" width="100">
                  <template #default="scope">
                    <el-tag :type="orderStatusType(scope.row.orderStatus)" size="small">
                      {{ scope.row.orderStatusText }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="支付状态" width="100">
                  <template #default="scope">
                    <el-tag :type="paymentStatusType(scope.row.paymentStatus)" size="small">
                      {{ scope.row.paymentStatusText }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="150">
                  <template #default="scope">
                    <el-button
                      type="primary"
                      link
                      @click="selectOrder(scope.row)"
                      :disabled="scope.row.orderStatus !== 'PENDING_CHECK_IN' || scope.row.paymentStatus !== 'PAID'"
                    >
                      办理入住
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 步骤2：资格核验 -->
            <div v-else-if="checkinStep === 1" class="step-content">
              <el-card shadow="never" class="verification-card">
                <template #header>
                  <div class="card-header">
                    <span>入住资格核验</span>
                    <el-button link @click="backToSearch">返回查询</el-button>
                  </div>
                </template>
                <el-descriptions :column="2" border v-if="selectedOrder">
                  <el-descriptions-item label="订单号">{{ selectedOrder.orderNo }}</el-descriptions-item>
                  <el-descriptions-item label="联系人">{{ selectedOrder.contactName }}</el-descriptions-item>
                  <el-descriptions-item label="房型">{{ selectedOrder.roomTypeName }}</el-descriptions-item>
                  <el-descriptions-item label="房间数">{{ selectedOrder.roomCount }}间</el-descriptions-item>
                  <el-descriptions-item label="入住日期">{{ selectedOrder.checkInDate }}</el-descriptions-item>
                  <el-descriptions-item label="离店日期">{{ selectedOrder.checkOutDate }}</el-descriptions-item>
                </el-descriptions>

                <div class="check-items" v-if="verificationResult">
                  <div class="check-item" v-for="(item, index) in checkItems" :key="index">
                    <div class="check-icon">
                      <el-icon v-if="item.pass" color="#10b981"><CircleCheck /></el-icon>
                      <el-icon v-else color="#ef4444"><CircleClose /></el-icon>
                    </div>
                    <div class="check-info">
                      <div class="check-name">{{ item.name }}</div>
                      <div class="check-desc" :class="{ fail: !item.pass }">
                        {{ item.desc }}
                      </div>
                    </div>
                  </div>
                </div>

                <div class="verification-result" v-if="verificationResult">
                  <el-alert
                    :type="verificationResult.canCheckIn ? 'success' : 'error'"
                    :title="verificationResult.canCheckIn ? '资格核验通过，可以办理入住' : translateVerificationMessage(verificationResult.failureMessage)"
                    show-icon
                  />
                </div>

                <div class="step-actions">
                  <el-button @click="backToSearch">返回</el-button>
                  <el-button
                    type="primary"
                    @click="goToQrVerification"
                    :disabled="!verificationResult?.canCheckIn"
                  >
                    下一步：二维码核验
                  </el-button>
                </div>
              </el-card>
            </div>

            <!-- 步骤3：二维码核验 -->
            <div v-else-if="checkinStep === 2" class="step-content">
              <el-card shadow="never" class="qr-card">
                <template #header>
                  <div class="card-header">
                    <span>二维码核验</span>
                    <el-button link @click="goToVerification">返回</el-button>
                  </div>
                </template>
                <div class="qr-section">
                  <div class="qr-placeholder">
                    <el-icon><Document /></el-icon>
                    <p>扫码区域（演示使用）</p>
                  </div>
                  <el-form :model="qrForm" label-width="100px">
                    <el-form-item label="核验内容">
                      <el-input
                        v-model="qrForm.content"
                        type="textarea"
                        :rows="3"
                        placeholder="请输入扫码内容、唯一核验码，或直接粘贴 token"
                      ></el-input>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="verifyQr" :loading="qrVerifying">
                        核验
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
                <div v-if="qrVerified" class="qr-result">
                  <el-alert type="success" title="二维码核验成功" show-icon />
                  <el-descriptions :column="1" border style="margin-top: 16px">
                    <el-descriptions-item label="订单号">{{ selectedOrder.orderNo }}</el-descriptions-item>
                    <el-descriptions-item label="联系人">{{ selectedOrder.contactName }}</el-descriptions-item>
                    <el-descriptions-item label="房型">{{ selectedOrder.roomTypeName }}</el-descriptions-item>
                    <el-descriptions-item label="房间数">{{ selectedOrder.roomCount }}间</el-descriptions-item>
                  </el-descriptions>
                </div>
                <div class="step-actions">
                  <el-button @click="goToVerification">返回</el-button>
                  <el-button type="primary" @click="verifyQrAndNext" :loading="qrVerifying" :disabled="!qrForm.content">
                    下一步：选择房间
                  </el-button>
                </div>
              </el-card>
            </div>

            <!-- 步骤4：选择房间 -->
            <div v-else-if="checkinStep === 3" class="step-content">
              <el-card shadow="never" class="room-select-card">
                <template #header>
                  <div class="card-header">
                    <span>选择房间</span>
                    <el-button link @click="goToQrVerification">返回</el-button>
                  </div>
                </template>
                <div class="selection-info">
                  <el-tag type="info">已选 {{ selectedRooms.length }} / {{ selectedOrder?.roomCount }} 间</el-tag>
                </div>
                <div class="available-rooms">
                  <div class="rooms-title">可用房间（{{ availableRooms.length }}间）</div>
                  <div class="rooms-grid">
                    <div
                      v-for="room in availableRooms"
                      :key="room.id"
                      class="room-option"
                      :class="{ selected: selectedRooms.includes(room.id) }"
                      @click="toggleRoomSelection(room.id)"
                    >
                      <div class="room-no">{{ room.roomNo }}</div>
                      <div class="room-type">{{ room.roomTypeName }}</div>
                    </div>
                  </div>
                </div>
                <div class="selected-rooms" v-if="selectedRooms.length > 0">
                  <div class="rooms-title">已选房间</div>
                  <el-tag
                    v-for="id in selectedRooms"
                    :key="id"
                    closable
                    @close="toggleRoomSelection(id)"
                    style="margin-right: 8px"
                  >
                    {{ getRoomById(id)?.roomNo }}
                  </el-tag>
                </div>
                <div class="step-actions">
                  <el-button @click="goToQrVerification">返回</el-button>
                  <el-button
                    type="primary"
                    @click="goToConfirmCheckin"
                    :disabled="selectedRooms.length !== selectedOrder?.roomCount"
                  >
                    下一步：确认入住
                  </el-button>
                </div>
              </el-card>
            </div>

            <!-- 步骤5：确认入住 -->
            <div v-else-if="checkinStep === 4" class="step-content">
              <el-card shadow="never" class="confirm-card">
                <template #header>
                  <div class="card-header">
                    <span>确认入住</span>
                    <el-button link @click="goToRoomSelect">返回</el-button>
                  </div>
                </template>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="订单号">{{ selectedOrder.orderNo }}</el-descriptions-item>
                  <el-descriptions-item label="联系人">{{ selectedOrder.contactName }}</el-descriptions-item>
                  <el-descriptions-item label="房型">{{ selectedOrder.roomTypeName }}</el-descriptions-item>
                  <el-descriptions-item label="入住日期">{{ selectedOrder.checkInDate }}</el-descriptions-item>
                  <el-descriptions-item label="离店日期">{{ selectedOrder.checkOutDate }}</el-descriptions-item>
                  <el-descriptions-item label="分配房间">
                    <el-tag v-for="id in selectedRooms" :key="id" style="margin-right: 8px">
                      {{ getRoomById(id)?.roomNo }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
                <el-alert
                  type="warning"
                  title="提示：确认入住后，订单将变为已入住状态，房间状态将变为在住"
                  style="margin-top: 16px"
                  show-icon
                />
                <div class="step-actions">
                  <el-button @click="goToRoomSelect">返回</el-button>
                  <el-button type="primary" @click="confirmCheckin" :loading="checkinLoading">
                    确认入住
                  </el-button>
                </div>
              </el-card>
            </div>

            <!-- 入住成功 -->
            <div v-else-if="checkinStep === 5" class="step-content">
              <el-result icon="success" title="入住办理成功">
                <template #sub-title>
                  <div>订单 {{ selectedOrder?.orderNo }} 已成功办理入住</div>
                  <div>分配房间：{{ selectedRoomNames.join('、') }}</div>
                </template>
                <template #extra>
                  <el-button type="primary" @click="resetCheckinFlow">返回前台工作台</el-button>
                </template>
              </el-result>
            </div>
          </div>
        </el-tab-pane>

        <!-- 退房办理 -->
        <el-tab-pane label="退房办理" name="checkout">
          <div class="checkout-section">
            <el-card shadow="never" class="search-card">
              <el-form :inline="true" :model="checkoutSearchForm">
                <el-form-item label="订单号">
                  <el-input v-model="checkoutSearchForm.orderNo" placeholder="请输入订单号" clearable />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="checkoutSearchForm.phone" placeholder="请输入手机号" clearable />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="searchCheckoutOrders" :loading="checkoutSearchLoading">
                    查询
                  </el-button>
                  <el-button @click="resetCheckoutSearch">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>

            <el-table
              :data="checkoutResults"
              v-loading="checkoutSearchLoading"
              stripe
              style="margin-top: 16px"
            >
              <el-table-column prop="orderNo" label="订单号" width="160" />
              <el-table-column prop="contactName" label="联系人" width="100" />
              <el-table-column prop="maskedContactPhone" label="手机号" width="130" />
              <el-table-column prop="roomTypeName" label="房型" width="140" />
              <el-table-column prop="checkInDate" label="入住日期" width="120" />
              <el-table-column prop="checkOutDate" label="计划离店" width="120" />
              <el-table-column label="订单状态" width="100">
                <template #default="scope">
                  <el-tag :type="orderStatusType(scope.row.orderStatus)" size="small">
                    {{ scope.row.orderStatusText }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" fixed="right" width="150">
                <template #default="scope">
                  <el-button
                    type="primary"
                    link
                    @click="openCheckoutConfirm(scope.row)"
                    :disabled="scope.row.orderStatus !== 'CHECKED_IN'"
                  >
                    办理退房
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 清洁管理 -->
        <el-tab-pane v-if="canManageCleaning" label="清洁管理" name="cleaning">
          <div class="cleaning-section">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>待清洁房间</span>
                  <el-button type="primary" @click="loadDirtyRooms">
                    <el-icon><Refresh /></el-icon>
                    刷新
                  </el-button>
                </div>
              </template>
              <el-table :data="dirtyRooms" stripe>
                <el-table-column prop="roomNo" label="房间号" width="120" />
                <el-table-column prop="floor" label="楼层" width="90">
                  <template #default="scope">{{ scope.row.floor }}楼</template>
                </el-table-column>
                <el-table-column prop="roomTypeName" label="房型" width="140" />
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag type="warning">待清洁</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="240">
                  <template #default="scope">
                    <el-button type="success" link @click="openCleaningComplete(scope.row)">
                      清洁完成
                    </el-button>
                    <el-button v-if="isAdmin" type="danger" link @click="openCleaningIssue(scope.row)">
                      发现问题
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="dirtyRooms.length === 0" class="empty-state">
                暂无待清洁房间
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 退房确认对话框 -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="确认退房"
      width="520px"
    >
      <div v-if="checkoutOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ checkoutOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ checkoutOrder.contactName }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ checkoutOrder.roomTypeName }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ checkoutOrder.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="计划离店">{{ checkoutOrder.checkOutDate }}</el-descriptions-item>
        </el-descriptions>
        <el-alert
          type="warning"
          title="提示：确认退房后，订单将变为已完成，房间将转为待清洁状态"
          style="margin-top: 16px"
          show-icon
        />
      </div>
      <template #footer>
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckout" :loading="checkoutLoading">
          确认退房
        </el-button>
      </template>
    </el-dialog>

    <!-- 清洁完成对话框 -->
    <el-dialog
      v-model="cleaningCompleteVisible"
      title="清洁完成"
      width="420px"
    >
      <el-form :model="cleaningCompleteForm" label-width="80px">
        <el-form-item label="房间号">
          <span>{{ cleaningRoom?.roomNo }}</span>
        </el-form-item>
        <el-form-item label="清洁备注">
          <el-input
            v-model="cleaningCompleteForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入清洁备注（可选）"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cleaningCompleteVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCleaningComplete" :loading="cleaningSubmitting">
          确认完成
        </el-button>
      </template>
    </el-dialog>

    <!-- 发现问题对话框 -->
    <el-dialog
      v-model="cleaningIssueVisible"
      title="发现问题转维修"
      width="420px"
    >
      <el-form :model="cleaningIssueForm" label-width="80px">
        <el-form-item label="房间号">
          <span>{{ cleaningRoom?.roomNo }}</span>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input
            v-model="cleaningIssueForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请描述发现的问题"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cleaningIssueVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCleaningIssue" :loading="cleaningSubmitting">
          确认上报
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCurrentUser } from '@/utils/request'
import {
  CircleCheck,
  CircleClose,
  Document,
  Refresh
} from '@element-plus/icons-vue'
import {
  searchFrontdeskOrders,
  getFrontdeskOrderDetail,
  verifyCheckin as verifyCheckinApi
} from '@/api/frontdesk/orders'
import {
  verifyCheckinQr,
  confirmCheckin as confirmCheckinApi,
  confirmCheckout as confirmCheckoutApi
} from '@/api/frontdesk/checkin'
import {
  listPendingCleaningRooms,
  completeCleaning as completeCleaningApi,
  reportCleaningIssue as reportCleaningIssueApi
} from '@/api/room/cleaning'

const router = useRouter()
const currentUser = getCurrentUser() || {}
const isAdmin = computed(() => currentUser?.roles?.includes('ADMIN'))
const isFrontdesk = computed(() => currentUser?.roles?.includes('FRONT_DESK') || currentUser?.roles?.includes('FRONTDESK'))
const canManageCleaning = computed(() => isAdmin.value || isFrontdesk.value)

const activeTab = ref('checkin')
const todayText = ref('')

// 入住流程
const checkinStep = ref(0)
const searchLoading = ref(false)
const qrVerifying = ref(false)
const checkinLoading = ref(false)

const searchForm = ref({
  orderNo: '',
  phone: ''
})
const searchResults = ref([])
const selectedOrder = ref(null)
const verificationResult = ref(null)
const qrVerificationResult = ref(null)
const qrForm = ref({
  content: ''
})
const qrVerified = ref(false)
const availableRooms = ref([])
const selectedRooms = ref([])

// 退房流程
const checkoutSearchLoading = ref(false)
const checkoutLoading = ref(false)
const checkoutSearchForm = ref({
  orderNo: '',
  phone: ''
})
const checkoutResults = ref([])
const checkoutDialogVisible = ref(false)
const checkoutOrder = ref(null)

// 清洁管理
const dirtyRooms = ref([])
const cleaningCompleteVisible = ref(false)
const cleaningIssueVisible = ref(false)
const cleaningSubmitting = ref(false)
const cleaningRoom = ref(null)
const cleaningCompleteForm = ref({ remark: '' })
const cleaningIssueForm = ref({ reason: '' })

const verificationItemNameMap = {
  PAYMENT_STATUS: '支付状态',
  ORDER_STATUS: '订单状态',
  CHECKIN_DATE: '入住日期',
  ROOM_TYPE: '房型有效性',
  ROOM_COUNT: '房间数量',
  ROOM_ASSIGNMENT: '房间分配',
  ROOM_AVAILABILITY: '可分配房间'
}

const verificationMessageMap = {
  'Order is paid': '订单已支付',
  'Order is not paid': '订单未支付',
  'Order is pending check-in': '订单处于待入住状态',
  'Order is still pending payment': '订单仍处于待支付状态',
  'Order has already checked in': '订单已办理入住',
  'Order has completed': '订单已完成',
  'Order has been cancelled': '订单已取消',
  'Order has been closed': '订单已关闭',
  'Order is not pending check-in': '订单不是待入住状态',
  'Current business date matches check-in date': '当前营业日期符合入住日期',
  'Order check-in or check-out date is missing': '订单入住或离店日期缺失',
  'Check-in date has not arrived': '入住日期尚未到达',
  'Order room type is valid': '订单房型有效',
  'Order room type is invalid': '订单房型无效',
  'Order room count is valid': '订单房间数量有效',
  'Order room count is invalid': '订单房间数量无效',
  'Order has no active room assignment': '订单暂无有效房间分配',
  'Order has completed room assignment': '订单已完成房间分配'
}

const translateVerificationMessage = (message = '') => {
  if (verificationMessageMap[message]) return verificationMessageMap[message]
  const partialAssignment = message.match(/^Order has partial room assignment, remaining rooms: (\d+)$/)
  if (partialAssignment) return `订单存在部分房间分配，剩余需分配房间数：${partialAssignment[1]}`
  const availability = message.match(/^Required rooms: (\d+), allocatable rooms: (\d+)$/)
  if (availability) return `需分配房间数：${availability[1]}，可分配房间数：${availability[2]}`
  return message
}

// 检查项
const checkItems = computed(() => {
  return (verificationResult.value?.checkItems || []).map(item => ({
    name: verificationItemNameMap[item.item] || item.item,
    pass: item.passed,
    desc: translateVerificationMessage(item.message)
  }))
})

// 已选房间名称
const selectedRoomNames = computed(() => {
  return selectedRooms.value.map(id => getRoomById(id)?.roomNo).filter(Boolean)
})

const formatDate = (date) => {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }).format(date)
}

const orderStatusType = (status) => {
  const map = {
    'PENDING_CHECK_IN': 'success',
    'PENDING_PAYMENT': 'warning',
    'CHECKED_IN': 'primary',
    'COMPLETED': 'info',
    'CANCELLED': 'info'
  }
  return map[status] || 'info'
}

const paymentStatusType = (status) => {
  return status === 'PAID' ? 'success' : 'warning'
}

const extractErrorMessage = (error, fallback = '操作失败') => {
  return error?.response?.data?.message || error?.message || fallback
}

const createRequestToken = (prefix) => {
  if (window.crypto?.randomUUID) {
    return `${prefix}_${window.crypto.randomUUID().replace(/-/g, '')}`
  }
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
}

const normalizeRoom = (room) => ({
  ...room,
  id: room.id ?? room.roomId,
  roomId: room.roomId ?? room.id
})

const normalizeRooms = (rooms = []) => rooms.map(normalizeRoom)

const getRoomById = (id) => {
  return availableRooms.value.find(r => r.id === id)
}

const loadDirtyRooms = async () => {
  if (!canManageCleaning.value) {
    dirtyRooms.value = []
    return
  }
  try {
    const response = await listPendingCleaningRooms({ page: 1, size: 100 })
    dirtyRooms.value = response.data?.records || []
  } catch (error) {
    dirtyRooms.value = []
    ElMessage.error(extractErrorMessage(error, '待清洁列表加载失败'))
  }
}

const goToRoomMap = () => {
  router.push('/frontdesk/room-map')
}

const searchOrders = async () => {
  searchLoading.value = true
  try {
    const response = await searchFrontdeskOrders({
      orderNo: searchForm.value.orderNo || undefined,
      phone: searchForm.value.phone || undefined,
      page: 1,
      size: 20
    })
    searchResults.value = response.data?.records || []
  } catch (error) {
    searchResults.value = []
    ElMessage.error(extractErrorMessage(error, '订单查询失败'))
  } finally {
    searchLoading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = { orderNo: '', phone: '' }
  searchOrders()
}

const selectOrder = async (order) => {
  searchLoading.value = true
  try {
    const [detailResponse, verificationResponse] = await Promise.all([
      getFrontdeskOrderDetail(order.orderId),
      verifyCheckinApi(order.orderId)
    ])
    selectedOrder.value = detailResponse.data
    verificationResult.value = verificationResponse.data
    qrVerificationResult.value = null
    checkinStep.value = 1
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '入住资格核验失败'))
  } finally {
    searchLoading.value = false
  }
}

const backToSearch = () => {
  resetCheckinFlow()
}

const goToQrVerification = () => {
  qrForm.value = { content: '' }
  qrVerified.value = false
  qrVerificationResult.value = null
  checkinStep.value = 2
}

const goToVerification = () => {
  checkinStep.value = 1
}

const verifyQr = async () => {
  if (!qrForm.value.content) {
    ElMessage.warning('请输入核验内容')
    return
  }
  qrVerifying.value = true
  try {
    const response = await verifyCheckinQr({ credential: qrForm.value.content })
    const result = response.data
    if (!result?.valid) {
      qrVerified.value = false
      ElMessage.error(result?.failureMessage || '二维码核验失败')
      return
    }
    if (selectedOrder.value?.orderId && result.orderId !== selectedOrder.value.orderId) {
      qrVerified.value = false
      ElMessage.error('二维码不属于当前订单')
      return
    }
    qrVerificationResult.value = result
    qrVerified.value = true
    ElMessage.success('二维码核验成功')
  } catch (error) {
    qrVerified.value = false
    ElMessage.error(extractErrorMessage(error, '二维码核验失败'))
  } finally {
    qrVerifying.value = false
  }
}

const goToRoomSelect = () => {
  const source = qrVerificationResult.value || verificationResult.value
  availableRooms.value = normalizeRooms(source?.allocatableRooms || [])
  selectedRooms.value = []
  checkinStep.value = 3
}

const verifyQrAndNext = async () => {
  if (qrVerified.value) {
    goToRoomSelect()
    return
  }
  await verifyQr()
  if (qrVerified.value) goToRoomSelect()
}

const toggleRoomSelection = (roomId) => {
  const index = selectedRooms.value.indexOf(roomId)
  if (index > -1) {
    selectedRooms.value.splice(index, 1)
  } else if (selectedRooms.value.length < selectedOrder.value.roomCount) {
    selectedRooms.value.push(roomId)
  }
}

const goToConfirmCheckin = () => {
  checkinStep.value = 4
}

const confirmCheckin = async () => {
  checkinLoading.value = true
  try {
    await confirmCheckinApi(selectedOrder.value.orderId, {
      qrContent: qrForm.value.content,
      roomIds: selectedRooms.value,
      requestToken: createRequestToken('CHECKIN'),
      remark: '前台办理入住'
    })
    const followUpTasks = [searchCheckoutOrders()]
    if (canManageCleaning.value) {
      followUpTasks.push(loadDirtyRooms())
    }
    await Promise.all(followUpTasks)
    checkinStep.value = 5
    ElMessage.success('入住办理成功')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '确认入住失败'))
  } finally {
    checkinLoading.value = false
  }
}

const resetCheckinFlow = () => {
  checkinStep.value = 0
  selectedOrder.value = null
  verificationResult.value = null
  qrVerificationResult.value = null
  qrForm.value = { content: '' }
  qrVerified.value = false
  availableRooms.value = []
  selectedRooms.value = []
}

// 退房
const searchCheckoutOrders = async () => {
  checkoutSearchLoading.value = true
  try {
    const response = await searchFrontdeskOrders({
      orderNo: checkoutSearchForm.value.orderNo || undefined,
      phone: checkoutSearchForm.value.phone || undefined,
      orderStatus: 'CHECKED_IN',
      page: 1,
      size: 20
    })
    checkoutResults.value = response.data?.records || []
  } catch (error) {
    checkoutResults.value = []
    ElMessage.error(extractErrorMessage(error, '退房订单查询失败'))
  } finally {
    checkoutSearchLoading.value = false
  }
}

const resetCheckoutSearch = () => {
  checkoutSearchForm.value = { orderNo: '', phone: '' }
  checkoutResults.value = []
}

const openCheckoutConfirm = (order) => {
  checkoutOrder.value = order
  checkoutDialogVisible.value = true
}

const confirmCheckout = async () => {
  checkoutLoading.value = true
  try {
    await confirmCheckoutApi(checkoutOrder.value.orderId, {
      requestToken: createRequestToken('CHECKOUT'),
      remark: '前台办理退房'
    })
    ElMessage.success('退房办理成功')
    checkoutDialogVisible.value = false
    const followUpTasks = [searchCheckoutOrders()]
    if (canManageCleaning.value) {
      followUpTasks.push(loadDirtyRooms())
    }
    await Promise.all(followUpTasks)
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '确认退房失败'))
  } finally {
    checkoutLoading.value = false
  }
}

// 清洁
const openCleaningComplete = (room) => {
  cleaningRoom.value = room
  cleaningCompleteForm.value = { remark: '' }
  cleaningCompleteVisible.value = true
}

const openCleaningIssue = (room) => {
  cleaningRoom.value = room
  cleaningIssueForm.value = { reason: '' }
  cleaningIssueVisible.value = true
}

const submitCleaningComplete = async () => {
  cleaningSubmitting.value = true
  try {
    await completeCleaningApi(cleaningRoom.value.roomId || cleaningRoom.value.id, {
      requestToken: createRequestToken('CLEAN'),
      remark: cleaningCompleteForm.value.remark
    })
    ElMessage.success('清洁完成，房间已恢复为空闲状态')
    cleaningCompleteVisible.value = false
    await loadDirtyRooms()
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '清洁完成提交失败'))
  } finally {
    cleaningSubmitting.value = false
  }
}

const submitCleaningIssue = async () => {
  if (!cleaningIssueForm.value.reason) {
    ElMessage.warning('请描述发现的问题')
    return
  }
  cleaningSubmitting.value = true
  try {
    await reportCleaningIssueApi(cleaningRoom.value.roomId || cleaningRoom.value.id, {
      requestToken: createRequestToken('ISSUE'),
      reason: cleaningIssueForm.value.reason,
      remark: '清洁异常转维修'
    })
    ElMessage.success('已上报问题，房间转为维修状态')
    cleaningIssueVisible.value = false
    await loadDirtyRooms()
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '上报清洁问题失败'))
  } finally {
    cleaningSubmitting.value = false
  }
}

onMounted(async () => {
  todayText.value = formatDate(new Date())
  const tasks = [searchOrders(), searchCheckoutOrders()]
  if (canManageCleaning.value) tasks.push(loadDirtyRooms())
  await Promise.all(tasks)
})
</script>

<style scoped>
.frontdesk-workbench {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header h1 {
  margin: 0 0 6px;
  font-size: 28px;
  font-family: 'Noto Serif SC', serif;
  color: #374151;
}

.page-header p {
  margin: 0;
  color: #78716c;
}

.header-date {
  padding: 10px 20px;
  border-radius: 30px;
  background: #f3f4f6;
  font-size: 14px;
  color: #6b7280;
}

.workbench-card {
  border-radius: 18px;
}

.flow-steps {
  margin-bottom: 32px;
}

.step-content {
  min-height: 400px;
}

.search-card,
.verification-card,
.qr-card,
.room-select-card,
.confirm-card {
  border-radius: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.check-items {
  margin: 24px 0;
}

.check-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.check-icon {
  font-size: 24px;
}

.check-name {
  font-weight: 500;
  color: #374151;
}

.check-desc {
  font-size: 13px;
  color: #6b7280;
}

.check-desc.fail {
  color: #ef4444;
}

.verification-result {
  margin: 16px 0;
}

.qr-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  align-items: start;
}

.qr-placeholder {
  padding: 60px 20px;
  text-align: center;
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  background: #f9fafb;
}

.qr-placeholder .el-icon {
  font-size: 48px;
  color: #9ca3af;
}

.qr-placeholder p {
  margin: 12px 0 0;
  color: #9ca3af;
}

.qr-result {
  margin-top: 20px;
}

.selection-info {
  margin-bottom: 16px;
}

.available-rooms,
.selected-rooms {
  margin: 16px 0;
}

.rooms-title {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 12px;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.room-option {
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  text-align: center;
  background: #fff;
  transition: all 0.2s;
}

.room-option:hover {
  border-color: #60a5fa;
  background: #eff6ff;
}

.room-option.selected {
  border-color: #3b82f6;
  background: #dbeafe;
}

.room-no {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.room-type {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
