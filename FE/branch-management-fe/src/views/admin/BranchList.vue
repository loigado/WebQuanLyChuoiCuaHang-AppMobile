<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Quản lý Chi nhánh</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Theo dõi và vận hành danh sách các điểm bán trên hệ thống</p>
        </div>
        <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="$router.push('/admin/branches/new')">
          <span class="mr-2 text-[18px]">+</span> Thêm chi nhánh
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="branches" class="meta-table" style="width: 100%">
          <el-table-column prop="branchCode" label="Mã" width="120">
            <template #default="{row}">
              <span class="text-[#5D6C7B] font-mono bg-[#F1F4F7] px-2 py-1 rounded-[6px]">{{ row.branchCode }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="branchName" label="Tên chi nhánh" min-width="220">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.branchName }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="address" label="Địa chỉ" min-width="300">
            <template #default="{row}">
              <span class="text-[#5D6C7B] truncate block w-full" :title="row.address">{{ row.address }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="Tọa độ (Lat, Long)" width="200" align="center">
             <template #default="{row}">
              <span class="text-[14px] text-[#8595A4]">{{ row.latitude }}, {{ row.longitude }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="row.status === 'active' ? 'meta-tag-success' : 'meta-tag-error'">
                {{ row.status === 'active' ? 'Đang chạy' : 'Tạm dừng' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="Thao tác" width="220" align="center" fixed="right">
            <template #default="{row}">
              <div class="flex justify-center gap-2">
                <el-button class="meta-btn-secondary-sm" @click="$router.push(`/admin/branches/edit/${row.branchId}`)">Sửa</el-button>
                <el-button class="meta-btn-outline-sm" @click="handleToggle(row.branchId)">Đổi trạng thái</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="branches.length === 0" class="py-16 text-center text-[#5D6C7B]">
          <span class="text-[32px] block mb-4">🏪</span>
          <span class="text-[16px]">Chưa có chi nhánh nào trên hệ thống</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { branchApi } from '@/api/branch';
import { ElMessage, ElMessageBox } from 'element-plus';

const branches = ref([]);

const loadBranches = async () => {
  try {
    const res = await branchApi.getAll();
    branches.value = res.data.data;
  } catch (e) {
    ElMessage.error("Không thể tải danh sách chi nhánh");
  }
};

const handleToggle = async (id) => {
  ElMessageBox.confirm(
    "Bạn có chắc chắn muốn đổi trạng thái hoạt động của chi nhánh này?",
    "Xác nhận",
    { confirmButtonText: "Đồng ý", cancelButtonText: "Hủy", type: "warning" }
  ).then(async () => {
    try {
      await branchApi.toggleStatus(id);
      ElMessage.success("Đổi trạng thái thành công");
      loadBranches(); 
    } catch (error) {
      ElMessage.error("Lỗi khi đổi trạng thái");
    }
  }).catch(() => {});
};

onMounted(loadBranches);
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

:deep(.meta-btn-secondary-sm) {
  background-color: rgba(0, 100, 224, 0.1) !important;
  color: #0064E0 !important;
  border: none !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 13px;
  padding: 8px 16px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-secondary-sm:hover) { background-color: rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-btn-outline-sm) {
  background-color: transparent !important;
  color: #1C2B33 !important;
  border: 1px solid rgba(10, 19, 23, 0.2) !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 13px;
  padding: 8px 16px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-outline-sm:hover) { background-color: rgba(70, 90, 105, 0.05) !important; }

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