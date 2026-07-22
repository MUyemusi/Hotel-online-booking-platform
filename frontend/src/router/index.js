import { createRouter, createWebHistory } from 'vue-router'
import { refresh } from '@/api/auth'
import { getAccessToken, getCurrentUser, hasRole, setAuthSession } from '@/utils/request'
import { defaultRouteForCurrentUser, getToken, hasRole } from '@/utils/auth'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/modules/home/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('@/modules/auth/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/modules/auth/Register.vue') },
  { path: '/rooms', name: 'RoomSearch', component: () => import('@/views/user/RoomSearchView.vue') },
  { path: '/rooms/:id', name: 'RoomDetail', component: () => import('@/views/user/RoomDetailView.vue') },
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { roles: ['USER'] },
    children: [
      { path: '/booking/confirm', name: 'BookingConfirm', component: () => import('@/views/user/BookingConfirmView.vue') },
      { path: '/orders', name: 'MyOrders', component: () => import('@/views/user/OrderListView.vue') },
      { path: '/orders/:id', name: 'OrderDetail', component: () => import('@/views/user/OrderDetailView.vue') },
      { path: '/orders/:id/pay', name: 'OrderPayment', component: () => import('@/views/user/PaymentView.vue') }
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/modules/home/Home.vue') },
      { path: 'rooms', name: 'RoomTypeList', component: () => import('@/views/user/roomtypes/RoomTypeListView.vue') },
      { path: 'rooms/:id', name: 'RoomTypeDetail', component: () => import('@/views/user/roomtypes/RoomTypeDetailView.vue') },
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
    meta: { roles: ['FRONT_DESK', 'ADMIN'] }
    meta: { requiresAuth: true, roles: ['FRONT_DESK', 'FRONTDESK', 'ADMIN'] },
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
      { path: 'content/hotel-info', name: 'AdminHotelInfo', component: () => import('@/views/admin/content/HotelInfoView.vue') },
      { path: 'content/banners', name: 'AdminBanners', component: () => import('@/views/admin/content/BannerManageView.vue') },
      { path: 'content/announcements', name: 'AdminAnnouncements', component: () => import('@/views/admin/content/AnnouncementManageView.vue') },
      { path: 'pricing/base-prices', name: 'AdminBasePrices', component: () => import('@/views/admin/pricing/BasePriceView.vue') },
      { path: 'pricing/holiday-calendar', name: 'AdminHolidayCalendar', component: () => import('@/views/admin/pricing/HolidayCalendarView.vue') },
      { path: 'pricing/rules', name: 'AdminPriceRules', component: () => import('@/views/admin/pricing/PriceRuleView.vue') },
      { path: 'pricing/daily-prices', name: 'AdminDailyPrices', component: () => import('@/views/admin/pricing/DailyPriceCalendarView.vue') },
      { path: 'system-config', name: 'AdminSystemConfig', component: () => import('@/views/admin/systemconfig/SystemConfigView.vue') }
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: '', name: 'AdminWorkspace', component: () => import('@/views/admin/AdminWorkspaceView.vue') }
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
  return '/rooms'
}

router.beforeEach(async to => {
  const roles = to.meta.roles || []
  if (roles.length && !getAccessToken() && !refreshAttempted) {
    refreshAttempted = true
    try {
      const result = await refresh()
      if (result.code === 'SUCCESS') setAuthSession(result.data)
    } catch { /* no active refresh session */ }
  }
  if (roles.length && !getAccessToken()) return { path: '/login', query: { redirect: to.fullPath } }
  if (roles.length && !roles.some(role => hasRole(role))) return '/403'
  if ((to.path === '/login' || to.path === '/register') && getAccessToken()) return roleHome(getCurrentUser())
  return true
})
export default router

router.beforeEach(to => {
  const token = getToken()
  if ((to.path === '/login' || to.path === '/register') && token) return defaultRouteForCurrentUser()
  if (to.matched.some(record => record.meta.requiresAuth) && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const roles = to.matched.flatMap(record => record.meta.roles || [])
  if (roles.length && !roles.some(role => hasRole(role))) return '/403'
  return true
})

export default router
