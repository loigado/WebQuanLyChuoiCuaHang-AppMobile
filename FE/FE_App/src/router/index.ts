import { createRouter, createWebHistory } from '@ionic/vue-router';
import { RouteRecordRaw } from 'vue-router';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/login' // Mặc định chuyển hướng về trang Đăng nhập
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginPage.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/HomePage.vue')
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('@/views/HistoryPage.vue')
  },
  {
    path: '/schedule',
    name: 'Schedule',
    component: () => import('@/views/SchedulePage.vue')
  },
  {
    path: '/leave',
    name: 'Leave',
    component: () => import('@/views/LeavePage.vue')
  },
  {
    path: '/stock-approval',
    name: 'StockApproval',
    component: () => import('@/views/StockApproval.vue')
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

export default router;