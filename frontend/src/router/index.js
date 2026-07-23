import { createRouter, createWebHistory } from 'vue-router'
import { refresh } from '@/api/auth'
import { getAccessToken, getCurrentUser, hasRole, setAuthSession } from '@/utils/request'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/modules/home/Home.vue') },
      { path: 'rooms', name: 'RoomSearch', component: () => import('@/views/user/roomtypes/RoomTypeListView.vue') },
      { path: 'rooms/:id', name: 'RoomDetail', component: () => import('@/views/user/roomtypes/RoomTypeDetailView.vue') },
      { path: 'booking/confirm', name: 'BookingConfirm', meta: { roles: ['USER'] }, component: () => import('@/views/user/bookings/BookingConfirmView.vue') },
      { path: 'orders', name: 'MyOrders', meta: { roles: ['USER'] }, component: () => import('@/views/user/bookings/MyOrdersView.vue') },
      { path: 'orders/:id', name: 'OrderDetail', meta: { roles: ['USER'] }, component: () => import('@/views/user/bookings/OrderDetailView.vue') },
      { path: 'orders/:id/pay', name: 'OrderPayment', meta: { roles: ['USER'] }, component: () => import('@/views/user/bookings/PaymentView.vue') },
      {
        path: 'account',
        meta: { requiresAuth: true },
        redirect: '/account/profile',
        children: [
          { path: 'profile', name: 'UserProfile', component: () => import('@/views/user/account/ProfileView.vue') },
          { path: 'password', name: 'UserPassword', component: () => import('@/views/user/account/PasswordView.vue') },
          { path: 'credit', name: 'UserCredit', component: () => import('@/views/user/account/CreditView.vue') }
        ]
      }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/modules/auth/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/modules/auth/Register.vue') },
  {
    path: '/frontdesk',
    component: () => import('@/layouts/FrontdeskLayout.vue'),
    meta: { roles: ['FRONT_DESK', 'ADMIN'] },
    children: [
      { path: '', name: 'FrontdeskHome', component: () => import('@/views/frontdesk/FrontdeskHomeView.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard/overview',
    meta: { roles: ['ADMIN'] },
    children: [
      { path: 'dashboard/overview', name: 'AdminDashboardOverview', component: () => import('@/views/admin/dashboard/DashboardOverviewView.vue') },
      { path: 'workspace', name: 'AdminWorkspace', component: () => import('@/views/admin/AdminWorkspaceView.vue') },
      { path: 'content/hotel-info', name: 'AdminHotelInfo', component: () => import('@/views/admin/content/HotelInfoView.vue') },
      { path: 'content/banners', name: 'AdminBanners', component: () => import('@/views/admin/content/BannerManageView.vue') },
      { path: 'content/announcements', name: 'AdminAnnouncements', component: () => import('@/views/admin/content/AnnouncementManageView.vue') },
      { path: 'pricing/base-prices', name: 'AdminBasePrices', component: () => import('@/views/admin/pricing/BasePriceView.vue') },
      { path: 'pricing/holiday-calendar', name: 'AdminHolidayCalendar', component: () => import('@/views/admin/pricing/HolidayCalendarView.vue') },
      { path: 'pricing/rules', name: 'AdminPriceRules', component: () => import('@/views/admin/pricing/PriceRuleView.vue') },
      { path: 'pricing/daily-prices', name: 'AdminDailyPrices', component: () => import('@/views/admin/pricing/DailyPriceCalendarView.vue') },
      { path: 'system-config', name: 'AdminSystemConfig', component: () => import('@/views/admin/systemconfig/SystemConfigView.vue') },
      { path: 'room-types', name: 'AdminRoomTypes', component: () => import('@/views/admin/roomtype/RoomTypeManageView.vue') },
      { path: 'inventory', name: 'AdminInventory', component: () => import('@/views/admin/inventory/InventoryManageView.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/user/UserManageView.vue') },
      { path: 'staff-roles', name: 'AdminStaffRoles', component: () => import('@/views/admin/user/StaffRoleManageView.vue') },
      { path: 'operation-logs', name: 'AdminOperationLogs', component: () => import('@/views/admin/audit/OperationLogView.vue') }
    ]
  },
  { path: '/403', name: 'Forbidden', component: () => import('@/components/403.vue') },
  { path: '/404', name: 'NotFound', component: () => import('@/components/404.vue') },
  { path: '/500', name: 'ServerError', component: () => import('@/components/500.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({ history: createWebHistory(), routes })
let refreshAttempted = false

function roleHome(user) {
  if (user?.roles?.includes('ADMIN')) return '/admin'
  if (user?.roles?.includes('FRONT_DESK')) return '/frontdesk'
  return '/'
}

router.beforeEach(async to => {
  const roles = to.matched.flatMap(record => record.meta.roles || [])
  const requiresAuth = roles.length > 0 || to.matched.some(record => record.meta.requiresAuth)
  if (requiresAuth && !getAccessToken() && !refreshAttempted) {
    refreshAttempted = true
    try {
      const result = await refresh()
      if (result.code === 'SUCCESS') setAuthSession(result.data)
    } catch { /* no active refresh session */ }
  }
  if (requiresAuth && !getAccessToken()) return { path: '/login', query: { redirect: to.fullPath } }
  if (roles.length && !roles.some(role => hasRole(role))) return '/403'
  if ((to.path === '/login' || to.path === '/register') && getAccessToken()) return roleHome(getCurrentUser())
  return true
})

export default router