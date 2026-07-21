<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="brand">
        <h1>智宿酒店</h1>
        <p>后台管理</p>
      </div>

      <el-menu :default-active="$route.path" class="sidebar-menu" router unique-opened>
        <el-sub-menu index="dashboard-admin">
          <template #title>运营看板</template>
          <el-menu-item index="/admin/dashboard/overview">数据总览</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="hotel-ops">
          <template #title>酒店运营</template>
          <el-menu-item index="/admin/content/hotel-info">酒店信息管理</el-menu-item>
          <el-menu-item index="/admin/content/banners">首页轮播图管理</el-menu-item>
          <el-menu-item index="/admin/content/announcements">酒店公告管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="pricing-admin">
          <template #title>价格管理</template>
          <el-menu-item index="/admin/pricing/base-prices">基础价格管理</el-menu-item>
          <el-menu-item index="/admin/pricing/holiday-calendar">节假日日历管理</el-menu-item>
          <el-menu-item index="/admin/pricing/daily-prices">每日价格日历</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="room-admin">
          <template #title>房型与库存</template>
          <el-menu-item index="/admin/room-types">房型管理</el-menu-item>
          <el-menu-item index="/admin/inventory">库存管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="system-admin">
          <template #title>系统管理</template>
          <el-menu-item index="/admin/system-config">系统参数管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div>
          <h2>管理员控制台</h2>
          <p>管理酒店内容、价格策略、库存观察与系统基础配置。</p>
        </div>
      </header>
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
  background: linear-gradient(135deg, #fef9f3 0%, #f5efe6 30%, #ede4d8 100%);
}

.admin-sidebar {
  padding: 28px 20px;
  background: linear-gradient(180deg, #f5efe6 0%, #ede4d8 100%);
  color: #2c2416;
  border-right: 1px solid rgba(0,0,0,0.05);
}

.brand {
  margin-bottom: 28px;
}

.brand h1 {
  margin: 0 0 6px;
  font-size: 24px;
  color: #2c2416;
}

.brand p {
  margin: 0;
  color: #a69888;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.sidebar-menu :deep(.el-sub-menu__title),
.sidebar-menu :deep(.el-menu-item) {
  margin-bottom: 8px;
  border-radius: 12px;
  color: #5c5040;
}

.sidebar-menu :deep(.el-sub-menu__title:hover),
.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(200, 148, 92, 0.1);
  color: #2c2416;
}

.sidebar-menu :deep(.el-menu) {
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #c8945c, #b07840);
  color: #fff;
}

.admin-main {
  padding: 24px;
}

.admin-header {
  margin-bottom: 20px;
  padding: 20px 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(10px);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.admin-header h2 {
  margin: 0 0 6px;
  font-size: 24px;
  color: #111827;
}

.admin-header p {
  margin: 0;
  color: #6b7280;
}

.admin-content {
  min-width: 0;
}

@media (max-width: 900px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    padding-bottom: 12px;
  }

  .admin-main {
    padding: 16px;
  }
}
</style>