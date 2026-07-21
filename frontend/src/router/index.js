import { createRouter, createWebHistory } from 'vue-router'
import { defaultRouteForCurrentUser, getToken, hasRole } from '@/utils/auth'

const routes = [
  {
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
    meta: { requiresAuth: true, roles: ['FRONT_DESK', 'FRONTDESK', 'ADMIN'] },
    children: [
      { path: '', name: 'FrontdeskHome', component: () => import('@/views/frontdesk/FrontdeskHomeView.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
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
