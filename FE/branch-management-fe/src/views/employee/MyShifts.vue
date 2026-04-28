<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1200px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Lịch làm việc của tôi</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Xem thông tin các ca làm việc mà bạn được phân công</p>
        </div>
      </div>

      <el-card shadow="never" class="meta-card mb-8">
        <el-table :data="shifts" v-loading="isLoading" class="meta-table" style="width: 100%" empty-text="Bạn chưa được phân ca làm việc nào">
          <el-table-column prop="date" label="Ngày làm việc" width="180">
            <template #default="{row}">
              <span class="font-medium text-[#1C2B33]">{{ formatDate(row.date) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="shiftName" label="Ca làm việc" width="150">
            <template #default="{row}">
              <span class="font-bold text-[#1C2B33]">{{ row.shiftName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Thời gian" width="220">
            <template #default="{row}">
              <span class="text-[#0064E0] font-medium">{{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="branchName" label="Chi nhánh" min-width="250" />
          <el-table-column prop="status" label="Trạng thái" width="150">
            <template #default="{row}">
              <el-tag :class="row.status === 'assigned' ? 'meta-tag-success' : 'meta-tag-warning'">
                {{ row.status === 'assigned' ? 'Đã phân công' : row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus';

const shifts = ref([]);
const isLoading = ref(false);
const auth = useAuthStore();

const fetchMyShifts = async () => {
  if (!auth.user?.id) return;
  isLoading.value = true;
  try {
    const res = await api.get('/mobile/my-shifts', { params: { userId: auth.user.id } });
    shifts.value = res.data?.data || [];
    
    // Sắp xếp các ca theo ngày gần nhất
    shifts.value.sort((a, b) => new Date(b.date) - new Date(a.date));
  } catch (error) {
    ElMessage.error('Không thể tải lịch làm việc');
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

const formatTime = (timeArray) => {
  if (!timeArray) return '';
  if (Array.isArray(timeArray)) {
    const hours = timeArray[0].toString().padStart(2, '0');
    const minutes = timeArray[1].toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }
  return timeArray;
};

onMounted(() => {
  fetchMyShifts();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }
:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 10px; }
:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 24px 0 !important; }
:deep(.meta-tag-success) { border-radius: 100px; font-weight: 700; border: none; padding: 6px 16px; height: auto; background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { border-radius: 100px; font-weight: 700; border: none; padding: 6px 16px; height: auto; background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
</style>
