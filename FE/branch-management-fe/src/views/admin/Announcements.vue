<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Quản Lý Thông Báo</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Đăng tin tức và lịch nghỉ nội bộ cho toàn hệ thống</p>
        </div>
        <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="openCreateModal">
          <span class="mr-2 text-[18px]">+</span> Đăng thông báo mới
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="announcements" v-loading="loading" class="meta-table" style="width: 100%">
          <el-table-column label="Ngày đăng" width="180">
            <template #default="{row}">
              <span class="text-[#5D6C7B] font-mono bg-[#F1F4F7] px-2 py-1 rounded-[6px]">{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="title" label="Tiêu đề" min-width="250">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.title }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="Hết hạn" width="180">
            <template #default="{row}">
              <span v-if="row.expiryDate" class="text-[#E41E3F] font-bold">{{ formatDate(row.expiryDate) }}</span>
              <span v-else class="text-[#5D6C7B]">Vĩnh viễn</span>
            </template>
          </el-table-column>

          <el-table-column label="Người đăng" width="150">
            <template #default="{row}">
              <span class="text-[#0064E0] font-bold">{{ row.author }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Thao tác" width="200" align="center" fixed="right">
            <template #default="{row}">
              <div class="flex justify-center gap-2">
                <el-button class="meta-btn-secondary-sm" @click="openEditModal(row)">Sửa</el-button>
                <el-button class="meta-btn-danger-sm" @click="deleteAnnouncement(row.id)">Xóa</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- Modal đăng/sửa thông báo -->
      <el-dialog 
        v-model="showModal" 
        :title="isEditing ? 'Chỉnh Sửa Thông Báo' : 'Đăng Thông Báo Mới'"
        width="500px"
        class="meta-dialog"
      >
        <div class="space-y-4">
          <div class="form-group">
            <label class="block text-[14px] font-bold text-[#1C2B33] mb-2">Tiêu đề thông báo</label>
            <el-input v-model="form.title" class="meta-input" placeholder="Ví dụ: Lịch nghỉ lễ 30/4..." />
          </div>
          <div class="form-group mt-4">
            <label class="block text-[14px] font-bold text-[#1C2B33] mb-2">Nội dung chi tiết</label>
            <el-input v-model="form.content" type="textarea" :rows="5" class="meta-input" placeholder="Nội dung..." />
          </div>
          <div class="form-group mt-4" v-if="!isEditing">
            <label class="block text-[14px] font-bold text-[#1C2B33] mb-2">Thời gian hiển thị (số ngày)</label>
            <el-select v-model="form.expiryDays" class="meta-input w-full">
              <el-option label="1 ngày" value="1" />
              <el-option label="2 ngày" value="2" />
              <el-option label="3 ngày" value="3" />
              <el-option label="7 ngày" value="7" />
              <el-option label="Vĩnh viễn" value="0" />
            </el-select>
          </div>
        </div>
        <template #footer>
          <div class="flex justify-end gap-3 mt-6">
            <el-button @click="showModal = false" class="meta-btn-outline-sm">Hủy</el-button>
            <el-button @click="saveAnnouncement" :loading="loading" class="meta-btn-primary">
              {{ isEditing ? 'Cập nhật' : 'Đăng ngay' }}
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';
import { ElMessage, ElMessageBox } from 'element-plus';

const announcements = ref([]);
const showModal = ref(false);
const isEditing = ref(false);
const loading = ref(false);
const currentId = ref(null);

const form = ref({
  title: '',
  content: '',
  author: 'Admin',
  expiryDays: '3'
});

const API_BASE = '/admin/announcements';

const fetchAnnouncements = async () => {
  loading.value = true;
  try {
    const response = await api.get(API_BASE);
    announcements.value = response.data;
  } catch (error) {
    console.error("Lỗi lấy thông báo:", error);
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  isEditing.value = false;
  currentId.value = null;
  form.value = { title: '', content: '', author: 'Admin', expiryDays: '3' };
  showModal.value = true;
};

const openEditModal = (item) => {
  isEditing.value = true;
  currentId.value = item.id;
  form.value = { title: item.title, content: item.content, author: item.author };
  showModal.value = true;
};

const saveAnnouncement = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning("Vui lòng nhập đầy đủ tiêu đề và nội dung");
    return;
  }
  loading.value = true;
  try {
    if (isEditing.value) {
      await api.put(`${API_BASE}/${currentId.value}`, form.value);
      ElMessage.success("Đã cập nhật thông báo!");
    } else {
      await api.post(API_BASE, form.value);
      ElMessage.success("Đã đăng thông báo thành công!");
    }
    showModal.value = false;
    fetchAnnouncements();
  } catch (error) {
    ElMessage.error("Thao tác thất bại");
  } finally {
    loading.value = false;
  }
};

const deleteAnnouncement = async (id) => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn xóa thông báo này?',
      'Xác nhận xóa',
      { confirmButtonText: 'Xóa ngay', cancelButtonText: 'Hủy', type: 'warning' }
    );
    await api.delete(`${API_BASE}/${id}`);
    ElMessage.success("Đã xóa thông báo");
    fetchAnnouncements();
  } catch (error) {
    if (error !== 'cancel') ElMessage.error("Lỗi khi xóa");
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString('vi-VN');
};

onMounted(fetchAnnouncements);
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
}

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
}

:deep(.meta-btn-danger-sm) {
  background-color: rgba(228, 30, 63, 0.1) !important;
  color: #E41E3F !important;
  border: none !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 13px;
  padding: 8px 16px !important;
  height: auto;
}

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
}

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

:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper) {
  border-radius: 8px !important;
  border: 1px solid #CED0D4 !important;
  box-shadow: none !important;
  padding: 4px 12px;
}
</style>
