<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Quản lý Nhân viên Chi nhánh</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Dành riêng cho Quản lý cửa hàng (Manager)</p>
        </div>
        <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="handleAdd">
          <span class="mr-2 text-[18px]">+</span> Thêm Nhân Viên
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="employees" v-loading="loading" class="meta-table" style="width: 100%">
          <el-table-column prop="username" label="Username" width="180">
            <template #default="{row}">
              <span class="text-[#5D6C7B] font-mono bg-[#F1F4F7] px-2 py-1 rounded-[6px]">{{ row.username }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="fullName" label="Họ và tên" min-width="250">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.fullName }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="role" label="Vai trò" width="160">
            <template #default="{row}">
              <span class="text-[#0064E0] font-bold">{{ row.role }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="row.status?.toUpperCase() === 'ACTIVE' ? 'meta-tag-success' : 'meta-tag-error'">
                {{ row.status?.toUpperCase() || 'UNKNOWN' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="Thao tác" width="150" align="center" fixed="right">
            <template #default="scope">
              <el-button class="meta-btn-secondary-sm" @click="handleEdit(scope.row)">Sửa</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="!loading && employees.length === 0" class="py-16 text-center text-[#5D6C7B]">
          <span class="text-[32px] block mb-4">👥</span>
          <span class="text-[16px]">Chi nhánh của bạn chưa có nhân viên nào</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';

const router = useRouter();
const employees = ref([]);
const loading = ref(false);

const fetchEmployees = async () => {
  loading.value = true;
  try {
    const res = await api.get('/manager/employees');
    employees.value = res.data.data;
  } catch (error) {
    console.error("Lỗi tải danh sách nhân viên:", error);
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  router.push('/admin/users/new'); // Chuyển đến form thêm user (tái sử dụng form Admin)
};

const handleEdit = (row) => {
  router.push(`/admin/users/edit/${row.userId}`);
};

onMounted(fetchEmployees);
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
  transition: all 0.2s ease;
}
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }

:deep(.meta-btn-secondary-sm) {
  background-color: rgba(0, 100, 224, 0.1) !important;
  color: #0064E0 !important;
  border: none !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 13px;
  padding: 8px 20px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-secondary-sm:hover) { background-color: rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-card) {
  border-radius: 20px !important;
  border: none !important;
  background-color: #FFFFFF !important;
  padding: 16px;
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

:deep(.meta-tag-success), :deep(.meta-tag-error) {
  border-radius: 100px;
  font-weight: 700;
  border: none;
  padding: 6px 16px;
  height: auto;
}
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
</style>