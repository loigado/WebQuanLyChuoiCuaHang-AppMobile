<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Cấu hình Camera Hệ thống</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Quản lý và theo dõi các luồng trực tiếp từ cửa hàng</p>
        </div>
        <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="showAddModal = true">
          <span class="mr-2 text-[16px]">+</span> Thêm Camera mới
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="cameras" class="meta-table" style="width: 100%">
          
          <el-table-column prop="cameraName" label="Tên Camera" min-width="200">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33] flex items-center gap-2">
                🎥 {{ row.cameraName }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column prop="branchName" label="Chi nhánh" width="220">
            <template #default="{row}">
              <span class="text-[#5D6C7B]">{{ row.branchName }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="streamUrl" label="Stream URL" min-width="350">
            <template #default="{row}">
              <code class="text-[14px] text-[#0064E0] bg-[#E8F3FF] px-3 py-1.5 rounded-[8px] block w-full truncate border border-[#ADD4E0]">
                {{ row.streamUrl }}
              </code>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="row.status === 'active' || row.status === 'ACTIVE' ? 'meta-tag-success' : 'meta-tag-error'">
                {{ row.status?.toUpperCase() }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="Thao tác" width="150" align="center" fixed="right">
            <template #default="{row}">
              <el-button class="meta-btn-danger-sm" @click="handleDelete(row.cameraId)">Gỡ bỏ</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { cameraApi } from '@/api/camera';
import { ElMessage, ElMessageBox } from 'element-plus';

const cameras = ref([]);
const showAddModal = ref(false);

const loadCameras = async () => {
  try {
    const res = await cameraApi.adminGetAll();
    cameras.value = res.data.data; 
  } catch (error) {
    console.error("Lỗi tải camera:", error);
    ElMessage.error("Không thể tải danh sách camera");
  }
};

const handleDelete = async (id) => {
  ElMessageBox.confirm(
    'Bạn có chắc chắn muốn gỡ camera này khỏi hệ thống?',
    'Xác nhận gỡ bỏ',
    { confirmButtonText: 'Xóa ngay', cancelButtonText: 'Hủy', type: 'warning' }
  ).then(async () => {
    try {
      await cameraApi.adminDelete(id);
      ElMessage.success("Đã gỡ camera thành công!");
      loadCameras();
    } catch (e) {
      ElMessage.error("Lỗi khi xóa camera");
    }
  }).catch(() => {});
};

onMounted(loadCameras);
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

:deep(.meta-btn-danger-sm) {
  background-color: transparent !important;
  color: #E41E3F !important;
  border: 1px solid #E41E3F !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 14px;
  padding: 8px 20px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-danger-sm:hover) { background-color: rgba(228, 30, 63, 0.05) !important; }

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