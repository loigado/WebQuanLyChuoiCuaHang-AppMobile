<template>
  <div class="px-8 py-16 md:px-16 bg-[#FFFFFF] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Báo cáo chấm công</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Theo dõi giờ công và các ca làm việc bất thường của nhân sự</p>
        </div>
        <el-button class="meta-btn-outline" @click="handleExportExcel">
          <span class="mr-2 text-[16px]">📥</span> Xuất Excel
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card-gray mb-10">
        <div class="flex flex-wrap gap-6 items-center">
          <el-select v-model="selectedBranch" placeholder="Tất cả chi nhánh" clearable class="w-64 meta-input" @change="fetchAttendanceData">
            <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
          </el-select>
          
          <el-date-picker 
            v-model="reportMonth" 
            type="month" 
            placeholder="Chọn tháng" 
            format="MM/YYYY"
            value-format="YYYY-MM"
            class="w-56 meta-input"
            @change="fetchAttendanceData"
          />
          
          <el-input 
            v-model="searchKeyword" 
            placeholder="Tìm theo mã NV, Họ tên..." 
            class="w-80 meta-input"
            clearable
          >
            <template #prefix><span class="text-[#65676B]">🔍</span></template>
          </el-input>
        </div>
      </el-card>

      <div class="border border-[#DEE3E9] rounded-[20px] overflow-hidden">
        <el-table 
          :data="filteredData" 
          style="width: 100%" 
          v-loading="loading"
          class="meta-table"
        >
          <el-table-column prop="branchName" label="Chi nhánh" width="200">
            <template #default="{row}">
              <span class="text-[#5D6C7B]">{{ row.branchName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="employeeCode" label="Mã NV" width="140">
            <template #default="{row}">
              <span class="text-[#65676B] bg-[#F1F4F7] px-3 py-1.5 rounded-[8px] text-[14px] font-mono">{{ row.employeeCode }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="fullName" label="Họ tên nhân viên" min-width="250">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.fullName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalShifts" label="Tổng ca" width="120" align="center">
            <template #default="{row}">
              <span class="text-[#1C2B33]">{{ row.totalShifts }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalHours" label="Tổng giờ công" width="160" align="center">
            <template #default="{row}">
              <span class="font-medium text-[#0064E0] text-[16px]">{{ row.totalHours }}h</span>
            </template>
          </el-table-column>
          <el-table-column prop="abnormalDays" label="Ca bất thường" width="180" align="center">
            <template #default="{row}">
              <el-tag 
                :class="row.abnormalDays > 0 ? 'meta-tag-warning' : 'meta-tag-success'" 
                class="w-full justify-center"
              >
                {{ row.abnormalDays }} ca
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/axios';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const branches = ref([]);
const attendanceData = ref([]);
const selectedBranch = ref(null);
const reportMonth = ref(new Date().toISOString().slice(0, 7));
const searchKeyword = ref('');

const filteredData = computed(() => {
  if (!searchKeyword.value) return attendanceData.value;
  const keyword = searchKeyword.value.toLowerCase();
  return attendanceData.value.filter(item => 
    item.fullName.toLowerCase().includes(keyword) || 
    item.employeeCode.toLowerCase().includes(keyword)
  );
});

const loadBranches = async () => {
  try {
    const res = await api.get('/admin/branches/active');
    branches.value = res.data?.data || [];
  } catch (error) {
    console.error("Lỗi tải chi nhánh", error);
  }
};

const fetchAttendanceData = async () => {
  if (!reportMonth.value) {
    attendanceData.value = [];
    return;
  }
  
  loading.value = true;
  try {
    const res = await api.get('/accountant/attendance', {
      params: { 
        branchId: selectedBranch.value, 
        month: reportMonth.value 
      }
    });
    attendanceData.value = res.data?.data || [];
    if (attendanceData.value.length === 0) {
      ElMessage.info("Không có dữ liệu chấm công cho kỳ này");
    }
  } catch (error) {
    console.error("Lỗi tải dữ liệu chấm công", error);
    ElMessage.error("Không thể kết nối tới máy chủ để tải dữ liệu");
  } finally {
    loading.value = false;
  }
};

const handleExportExcel = () => {
  ElMessage.success("Hệ thống đang trích xuất dữ liệu ra file Excel...");
};

onMounted(() => {
  loadBranches();
  fetchAttendanceData();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');

.font-meta {
  font-family: 'Montserrat', sans-serif;
}

/* Outlined Pill Button */
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

/* Card Overrides */
:deep(.meta-card-gray) {
  border-radius: 20px !important;
  border: none !important;
  background-color: #F7F8FA !important;
  padding: 16px;
}

/* Input Overrides */
:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper) {
  border-radius: 8px !important;
  border: 1px solid #CED0D4 !important;
  box-shadow: none !important;
  padding: 8px 12px;
}
:deep(.meta-input .el-input__wrapper.is-focus), :deep(.meta-input .el-select__wrapper.is-focus) {
  border-color: #0064E0 !important;
  box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important;
}

/* Table Overrides */
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

/* Tag Overrides */
:deep(.meta-tag-success), :deep(.meta-tag-warning) {
  border-radius: 100px;
  font-weight: 700;
  border: none;
  padding: 6px 16px;
  height: auto;
}
:deep(.meta-tag-success) {
  background-color: rgba(36, 228, 0, 0.15);
  color: #007D1E;
}
:deep(.meta-tag-warning) {
  background-color: rgba(255, 123, 145, 0.15);
  color: #E41E3F;
}
</style>