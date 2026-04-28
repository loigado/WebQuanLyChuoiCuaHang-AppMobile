<template>
  <div class="enterprise-dashboard" v-loading="loading">
    <!-- Quick Overview Stats -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
      <div class="stat-card-premium gradient-blue">
        <div class="overlay"></div>
        <div class="content">
          <div class="icon">🏢</div>
          <div class="details">
            <span class="label">Tổng Chi Nhánh</span>
            <h2 class="value">{{ summaryData.totalBranches || 0 }}</h2>
            <span class="trend">Đang hoạt động ổn định</span>
          </div>
        </div>
      </div>

      <div class="stat-card-premium gradient-indigo">
        <div class="overlay"></div>
        <div class="content">
          <div class="icon">👥</div>
          <div class="details">
            <span class="label">Tổng Nhân Sự</span>
            <h2 class="value">{{ summaryData.totalEmployees || 0 }}</h2>
            <span class="trend">Tăng 2% tháng này</span>
          </div>
        </div>
      </div>

      <div class="stat-card-premium gradient-emerald">
        <div class="overlay"></div>
        <div class="content">
          <div class="icon">📦</div>
          <div class="details">
            <span class="label">Tổng Tồn Kho</span>
            <h2 class="value">{{ (summaryData.totalStockQuantity || 0).toLocaleString() }}</h2>
            <span class="trend">Ghi nhận toàn hệ thống</span>
          </div>
        </div>
      </div>

      <div class="stat-card-premium gradient-amber">
        <div class="overlay"></div>
        <div class="content">
          <div class="icon">⚠️</div>
          <div class="details">
            <span class="label">Hàng Sắp Hết</span>
            <h2 class="value">{{ summaryData.lowStockCount || 0 }}</h2>
            <span class="trend">Cần kiểm tra ngay</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-10">
      <!-- Inventory Distribution -->
      <div class="chart-card">
        <div class="card-header">
          <h3 class="title">Phân bổ tồn kho theo chi nhánh</h3>
          <p class="subtitle">Tỉ trọng lưu kho trên toàn mạng lưới</p>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="stockPieOption" autoresize />
        </div>
      </div>

      <!-- Attendance Trends -->
      <div class="chart-card">
        <div class="card-header">
          <h3 class="title">Hiệu suất chấm công</h3>
          <p class="subtitle">Thống kê số ca làm việc 7 ngày qua</p>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="attendanceBarOption" autoresize />
        </div>
      </div>
    </div>

    <!-- Recent Logs / Activity -->
    <div class="chart-card">
      <div class="card-header border-none">
        <h3 class="title">Nhật ký vận hành hệ thống</h3>
        <p class="subtitle">Các hoạt động quản trị gần nhất</p>
      </div>
      <el-table :data="recentLogs" class="enterprise-table" stripe>
        <el-table-column prop="createdAt" label="Thời gian" width="200">
           <template #default="{row}">
             <span class="text-slate-500 text-sm">{{ formatDate(row.createdAt) }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="username" label="Người thực hiện" width="180">
           <template #default="{row}">
             <div class="flex items-center gap-2">
               <div class="w-6 h-6 rounded-full bg-slate-200 flex items-center justify-center text-[10px] font-bold">
                 {{ row.username?.charAt(0).toUpperCase() }}
               </div>
               <span class="font-medium text-slate-700">{{ row.username }}</span>
             </div>
           </template>
        </el-table-column>
        <el-table-column prop="action" label="Hành động">
           <template #default="{row}">
             <el-tag size="small" :type="getActionType(row.action)" effect="light" class="rounded-lg font-bold">
               {{ row.action }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="details" label="Chi tiết" min-width="300" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/api/axios';
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { PieChart, BarChart } from "echarts/charts";
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from "echarts/components";
import VChart from "vue-echarts";

use([CanvasRenderer, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent]);

const loading = ref(false);
const summaryData = ref({});
const recentLogs = ref([]);

const stockPieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: '5%', left: 'center', icon: 'circle' },
  series: [
    {
      name: 'Số lượng tồn',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: '14', fontWeight: 'bold' } },
      data: summaryData.value.stockByBranch?.map(i => ({ value: i.totalQuantity, name: i.branchName })) || []
    }
  ]
}));

const attendanceBarOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: summaryData.value.attendanceTrend?.map(i => i.date) || [], axisLine: { show: false } },
  yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
  series: [
    {
      data: summaryData.value.attendanceTrend?.map(i => i.count) || [],
      type: 'bar',
      barWidth: '40%',
      itemStyle: {
        color: '#6366F1',
        borderRadius: [4, 4, 0, 0]
      }
    }
  ]
}));

const fetchData = async () => {
  loading.value = true;
  try {
    const [statsRes, logsRes] = await Promise.all([
      api.get('/admin/dashboard/summary'),
      api.get('/admin/audit-logs', { params: { size: 10 } })
    ]);
    summaryData.value = statsRes.data?.data || {};
    recentLogs.value = logsRes.data?.data?.content || [];
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateStr) => new Date(dateStr).toLocaleString();
const getActionType = (action) => {
  if (action.includes('DELETE')) return 'danger';
  if (action.includes('UPDATE')) return 'warning';
  return 'success';
};

onMounted(fetchData);
</script>

<style scoped>
.enterprise-dashboard {
  animation: fadeIn 0.6s ease-out;
}

/* PREMIUM STAT CARDS */
.stat-card-premium {
  position: relative;
  height: 160px;
  border-radius: 24px;
  overflow: hidden;
  padding: 24px;
  display: flex;
  align-items: center;
  transition: transform 0.3s;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}
.stat-card-premium:hover { transform: translateY(-8px); }

.stat-card-premium .overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.stat-card-premium .content { position: relative; z-index: 2; display: flex; gap: 20px; align-items: center; }

.icon {
  width: 60px; height: 60px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px;
}

.label { font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; }
.value { font-size: 32px; font-weight: 800; margin: 4px 0; color: white; }
.trend { font-size: 11px; font-weight: 500; }

.gradient-blue { background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%); color: #DBEAFE; }
.gradient-indigo { background: linear-gradient(135deg, #6366F1 0%, #4F46E5 100%); color: #E0E7FF; }
.gradient-emerald { background: linear-gradient(135deg, #10B981 0%, #059669 100%); color: #D1FAE5; }
.gradient-amber { background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%); color: #FEF3C7; }

/* CHART CARDS */
.chart-card {
  background: white;
  border-radius: 24px;
  padding: 32px;
  border: 1px solid #F1F5F9;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.card-header { margin-bottom: 24px; }
.title { font-size: 18px; font-weight: 700; color: #1E293B; margin: 0; }
.subtitle { font-size: 13px; color: #94A3B8; margin-top: 4px; }

.chart-container { height: 350px; }
.chart { width: 100%; height: 100%; }

.enterprise-table { border-radius: 12px; overflow: hidden; }

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98); }
  to { opacity: 1; transform: scale(1); }
}
</style>