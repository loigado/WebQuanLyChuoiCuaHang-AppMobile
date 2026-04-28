<template>
  <div class="enterprise-layout">
    <!-- Sidebar hiện đại -->
    <aside class="side-nav" :class="{ 'nav-collapsed': isCollapsed }">
      <div class="nav-header">
        <div class="logo-wrapper">
          <div class="logo-box">B</div>
          <span v-if="!isCollapsed" class="brand-name">BRANCH<span class="text-primary">CORE</span></span>
        </div>
        <button @click="isCollapsed = !isCollapsed" class="collapse-btn">
          {{ isCollapsed ? '→' : '←' }}
        </button>
      </div>

      <div class="nav-content menu-scroll">
        <div class="nav-section">
          <router-link to="/" class="nav-item">
            <i class="nav-icon">📊</i>
            <span v-if="!isCollapsed">Dashboard Tổng quan</span>
          </router-link>
        </div>

        <!-- Nhóm Admin -->
        <div v-if="auth.role?.includes('ADMIN')" class="nav-section">
          <p v-if="!isCollapsed" class="section-title">Quản trị hệ thống</p>
          <router-link to="/admin/system-analytics" class="nav-item">
            <i class="nav-icon">📈</i>
            <span v-if="!isCollapsed">Phân tích hệ thống</span>
          </router-link>
          <router-link to="/admin/users" class="nav-item">
            <i class="nav-icon">👥</i>
            <span v-if="!isCollapsed">Quản lý nhân sự</span>
          </router-link>
          <router-link to="/admin/branches" class="nav-item">
            <i class="nav-icon">🏢</i>
            <span v-if="!isCollapsed">Mạng lưới chi nhánh</span>
          </router-link>
          <router-link to="/admin/announcements" class="nav-item">
            <i class="nav-icon">📢</i>
            <span v-if="!isCollapsed">Thông báo nội bộ</span>
          </router-link>
          <router-link to="/admin/inventory-management" class="nav-item">
            <i class="nav-icon">📦</i>
            <span v-if="!isCollapsed">Điều phối kho tổng</span>
          </router-link>
          <router-link to="/admin/approve-inventory" class="nav-item">
            <i class="nav-icon">⚖️</i>
            <span v-if="!isCollapsed">Phê duyệt phiếu kho</span>
          </router-link>
          <router-link to="/admin/approval-history" class="nav-item">
            <i class="nav-icon">📜</i>
            <span v-if="!isCollapsed">Lịch sử duyệt kho</span>
          </router-link>
          <router-link to="/admin/cameras" class="nav-item">
            <i class="nav-icon">📹</i>
            <span v-if="!isCollapsed">Camera hệ thống</span>
          </router-link>
          <router-link to="/admin/config" class="nav-item">
            <i class="nav-icon">⚙️</i>
            <span v-if="!isCollapsed">Cấu hình hệ thống</span>
          </router-link>
          <router-link to="/admin/audit-logs" class="nav-item">
            <i class="nav-icon">🕵️‍♂️</i>
            <span v-if="!isCollapsed">Nhật ký hệ thống</span>
          </router-link>
        </div>

        <!-- Nhóm Manager -->
        <div v-if="auth.role?.includes('MANAGER')" class="nav-section">
          <p v-if="!isCollapsed" class="section-title">Vận hành chi nhánh</p>
          <router-link to="/manager/employees" class="nav-item">
            <i class="nav-icon">🧑‍🤝‍🧑</i>
            <span v-if="!isCollapsed">Nhân sự chi nhánh</span>
          </router-link>
          <router-link to="/manager/shifts" class="nav-item">
            <i class="nav-icon">🕒</i>
            <span v-if="!isCollapsed">Ca làm việc</span>
          </router-link>
          <router-link to="/manager/attendance" class="nav-item">
            <i class="nav-icon">📅</i>
            <span v-if="!isCollapsed">Quản lý chấm công</span>
          </router-link>
          <router-link to="/manager/attendance-report" class="nav-item">
            <i class="nav-icon">📊</i>
            <span v-if="!isCollapsed">Báo cáo công & lương</span>
          </router-link>
          <router-link to="/manager/leave-requests" class="nav-item">
            <i class="nav-icon">📩</i>
            <span v-if="!isCollapsed">Quản lý nghỉ phép</span>
          </router-link>
          <router-link to="/manager/categories" class="nav-item">
            <i class="nav-icon">📂</i>
            <span v-if="!isCollapsed">Danh mục sản phẩm</span>
          </router-link>
          <router-link to="/manager/products" class="nav-item">
            <i class="nav-icon">📦</i>
            <span v-if="!isCollapsed">Quản lý sản phẩm</span>
          </router-link>
          <router-link to="/manager/inventory-detail" class="nav-item">
            <i class="nav-icon">🔍</i>
            <span v-if="!isCollapsed">Kiểm soát tồn kho</span>
          </router-link>
          <router-link to="/manager/stocktake" class="nav-item">
            <i class="nav-icon">📋</i>
            <span v-if="!isCollapsed">Kiểm kê kho định kỳ</span>
          </router-link>
          <router-link to="/manager/inventory-history" class="nav-item">
            <i class="nav-icon">⏳</i>
            <span v-if="!isCollapsed">Lịch sử & Điều chuyển</span>
          </router-link>
          <router-link to="/manager/stock-requests" class="nav-item">
            <i class="nav-icon">📤</i>
            <span v-if="!isCollapsed">Giao dịch kho bãi</span>
          </router-link>
          <router-link to="/manager/reports" class="nav-item">
            <i class="nav-icon">📈</i>
            <span v-if="!isCollapsed">Báo cáo chi nhánh</span>
          </router-link>
        </div>

        <!-- Nhóm Accountant -->
        <div v-if="auth.role?.includes('ACCOUNTANT')" class="nav-section">
          <p v-if="!isCollapsed" class="section-title">Kiểm soát tài chính</p>
          <router-link to="/accountant/inventory-data" class="nav-item">
            <i class="nav-icon">📦</i>
            <span v-if="!isCollapsed">Dữ liệu kho hệ thống</span>
          </router-link>
          <router-link to="/accountant/financial-reports" class="nav-item">
            <i class="nav-icon">💰</i>
            <span v-if="!isCollapsed">Báo cáo tài chính</span>
          </router-link>
          <router-link to="/accountant/attendance-reports" class="nav-item">
            <i class="nav-icon">🕒</i>
            <span v-if="!isCollapsed">Báo cáo chấm công tổng</span>
          </router-link>
        </div>

        <!-- Nhóm Employee -->
        <div v-if="auth.role?.includes('EMPLOYEE')" class="nav-section">
          <p v-if="!isCollapsed" class="section-title">Cá nhân</p>
          <router-link to="/employee/attendance" class="nav-item">
            <i class="nav-icon">📍</i>
            <span v-if="!isCollapsed">Chấm công cá nhân</span>
          </router-link>
          <router-link to="/employee/my-shifts" class="nav-item">
            <i class="nav-icon">🕒</i>
            <span v-if="!isCollapsed">Lịch làm việc</span>
          </router-link>
          <router-link to="/employee/leave-requests" class="nav-item">
            <i class="nav-icon">📩</i>
            <span v-if="!isCollapsed">Đơn xin nghỉ phép</span>
          </router-link>
        </div>
      </div>

      <!-- User Profile Footer -->
      <div class="nav-footer">
        <div class="user-info" v-if="!isCollapsed">
          <div class="avatar">{{ auth.user?.username?.charAt(0).toUpperCase() }}</div>
          <div class="meta">
            <span class="username">{{ auth.user?.fullName || auth.user?.username }}</span>
            <span class="role">{{ auth.role?.replace('ROLE_', '') }}</span>
          </div>
        </div>
        <button @click="auth.logout" class="logout-btn" :title="'Đăng xuất'">
          🚪 <span v-if="!isCollapsed" class="ml-2">Đăng xuất</span>
        </button>
      </div>
    </aside>

    <!-- Main Content Area -->
    <main class="main-container">
      <header class="top-bar">
        <div class="page-info">
          <h1 class="current-page-title">{{ $route.meta.title || 'Hệ thống quản trị' }}</h1>
          <nav class="breadcrumb">BranchCore / {{ $route.meta.title }}</nav>
        </div>

        <div class="top-bar-actions">
          <!-- Notification Bell -->
          <el-dropdown trigger="click" class="notification-dropdown" @command="handleNotificationClick">
            <div class="bell-wrapper">
              <el-badge :value="unreadNotifications.length" :hidden="unreadNotifications.length === 0" class="item">
                <i class="bell-icon">🔔</i>
              </el-badge>
            </div>
            <template #footer>
                <div class="p-2 text-center border-t border-[#F1F5F9]">
                    <span class="text-[12px] text-primary cursor-pointer hover:underline font-medium">Xem tất cả thông báo</span>
                </div>
            </template>
            <template #dropdown>
              <el-dropdown-menu class="notification-menu">
                <div class="notification-header">
                  <span class="title">Thông báo mới</span>
                  <span class="count">{{ unreadNotifications.length }} chưa đọc</span>
                </div>
                <div v-if="unreadNotifications.length === 0" class="empty-notif">
                   🎉 Tuyệt vời! Không có thông báo mới.
                </div>
                <el-dropdown-item v-for="n in unreadNotifications" :key="n.id" :command="n" class="notif-item">
                  <div class="notif-content">
                    <div class="notif-title">{{ n.title }}</div>
                    <div class="notif-message">{{ n.message }}</div>
                    <div class="notif-time">{{ formatTime(n.createdAt) }}</div>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <section class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { ElNotification, ElMessage } from 'element-plus';
import SockJS from 'sockjs-client/dist/sockjs';
import { Client } from '@stomp/stompjs';
import api from '@/api/axios';

const auth = useAuthStore();
const router = useRouter();
const isCollapsed = ref(false);
const stompClient = ref(null);
const unreadNotifications = ref([]);

const fetchNotifications = async () => {
  if (!auth.user?.userId) return;
  try {
    const res = await api.get(`/notifications/my-alerts/${auth.user.userId}`);
    unreadNotifications.value = res.data?.data || [];
  } catch (e) { console.error("Lỗi tải thông báo"); }
};

const handleNotificationClick = async (notif) => {
  try {
    await api.post(`/notifications/${notif.id}/read`);
    unreadNotifications.value = unreadNotifications.value.filter(n => n.id !== notif.id);
    if (notif.link) router.push(notif.link);
  } catch (e) { ElMessage.error("Lỗi xử lý thông báo"); }
};

const formatTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
};

// WebSocket real-time notifications logic
const connectWebSocket = () => {
  if (!auth.token) return;
  const socket = new SockJS('http://localhost:8081/ws-notifications');
  stompClient.value = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      // Subscribe to user-specific notifications
      if (auth.user?.userId) {
        stompClient.value.subscribe(`/user/${auth.user.userId}/queue/notifications`, (msg) => {
          const notif = JSON.parse(msg.body);
          unreadNotifications.value.unshift(notif);
          ElNotification({ 
            title: notif.title || 'Thông báo mới', 
            message: notif.message, 
            type: 'info', 
            position: 'bottom-right' 
          });
        });
      }

      if (auth.role && (auth.role.includes('ADMIN') || auth.role.includes('ACCOUNTANT'))) {
        stompClient.value.subscribe('/topic/admin-notifications', (msg) => {
          ElNotification({ title: 'Yêu cầu mới', message: msg.body, type: 'warning', position: 'bottom-right' });
          fetchNotifications();
        });
      }
    }
  });
  stompClient.value.activate();
};

onMounted(() => {
  connectWebSocket();
  fetchNotifications();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

.enterprise-layout {
  display: flex;
  height: 100vh;
  background-color: #F8FAFC;
  font-family: 'Inter', sans-serif;
  color: #1E293B;
  overflow: hidden;
}

/* SIDEBAR STYLES */
.side-nav {
  width: 280px;
  background-color: #FFFFFF;
  border-right: 1px solid #E2E8F0;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.02);
}

.nav-collapsed {
  width: 80px;
}

.nav-header {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #F1F5F9;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-box {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #0064E0 0%, #0046B5 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 800;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0, 100, 224, 0.25);
}

.brand-name {
  font-weight: 700;
  font-size: 18px;
  letter-spacing: -0.5px;
  color: #0F172A;
}

.text-primary { color: #0064E0; }

.collapse-btn {
  background: #F1F5F9;
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-content {
  flex: 1;
  padding: 24px 12px;
  overflow-y: auto;
}

.nav-section { margin-bottom: 32px; }

.section-title {
  padding: 0 12px;
  font-size: 11px;
  font-weight: 700;
  color: #94A3B8;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 4px;
  border-radius: 12px;
  color: #475569;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s;
  gap: 12px;
}

.nav-item:hover {
  background-color: #F1F5F9;
  color: #0F172A;
}

.router-link-active {
  background-color: #E0E7FF !important;
  color: #0064E0 !important;
}

.nav-icon { font-size: 18px; font-style: normal; }

/* FOOTER & PROFILE */
.nav-footer {
  padding: 20px;
  border-top: 1px solid #F1F5F9;
  background-color: #FAFBFC;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.avatar {
  width: 36px;
  height: 36px;
  background-color: #0064E0;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.top-bar {
  height: 80px;
  background-color: #FFFFFF;
  border-bottom: 1px solid #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  position: sticky;
  top: 0;
  z-index: 900;
}

.top-bar-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.bell-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background-color: #F8FAFC;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #F1F5F9;
}

.bell-wrapper:hover {
  background-color: #F1F5F9;
  transform: translateY(-2px);
}

.bell-icon {
  font-size: 20px;
  font-style: normal;
}

.notification-menu {
  width: 360px;
  padding: 0;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  border: none;
}

.notification-header {
  padding: 16px 20px;
  border-bottom: 1px solid #F1F5F9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-header .title {
  font-weight: 700;
  color: #1E293B;
  font-size: 16px;
}

.notification-header .count {
  font-size: 12px;
  color: #64748B;
  background-color: #F1F5F9;
  padding: 2px 10px;
  border-radius: 100px;
}

.notif-item {
  padding: 16px 20px !important;
  border-bottom: 1px solid #F8FAFC;
  white-space: normal !important;
}

.notif-item:hover {
  background-color: #F8FAFC !important;
}

.notif-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notif-title {
  font-weight: 600;
  color: #1E293B;
  font-size: 14px;
}

.notif-message {
  font-size: 13px;
  color: #64748B;
  line-height: 1.4;
}

.notif-time {
  font-size: 11px;
  color: #94A3B8;
  margin-top: 4px;
}

.empty-notif {
  padding: 40px 20px;
  text-align: center;
  color: #94A3B8;
  font-size: 14px;
}

.page-info {
  display: flex;
  flex-direction: column;
}
.username { font-size: 14px; font-weight: 600; color: #0F172A; }
.role { font-size: 12px; color: #64748B; }

.logout-btn {
  width: 100%;
  padding: 10px;
  background: #FFF;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  color: #EF4444;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logout-btn:hover { background-color: #FEF2F2; border-color: #FCA5A5; }

/* MAIN CONTAINER STYLES */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-bar {
  height: 80px;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  flex-shrink: 0;
}
.current-page-title { font-size: 20px; font-weight: 700; margin: 0; color: #0F172A; }
.breadcrumb { font-size: 12px; color: #94A3B8; margin-top: 4px; }

.page-content {
  flex: 1;
  padding: 40px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* ANIMATIONS */
.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from { opacity: 0; transform: translateY(10px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-10px); }

.menu-scroll::-webkit-scrollbar { width: 5px; }
.menu-scroll::-webkit-scrollbar-thumb { background: #E2E8F0; border-radius: 10px; }
</style>