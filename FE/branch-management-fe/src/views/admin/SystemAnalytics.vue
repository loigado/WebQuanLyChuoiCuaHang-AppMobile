<template>
  <div class="px-8 py-10 bg-[#F8FAFC] min-h-screen font-inter">
    <div class="max-w-[1400px] mx-auto">
      <div class="flex justify-between items-center mb-8">
        <div>
          <h1 class="text-[28px] font-bold text-[#0F172A]">Phân tích hệ thống</h1>
          <p class="text-[#64748B] mt-1">Dữ liệu tổng hợp toàn bộ mạng lưới chi nhánh</p>
        </div>
        <el-button type="primary" @click="fetchData" :loading="loading" class="meta-btn">Làm mới dữ liệu</el-button>
      </div>

      <el-row :gutter="20" class="mb-10">
        <template v-for="(val, key) in summary" :key="key">
          <el-col :span="4" v-if="formatKey(key) !== key" class="mb-4">
            <el-card shadow="never" class="stat-card">
              <p class="stat-label uppercase text-[10px] font-bold tracking-wider text-[#94A3B8] mb-1">{{ formatKey(key) }}</p>
              <h3 class="stat-value text-[22px] font-black text-[#0F172A]">{{ val }}</h3>
            </el-card>
          </el-col>
        </template>
      </el-row>

      <!-- Biểu đồ -->
      <el-row :gutter="24">
        <el-col :span="14">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="flex justify-between items-center">
                <span class="font-bold text-[#1E293B]">Giá trị tồn kho theo danh mục</span>
                <el-tag type="success">Dữ liệu thời gian thực</el-tag>
              </div>
            </template>
            <div class="h-[400px] pb-10 border-b border-[#E2E8F0] overflow-x-auto" 
                 style="display: flex !important; flex-direction: row !important; align-items: flex-end !important; justify-content: center !important; gap: 60px !important;">
              <div v-for="item in inventoryValue" :key="item.categoryName" class="group" 
                   style="display: flex !important; flex-direction: column !important; align-items: center !important; min-width: 120px;">
                  
                  <!-- Bar -->
                  <div class="transition-all duration-500 hover:opacity-80 shadow-md" 
                       :style="{
                         height: item.height + 'px', 
                         width: '50px', 
                         backgroundColor: '#0064E0', 
                         borderRadius: '8px 8px 0 0',
                         background: 'linear-gradient(to top, #0064E0, #3B82F6)'
                       }">
                  </div>
                  
                  <!-- Labels -->
                  <div class="mt-4 text-center">
                    <p class="text-[13px] font-bold text-[#1E293B]">{{ item.categoryName }}</p>
                    <p class="text-[11px] font-medium text-[#0064E0]">
                      {{ new Intl.NumberFormat('vi-VN').format(item.totalValue) }} đ
                    </p>
                  </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="10">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="font-bold text-[#1E293B]">Hiệu suất phê duyệt phiếu</span>
            </template>
            <div class="py-10 text-center">
               <el-progress type="circle" :percentage="workflow.approvalRate || 0" :width="200" stroke-width="12" color="#0064E0" />
               <div class="mt-8 grid grid-cols-2 gap-4">
                  <div class="p-4 bg-[#F1F5F9] rounded-xl">
                    <p class="text-[12px] text-[#64748B]">Tỷ lệ Duyệt</p>
                    <p class="text-[20px] font-bold text-[#0064E0]">{{ workflow.approvalRate }}%</p>
                  </div>
                  <div class="p-4 bg-[#F1F5F9] rounded-xl">
                    <p class="text-[12px] text-[#64748B]">Tỷ lệ Từ chối</p>
                    <p class="text-[20px] font-bold text-[#EF4444]">{{ workflow.rejectionRate }}%</p>
                  </div>
               </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const summary = ref({});
const workflow = ref({});
const inventoryValue = ref([]);

const formatKey = (key) => {
  const maps = {
    'totalBranches': 'Chi nhánh',
    'totalProducts': 'Sản phẩm',
    'totalUsers': 'Nhân viên',
    'pendingTransactions': 'Phiếu chờ duyệt',
    'totalTransactions': 'Tổng giao dịch',
    'totalAttendances': 'Chấm công',
    'importCount': 'Nhập kho',
    'exportCount': 'Xuất kho',
    'transferCount': 'Điều chuyển'
  };
  return maps[key] || key;
};

const fetchData = async () => {
  loading.value = true;
  try {
    const [resSum, resWork, resInv] = await Promise.all([
      api.get('/admin/reports/summary'),
      api.get('/admin/reports/workflow-stats'),
      api.get('/admin/reports/inventory-value')
    ]);
    summary.value = resSum.data || {};
    workflow.value = resWork.data || {};
    
    // Xử lý dữ liệu tồn kho để vẽ biểu đồ đẹp hơn
    const rawInv = resInv.data || [];
    const maxVal = Math.max(...rawInv.map(i => i.totalValue || 0), 1);
    inventoryValue.value = rawInv.map(i => {
      const val = i.totalValue || 0;
      return {
        ...i,
        totalValue: val,
        height: Math.max((val / maxVal) * 200, 15) // Tối thiểu 15px để luôn thấy thanh
      };
    });
  } catch (e) {
    ElMessage.error("Không thể tải dữ liệu báo cáo");
  } finally {
    loading.value = false;
  }
};

onMounted(fetchData);
</script>

<style scoped>
.stat-card {
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  padding: 10px;
}
.chart-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.meta-btn {
  border-radius: 10px;
  padding: 12px 24px;
}
</style>
