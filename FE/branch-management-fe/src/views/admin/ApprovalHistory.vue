<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Lịch Sử Duyệt Phiếu Kho</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Xem lại các yêu cầu đã được phê duyệt hoặc từ chối</p>
        </div>
      </div>

      <el-card shadow="never" class="meta-card mb-8">
        <div class="flex flex-wrap gap-6 items-center">
          <span class="text-[14px] font-bold text-[#1C2B33]">Kỳ báo cáo:</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="→"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
            @change="handleFilterChange"
            clearable
            class="w-[400px] meta-input"
          />
          <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="fetchHistory">
            Lọc dữ liệu
          </el-button>
        </div>
      </el-card>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="historyData" v-loading="loading" class="meta-table" style="width: 100%">
          <el-table-column prop="transactionCode" label="Mã phiếu" width="180">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.transactionCode }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="Loại phiếu" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="row.transactionType === 'import' ? 'meta-tag-success' : 'meta-tag-warning'">
                {{ row.transactionType === 'import' ? '↓ NHẬP KHO' : '↑ XUẤT KHO' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Thông tin duyệt" min-width="280">
            <template #default="{row}">
              <div v-if="row.status === 'approved'" class="flex flex-col gap-1">
                <span class="text-[14px] text-[#1C2B33]"><span class="text-[#007D1E] font-bold mr-1">✓</span> Đã duyệt bởi: <span class="font-medium">{{ row.approvedByName }}</span></span>
                <span class="text-[12px] text-[#5D6C7B]">{{ formatDate(row.approvedAt) }}</span>
              </div>
              <div v-else-if="row.status === 'rejected'" class="flex flex-col gap-1">
                <span class="text-[14px] text-[#1C2B33]"><span class="text-[#E41E3F] font-bold mr-1">✕</span> Từ chối bởi: <span class="font-medium">{{ row.rejectedByName || 'Admin' }}</span></span>
                <span class="text-[12px] text-[#E41E3F] italic">Lý do: {{ row.rejectionReason }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Chi nhánh" min-width="250">
            <template #default="{row}">
              <div class="flex flex-col gap-1">
                <span v-if="row.fromBranchName" class="text-[14px] text-[#5D6C7B]">Từ: <span class="text-[#1C2B33] font-medium">{{ row.fromBranchName }}</span></span>
                <span v-if="row.toBranchName" class="text-[14px] text-[#5D6C7B]">Đến: <span class="text-[#1C2B33] font-medium">{{ row.toBranchName }}</span></span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="row.status === 'approved' ? 'meta-tag-success' : 'meta-tag-error'">
                {{ row.status === 'approved' ? 'ĐÃ DUYỆT' : 'BỊ TỪ CHỐI' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-8 flex justify-end">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios.js';

const historyData = ref([]);
const loading = ref(false);
const total = ref(0);
const page = ref(0);

const dateRange = ref([]);

const fetchHistory = async () => {
  loading.value = true;
  try {
    const startDate = dateRange.value?.length === 2 ? dateRange.value[0] : null;
    const endDate = dateRange.value?.length === 2 ? dateRange.value[1] : null;

    const res = await api.get('/admin/stock/approve/history', {
      params: { 
        page: page.value, 
        size: 10,
        startDate: startDate, 
        endDate: endDate      
      }
    });
    historyData.value = res.data?.data?.content || [];
    total.value = res.data?.data?.totalElements || 0;
  } catch (error) {
    console.error("Lỗi: ", error);
  } finally {
    loading.value = false;
  }
};

const handleFilterChange = () => {
  page.value = 0; 
  fetchHistory();
};

const handlePageChange = (val) => {
  page.value = val - 1;
  fetchHistory();
};

const formatDate = (date) => {
  if (!date) return '---';
  return new Date(date).toLocaleString('vi-VN', {
    hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric'
  });
};

onMounted(fetchHistory);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');

.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-btn-primary) {
  border-radius: 100px !important;
  background-color: #0064E0 !important;
  border-color: #0064E0 !important;
  color: #FFFFFF !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 14px;
  padding: 10px 24px !important;
  height: auto;
  letter-spacing: -0.14px;
  transition: all 0.2s ease;
}
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }

:deep(.meta-card) {
  border-radius: 20px !important;
  border: none !important;
  background-color: #FFFFFF !important;
  padding: 16px;
}

:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-range-editor.el-input__wrapper) {
  border-radius: 8px !important;
  border: 1px solid #CED0D4 !important;
  box-shadow: none !important;
  padding: 8px 12px;
}
:deep(.meta-input .el-input__wrapper.is-focus) {
  border-color: #0064E0 !important;
  box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important;
}

:deep(.meta-table th.el-table__cell) {
  background-color: #FFFFFF !important;
  color: #5D6C7B !important;
  font-weight: 500 !important;
  border-bottom: 1px solid #DEE3E9 !important;
  padding: 24px 0 16px 0 !important;
  font-size: 14px;
}
:deep(.meta-table td.el-table__cell) {
  border-bottom: 1px solid #F1F4F7 !important;
  padding: 20px 0 !important;
}

:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error) {
  border-radius: 100px;
  font-weight: 700;
  border: none;
  padding: 6px 16px;
  height: auto;
}
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }

/* Pagination styling adjustments */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #0064E0 !important;
  border-radius: 8px;
}
:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
  font-family: 'Montserrat', sans-serif;
}
</style>