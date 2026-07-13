import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/modules/home/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/modules/auth/Login.vue')
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
