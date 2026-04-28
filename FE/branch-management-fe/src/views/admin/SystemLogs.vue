<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Nhật Ký Hoạt Động Hệ Thống</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Truy vết mọi hành động quan trọng để đảm bảo an toàn dữ liệu</p>
        </div>
        <el-button class="meta-btn-outline" @click="fetchLogs" :loading="loading">
          <span class="mr-2 text-[16px]">↻</span> Làm mới dữ liệu
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card mb-8">
        <div class="flex flex-wrap gap-6 items-center">
          <span class="text-[14px] font-bold text-[#1C2B33]">Lọc thời gian:</span>
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
          <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="fetchLogs">
            Truy xuất
          </el-button>
        </div>
      </el-card>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="logs" v-loading="loading" class="meta-table" style="width: 100%">
          
          <el-table-column label="Thời gian" width="220">
            <template #default="{row}">
              <span class="text-[14px] text-[#5D6C7B]">{{ formatDateTime(row.createdAt) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="performedBy" label="Tài khoản thực hiện" width="250">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">@{{ row.performedBy || row.username || 'System' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="action" label="Loại hành động" width="220" align="center">
            <template #default="{row}">
              <el-tag :class="getActionTagClass(row.action)">
                {{ row.action }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="description" label="Chi tiết hành động" min-width="350">
            <template #default="{row}">
              <span class="text-[#5D6C7B]">{{ row.description }}</span>
            </template>
          </el-table-column>

        </el-table>

        <div class="mt-8 flex justify-end">
          <el-pagination
            background
            v-model:current-page="currentPage"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="totalElements"
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

const logs = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);
const totalElements = ref(0);
const dateRange = ref([]);

const fetchLogs = async () => {
  loading.value = true;
  try {
    const startDate = dateRange.value?.length === 2 ? dateRange.value[0] : null;
    const endDate = dateRange.value?.length === 2 ? dateRange.value[1] : null;

    const res = await api.get('/admin/audit-logs', {
      params: { 
        page: currentPage.value - 1, 
        size: pageSize.value,
        startDate: startDate, 
        endDate: endDate      
      }
    });
    logs.value = res.data?.data?.content || [];
    totalElements.value = res.data?.data?.totalElements || 0;
  } catch (error) {
    console.error("Lỗi tải nhật ký hệ thống:", error);
  } finally {
    loading.value = false;
  }
};

const handleFilterChange = () => {
  currentPage.value = 1; 
  fetchLogs();
};

const handlePageChange = (val) => {
  currentPage.value = val;
  fetchLogs();
};

const getActionTagClass = (action) => {
  if (!action) return 'meta-tag-info';
  const act = action.toUpperCase();
  if (act.includes('DUYET') || act.includes('THÊM') || act.includes('CREATE')) return 'meta-tag-success';
  if (act.includes('TU_CHOI') || act.includes('XOA') || act.includes('DELETE')) return 'meta-tag-error';
  if (act.includes('SUA') || act.includes('CAP_NHAT') || act.includes('UPDATE')) return 'meta-tag-warning';
  return 'meta-tag-info';
};

const formatDateTime = (val) => {
  if (!val) return '---';
  return new Date(val).toLocaleString('vi-VN', {
    hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric'
  });
};

onMounted(fetchLogs);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');

.font-meta { font-family: 'Montserrat', sans-serif; }

/* Buttons */
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

:deep(.meta-btn-outline) {
  background-color: transparent !important;
  color: #1C2B33 !important;
  border: 2px solid rgba(10, 19, 23, 0.12) !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 14px;
  padding: 10px 22px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-outline:hover) {
  background-color: rgba(70, 90, 105, 0.7) !important;
  color: #FFFFFF !important;
  border-color: transparent !important;
}

/* Cards & Inputs */
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

/* Table */
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

/* Tags */
:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error), :deep(.meta-tag-info) {
  border-radius: 100px;
  font-weight: 700;
  border: none;
  padding: 6px 16px;
  height: auto;
}
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
:deep(.meta-tag-info) { background-color: rgba(0, 100, 224, 0.15); color: #0064E0; }

/* Pagination */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #0064E0 !important;
  border-radius: 8px;
}
:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
  font-family: 'Montserrat', sans-serif;
}
</style>