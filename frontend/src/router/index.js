import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/modules/home/Home.vue')
      },
      {
        path: 'rooms',
        name: 'RoomTypeList',
        component: () => import('@/views/user/roomtypes/RoomTypeListView.vue')
      },
      {
        path: 'rooms/:id',
        name: 'RoomTypeDetail',
        component: () => import('@/views/user/roomtypes/RoomTypeDetailView.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/modules/auth/Login.vue')
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard/overview',
    children: [
      {
        path: 'dashboard/overview',
        name: 'AdminDashboardOverview',
        component: () => import('@/views/admin/dashboard/DashboardOverviewView.vue')
      },
      {
        path: 'content/hotel-info',
        name: 'AdminHotelInfo',
        component: () => import('@/views/admin/content/HotelInfoView.vue')
      },
      {
        path: 'content/banners',
        name: 'AdminBanners',
        component: () => import('@/views/admin/content/BannerManageView.vue')
      },
      {
        path: 'content/announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/content/AnnouncementManageView.vue')
      },
      {
        path: 'pricing/base-prices',
        name: 'AdminBasePrices',
        component: () => import('@/views/admin/pricing/BasePriceView.vue')
      },
      {
        path: 'pricing/holiday-calendar',
        name: 'AdminHolidayCalendar',
        component: () => import('@/views/admin/pricing/HolidayCalendarView.vue')
      },
      {
        path: 'pricing/daily-prices',
        name: 'AdminDailyPrices',
        component: () => import('@/views/admin/pricing/DailyPriceCalendarView.vue')
      },
      {
        path: 'room-types',
        name: 'AdminRoomTypes',
        component: () => import('@/views/admin/roomtype/RoomTypeManageView.vue')
      },
      {
        path: 'inventory',
        name: 'AdminInventory',
        component: () => import('@/views/admin/inventory/InventoryManageView.vue')
      },
      {
        path: 'system-config',
        name: 'AdminSystemConfig',
        component: () => import('@/views/admin/systemconfig/SystemConfigView.vue')
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/components/403.vue')
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/components/404.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
