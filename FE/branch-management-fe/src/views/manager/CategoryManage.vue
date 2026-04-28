<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Quản lý Danh mục Sản phẩm</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Thiết lập cấu trúc phân cấp (Nguyên liệu, Thành phẩm, Vật tư...)</p>
        </div>
        <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="handleAdd()">
          <span class="mr-2 text-[18px]">+</span> Thêm Danh Mục Gốc
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table v-loading="isLoading" :data="categories" row-key="categoryId" default-expand-all class="meta-table-tree" style="width: 100%">
          
          <el-table-column prop="categoryName" label="Tên danh mục" min-width="300">
            <template #default="{ row }">
              <span class="font-medium text-[#1C2B33] text-[16px]">{{ row.categoryName }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="categoryCode" label="Mã danh mục" width="200">
            <template #default="{ row }">
              <span class="text-[#5D6C7B] font-mono bg-[#F1F4F7] px-2 py-1 rounded-[6px]">{{ row.categoryCode }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="description" label="Mô tả" min-width="250" show-overflow-tooltip>
             <template #default="{ row }"><span class="text-[#5D6C7B]">{{ row.description || '—' }}</span></template>
          </el-table-column>

          <el-table-column label="Thao tác" width="250" align="center" fixed="right">
            <template #default="scope">
              <div class="flex justify-center gap-2">
                <el-button class="meta-btn-icon-primary" size="small" @click="handleAdd(scope.row)" title="Thêm danh mục con">➕</el-button>
                <el-button class="meta-btn-secondary-sm" size="small" @click="handleEdit(scope.row)">Sửa</el-button>
                <el-button class="meta-btn-danger-sm" size="small" @click="handleDelete(scope.row)">Xóa</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-dialog v-model="dialogVisible" :title="isEditing ? 'Sửa Danh Mục' : 'Thêm Danh Mục'" width="500px" class="meta-dialog" destroy-on-close>
        <div class="flex flex-col gap-5 pt-2">
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Mã danh mục <span class="text-[#E41E3F]">*</span></label>
            <input v-model="form.categoryCode" placeholder="VD: NGUYEN_LIEU" :disabled="isEditing" class="meta-native-input disabled:bg-[#F1F4F7]" />
          </div>
          
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Tên danh mục <span class="text-[#E41E3F]">*</span></label>
            <input v-model="form.categoryName" placeholder="Nhập tên danh mục" class="meta-native-input" />
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Thuộc danh mục cha</label>
            <el-tree-select v-model="form.parentId" :data="categories" node-key="categoryId" :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }" check-strictly clearable placeholder="Để trống nếu là danh mục gốc" class="meta-input w-full" />
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Mô tả thêm</label>
            <textarea v-model="form.description" rows="3" placeholder="Ghi chú về danh mục này..." class="meta-native-input resize-none"></textarea>
          </div>
        </div>
        <template #footer>
          <div class="flex gap-3 justify-end pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary" @click="dialogVisible = false">Hủy bỏ</el-button>
            <el-button class="meta-btn-primary" @click="handleSave">Lưu danh mục</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios.js';
import { ElMessage, ElMessageBox } from 'element-plus';

const categories = ref([]);
const isLoading = ref(false);
const dialogVisible = ref(false);
const isEditing = ref(false);

const form = ref({ categoryId: null, categoryCode: '', categoryName: '', description: '', parentId: null });

const fetchCategories = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/manager/categories/tree');
    categories.value = (res.data && res.data.success) ? (res.data.data || []) : (res.data || []);
  } catch (error) {
    ElMessage.error('Không thể tải danh sách danh mục');
  } finally {
    isLoading.value = false;
  }
};

const handleAdd = (parentRow = null) => {
  isEditing.value = false;
  form.value = { categoryId: null, categoryCode: '', categoryName: '', description: '', parentId: parentRow ? parentRow.categoryId : null };
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  isEditing.value = true;
  form.value = { categoryId: row.categoryId, categoryCode: row.categoryCode, categoryName: row.categoryName, description: row.description, parentId: row.parentId };
  dialogVisible.value = true;
};

const handleSave = async () => {
  if (!form.value.categoryCode || !form.value.categoryName) return ElMessage.warning('Vui lòng nhập Mã và Tên danh mục');
  if (isEditing.value && form.value.parentId === form.value.categoryId) return ElMessage.error('Một danh mục không thể làm cha của chính nó!');

  try {
    const payload = { ...form.value };
    if (isEditing.value) {
      await api.put(`/manager/categories/${payload.categoryId}`, payload);
      ElMessage.success('Cập nhật thành công');
    } else {
      await api.post('/manager/categories', payload);
      ElMessage.success('Tạo mới thành công');
    }
    dialogVisible.value = false;
    fetchCategories();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Lỗi khi lưu');
  }
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`Bạn có chắc muốn xóa danh mục "${row.categoryName}" không?`, 'Cảnh báo', { type: 'warning', confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy' }).then(async () => {
    try {
      await api.delete(`/manager/categories/${row.categoryId}`);
      ElMessage.success('Đã xóa thành công');
      fetchCategories();
    } catch (error) { ElMessage.error('Lỗi khi xóa'); }
  }).catch(() => {});
};

onMounted(fetchCategories);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }

:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }

:deep(.meta-btn-secondary) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }

:deep(.meta-btn-secondary-sm) { background-color: rgba(0, 100, 224, 0.1) !important; color: #0064E0 !important; border: none !important; border-radius: 100px !important; font-weight: 600; padding: 6px 16px !important; transition: all 0.2s ease; }
:deep(.meta-btn-danger-sm) { background-color: rgba(228, 30, 63, 0.1) !important; color: #E41E3F !important; border: none !important; border-radius: 100px !important; font-weight: 600; padding: 6px 16px !important; transition: all 0.2s ease; }
:deep(.meta-btn-icon-primary) { background-color: #E8F3FF !important; color: #0064E0 !important; border: none !important; border-radius: 100px !important; padding: 6px 12px !important; transition: all 0.2s ease; }

.meta-native-input { width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid #CED0D4; font-family: 'Montserrat', sans-serif; font-size: 14px; color: #1C2B33; transition: all 0.2s ease; }
.meta-native-input:focus { outline: none; border-color: #0064E0; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2); }

:deep(.meta-input .el-input__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; padding: 8px 12px; }
:deep(.meta-input .el-input__wrapper.is-focus) { border-color: #0064E0 !important; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important; }

/* Tree Table Override */
:deep(.meta-table-tree th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; font-size: 14px; border-right: none !important; }
:deep(.meta-table-tree td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 20px 0 !important; border-right: none !important; }
:deep(.meta-table-tree .el-table__inner-wrapper::before) { display: none; }
:deep(.meta-table-tree .el-table__row--level-1) { background-color: #FAFAFA !important; }
:deep(.meta-table-tree .el-table__row--level-2) { background-color: #F7F8FA !important; }

/* Dialog */
:deep(.meta-dialog) { border-radius: 20px !important; font-family: 'Montserrat', sans-serif; }
:deep(.meta-dialog .el-dialog__header) { border-bottom: 1px solid #DEE3E9; padding-bottom: 16px; margin-right: 0; }
:deep(.meta-dialog .el-dialog__title) { font-weight: 600; color: #1C2B33; }
</style>