<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Báo cáo Công & Lương</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Tổng hợp giờ làm việc của nhân viên tại chi nhánh theo tháng</p>
        </div>
        <el-button class="meta-btn-primary" @click="handleExport">
          <span class="mr-2 text-[16px]">📥</span> Xuất báo cáo (Excel)
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card mb-10">
        <div class="flex flex-wrap gap-6 items-center">
          <div class="flex items-center gap-4">
            <span class="text-[14px] font-bold text-[#1C2B33]">Tháng báo cáo:</span>
            <el-date-picker 
              v-model="reportMonth" 
              type="month" 
              placeholder="Chọn tháng" 
              format="MM/YYYY"
              value-format="YYYY-MM"
              class="w-56 meta-input"
              @change="fetchData"
            />
          </div>
          
          <div class="flex items-center gap-4 flex-1">
            <span class="text-[14px] font-bold text-[#1C2B33]">Tìm kiếm:</span>
            <el-input 
              v-model="searchKeyword" 
              placeholder="Tìm theo mã NV, Họ tên..." 
              class="w-80 meta-input"
              clearable
            >
              <template #prefix><span class="text-[#65676B]">🔍</span></template>
            </el-input>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="filteredData" v-loading="loading" class="meta-table" style="width: 100%">
          <el-table-column label="Nhân viên" min-width="250">
            <template #default="{row}">
              <div class="font-medium text-[#1C2B33] text-[16px]">{{ row.fullName }}</div>
              <div class="text-[13px] text-[#5D6C7B] font-mono mt-1">Mã: {{ row.employeeCode }}</div>
            </template>
          </el-table-column>
          
          <el-table-column prop="totalShifts" label="Tổng số ca" width="150" align="center">
            <template #default="{row}">
              <span class="text-[#1C2B33] font-bold">{{ row.totalShifts }}</span> ca
            </template>
          </el-table-column>

          <el-table-column prop="totalHours" label="Tổng giờ công" width="200" align="center">
            <template #default="{row}">
              <div class="flex flex-col items-center">
                <span class="text-[20px] font-black text-[#0064E0]">{{ row.totalHours }}</span>
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold mt-1">Giờ&nbsp;làm&nbsp;việc</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="totalSalary" label="Dự tính lương" width="220" align="center">
            <template #default="{row}">
              <div class="flex flex-col items-center">
                <span class="text-[18px] font-bold text-[#31A24C]">{{ formatCurrency(row.totalSalary) }}</span>
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold mt-1">Lương&nbsp;tạm&nbsp;tính</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Bất thường" width="180" align="center">
            <template #default="{row}">
              <el-tag :class="row.abnormalDays > 0 ? 'meta-tag-error' : 'meta-tag-success'">
                {{ row.abnormalDays }} ca bất thường
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Hành động" width="150" align="center" fixed="right">
            <template #default="{row}">
              <el-button class="meta-btn-primary-sm" @click="viewDetails(row)">
                Chi tiết
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- Dialog chi tiết bảng lương -->
    <el-dialog v-model="detailsVisible" :title="'Chi tiết bảng lương: ' + selectedUser?.fullName" width="900px" class="meta-dialog">
      <div v-if="detailsLoading" class="py-10 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-[#0064E0] mx-auto"></div>
        <p class="mt-2 text-[#5D6C7B]">Đang tải chi tiết...</p>
      </div>
      <div v-else>
        <div class="bg-[#F1F4F7] p-4 rounded-[12px] mb-6 flex justify-between items-center">
          <div>
             <p class="text-[14px] text-[#5D6C7B]">Nhân viên: <b class="text-[#1C2B33]">{{ selectedUser?.fullName }}</b></p>
             <p class="text-[12px] text-[#5D6C7B]">Mã NV: {{ selectedUser?.employeeCode }}</p>
          </div>
          <div class="text-right">
             <p class="text-[14px] text-[#5D6C7B]">Tháng: <b class="text-[#1C2B33]">{{ reportMonth }}</b></p>
             <p class="text-[16px] font-bold text-[#0064E0]">Tổng lương: {{ formatCurrency(selectedUser?.totalSalary) }}</p>
          </div>
        </div>

        <el-table :data="detailsData" stripe style="width: 100%" class="meta-table">
          <el-table-column label="Ngày" width="120">
            <template #default="{row}">
              {{ new Date(row.checkIn).toLocaleDateString('vi-VN') }}
            </template>
          </el-table-column>
          <el-table-column label="Giờ vào - Giờ ra" min-width="180">
            <template #default="{row}">
              <span class="font-mono text-[#0064E0]">{{ formatTime(row.checkIn) }}</span>
              <span class="mx-2 text-[#8595A4]">→</span>
              <span class="font-mono text-[#0064E0]">{{ row.checkOut ? formatTime(row.checkOut) : '---' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Số giờ" width="100" align="center">
            <template #default="{row}">
              <b class="text-[#1C2B33]">{{ row.totalHours }}</b> h
            </template>
          </el-table-column>
          <el-table-column label="Lương/Giờ" width="140" align="right">
            <template #default="{row}">
              {{ formatCurrency(row.hourlyRate) }}
            </template>
          </el-table-column>
          <el-table-column label="Thành tiền" width="160" align="right">
            <template #default="{row}">
              <b class="text-[#31A24C]">{{ formatCurrency(row.salary) }}</b>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="Trạng thái" width="120" align="center">
            <template #default="{row}">
               <el-tag :type="row.status === 'APPROVED' ? 'success' : 'warning'" size="small">
                  {{ row.status }}
               </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const loading = ref(false);
const attendanceData = ref([]);
const reportMonth = ref(new Date().toISOString().slice(0, 7));
const searchKeyword = ref('');

const detailsVisible = ref(false);
const detailsLoading = ref(false);
const detailsData = ref([]);
const selectedUser = ref(null);

const formatCurrency = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const formatTime = (dateStr) => {
  if (!dateStr) return '---';
  return new Date(dateStr).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
};

const fetchData = async () => {
  loading.value = true;
  try {
    // API kế toán nhưng Manager gọi với branchId của mình
    const res = await api.get('/accountant/attendance', {
      params: {
        branchId: auth.branchId,
        month: reportMonth.value
      }
    });
    attendanceData.value = res.data?.data || [];
  } catch (e) {
    ElMessage.error("Lỗi tải báo cáo công");
  } finally {
    loading.value = false;
  }
};

const filteredData = computed(() => {
  if (!searchKeyword.value) return attendanceData.value;
  const kw = searchKeyword.value.toLowerCase();
  return attendanceData.value.filter(item => 
    item.fullName.toLowerCase().includes(kw) || 
    item.employeeCode.toLowerCase().includes(kw)
  );
});

const handleExport = () => {
  ElMessage.success("Đang chuẩn bị tệp Excel báo cáo tháng " + reportMonth.value);
};

const viewDetails = async (row) => {
  selectedUser.value = row;
  detailsVisible.value = true;
  detailsLoading.value = true;
  try {
    const res = await api.get('/accountant/reports/attendance-details', {
      params: {
        username: row.employeeCode, 
        month: reportMonth.value
      }
    });
    detailsData.value = res.data?.data || [];
  } catch (e) {
    ElMessage.error("Không thể tải chi tiết bảng lương");
  } finally {
    detailsLoading.value = false;
  }
};

onMounted(fetchData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }

:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; }
:deep(.meta-btn-primary-sm) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-size: 12px; font-weight: 600; padding: 5px 14px !important; height: auto; }

:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; }

:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 20px 0 14px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 18px 0 !important; }

:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error), :deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; border: none; padding: 5px 14px; height: auto; }
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error)   { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
:deep(.meta-tag-info)    { background-color: rgba(0, 100, 224, 0.15); color: #0064E0; }
</style>
