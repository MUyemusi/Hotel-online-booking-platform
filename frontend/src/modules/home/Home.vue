<template>
  <div class="home">
    <h1>智宿酒店在线预订与运营管理系统</h1>
    <el-card class="health-card">
      <template #header>
        <div class="card-header">
          <span>后端连接状态</span>
        </div>
      </template>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="healthStatus" class="status success">
        <el-icon :size="40" color="#67c23a"><CircleCheck /></el-icon>
        <p>{{ healthStatus }}</p>
      </div>
      <div v-else class="status error">
        <el-icon :size="40" color="#f56c6c"><CircleClose /></el-icon>
        <p>后端连接失败</p>
        <el-button @click="checkHealth" type="primary">重试</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { checkHealth } from '@/api/health'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'

const loading = ref(true)
const healthStatus = ref('')

const checkHealth = async () => {
  loading.value = true
  try {
    const response = await checkHealth()
    if (response.code === 'SUCCESS') {
      healthStatus.value = '后端连接成功: ' + response.data
    } else {
      healthStatus.value = ''
    }
  } catch (error) {
    healthStatus.value = ''
    ElMessage.error('连接后端失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkHealth()
})
</script>

<style scoped>
.home {
  padding: 20px;
  text-align: center;
}

.health-card {
  max-width: 500px;
  margin: 20px auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status {
  padding: 20px;
}

.loading {
  padding: 20px;
}
</style>
