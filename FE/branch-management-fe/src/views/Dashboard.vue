<template>
  <div class="px-8 py-12 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      
      <!-- Welcome Banner -->
      <div class="welcome-banner mb-12">
        <div class="banner-content">
          <h2 class="text-[32px] font-bold text-white mb-2">Chào buổi tối, {{ auth.user?.fullName || auth.user?.username }}! 👋</h2>
          <p class="text-[16px] text-white/80">Hệ thống BranchCore đã sẵn sàng. Hôm nay bạn muốn xử lý công việc gì?</p>
        </div>
        <div class="banner-image">🏢</div>
      </div>

      <!-- Main Stats Grid - Dùng el-row để ổn định -->
      <el-row :gutter="24" class="mb-12">
        <el-col :xs="24" :sm="12" :lg="6" class="mb-6 lg:mb-0">
          <el-card shadow="never" class="meta-stat-card border-l-[6px] border-l-[#0064E0]">
            <div class="flex flex-col">
              <span class="text-[12px] font-bold text-[#5D6C7B] uppercase tracking-wider mb-2">Chi nhánh hoạt động</span>
              <div class="flex items-baseline">
                <span class="text-[40px] font-medium text-[#1C2B33]">{{ stats.totalBranches || 0 }}</span>
                <span class="text-[14px] text-[#31A24C] font-bold">&nbsp;Chi nhánh</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :lg="6" class="mb-6 lg:mb-0">
          <el-card shadow="never" class="meta-stat-card border-l-[6px] border-l-[#31A24C]">
            <div class="flex flex-col">
              <span class="text-[12px] font-bold text-[#5D6C7B] uppercase tracking-wider mb-2">Tổng nhân sự</span>
              <div class="flex items-baseline">
                <span class="text-[40px] font-medium text-[#1C2B33]">{{ stats.totalUsers || 0 }}</span>
                <span class="text-[14px] text-[#5D6C7B]">&nbsp;Thành viên</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :lg="6" class="mb-6 lg:mb-0">
          <el-card shadow="never" class="meta-stat-card border-l-[6px] border-l-[#F7B928]">
            <div class="flex flex-col">
              <span class="text-[12px] font-bold text-[#5D6C7B] uppercase tracking-wider mb-2">Phiếu kho chờ xử lý</span>
              <div class="flex items-baseline">
                <span class="text-[40px] font-medium text-[#F7B928]">{{ stats.totalInventory || 0 }}</span>
                <span class="text-[14px] text-[#5D6C7B]">&nbsp;Yêu cầu</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="never" class="meta-stat-card border-l-[6px] border-l-[#7B61FF]">
            <div class="flex flex-col">
              <span class="text-[12px] font-bold text-[#5D6C7B] uppercase tracking-wider mb-2">Trạng thái hệ thống</span>
              <div class="flex items-center gap-2 mt-2">
                <div class="w-3 h-3 rounded-full bg-[#31A24C] animate-pulse"></div>
                <span class="text-[16px] font-bold text-[#31A24C]">Ổn định</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="32">
        <!-- Quick Actions -->
        <el-col :xs="24" :lg="16" class="mb-8 lg:mb-0">
          <h3 class="text-[20px] font-bold text-[#1C2B33] mb-6 flex items-center gap-2">
            🚀 Thao tác nhanh
          </h3>
          <el-row :gutter="16">
            <el-col :xs="12" :sm="8" v-for="act in quickActions" :key="act.title" class="mb-4">
              <div class="action-card" @click="router.push(act.path)">
                <div class="action-icon" :style="{ backgroundColor: act.bg }">{{ act.icon }}</div>
                <span class="action-title">{{ act.title }}</span>
              </div>
            </el-col>
          </el-row>
        </el-col>

        <!-- System Alerts -->
        <el-col :xs="24" :lg="8">
          <h3 class="text-[20px] font-bold text-[#1C2B33] mb-6 flex items-center gap-2">
            🔔 Thông báo mới nhất
          </h3>
          <el-card shadow="never" class="meta-card">
            <div class="space-y-4">
              <div v-for="i in 3" :key="i" class="p-4 bg-[#F8FAFC] rounded-xl border border-gray-100 mb-3">
                <div class="flex justify-between mb-1">
                  <span class="text-[13px] font-bold text-[#1C2B33]">Hệ thống</span>
                  <span class="text-[11px] text-[#94A3B8]">Vừa xong</span>
                </div>
                <p class="text-[12px] text-[#5D6C7B]">Chào mừng bạn đã quay trở lại. Hãy kiểm tra các yêu cầu điều chuyển kho đang chờ duyệt.</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const router = useRouter();
const stats = ref({
  totalBranches: 0,
  totalUsers: 0,
  activeBranches: 0,
  totalInventory: 0
});

const quickActions = computed(() => {
  const actions = [];
  if (auth.role?.includes('ADMIN')) {
    actions.push({ title: 'Duyệt phiếu kho', icon: '⚖️', bg: '#E8F3FF', path: '/admin/approve-inventory' });
    actions.push({ title: 'Mạng lưới chi nhánh', icon: '🏢', bg: '#E6F4EA', path: '/admin/branches' });
    actions.push({ title: 'Nhật ký hệ thống', icon: '📜', bg: '#FEF6E1', path: '/admin/audit-logs' });
  } else if (auth.role?.includes('MANAGER')) {
    actions.push({ title: 'Chấm công nhân sự', icon: '📅', bg: '#E8F3FF', path: '/manager/attendance' });
    actions.push({ title: 'Kiểm kê kho', icon: '📋', bg: '#E6F4EA', path: '/manager/stocktake' });
    actions.push({ title: 'Điều chuyển kho', icon: '🔄', bg: '#FEF6E1', path: '/manager/inventory-history' });
    actions.push({ title: 'Báo cáo công & lương', icon: '📊', bg: '#F5E8FF', path: '/manager/attendance-report' });
  } else {
    actions.push({ title: 'Lịch sử chấm công', icon: '⏳', bg: '#E8F3FF', path: '/employee/attendance' });
  }
  return actions;
});

const fetchDashboardStats = async () => {
  try {
    const params = {};
    if (!auth.role?.includes('ADMIN') && auth.branchId) {
      params.branchId = auth.branchId;
    }
    const res = await api.get('/admin/dashboard/summary', { params });
    if (res?.data?.data) stats.value = res.data.data;
  } catch (error) { console.error("Lỗi Dashboard:", error); }
};

onMounted(fetchDashboardStats);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

.welcome-banner {
  background: linear-gradient(135deg, #0064E0 0%, #0047AB 100%);
  border-radius: 24px;
  padding: 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 20px 40px rgba(0, 100, 224, 0.15);
}

.banner-content { color: white; }
.banner-image { font-size: 80px; opacity: 0.2; }

:deep(.meta-stat-card) {
  border-radius: 20px !important;
  background-color: #FFFFFF !important;
  padding: 20px !important;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

:deep(.meta-card) {
  border-radius: 24px !important;
  border: none !important;
  padding: 24px;
  background-color: #FFFFFF !important;
}

.action-card {
  background-color: #FFFFFF;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  height: 100%;
}

.action-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  border-color: #0064E0;
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.action-title {
  font-size: 14px;
  font-weight: 700;
  color: #1E293B;
  text-align: center;
}
</style>