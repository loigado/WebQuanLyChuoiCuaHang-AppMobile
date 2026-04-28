<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Danh sách Sản phẩm</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Quản lý thông tin, giá bán và định mức tồn kho hàng hóa</p>
        </div>
        <div class="flex gap-4">
          <el-upload action="#" :auto-upload="false" :on-change="handleImportExcel" :show-file-list="false">
            <el-button class="meta-btn-secondary">
              <span class="mr-2">📥</span> Import Excel
            </el-button>
          </el-upload>
          <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="openAddDialog">
            <span class="mr-2 text-[18px]">+</span> Thêm mới
          </el-button>
        </div>
      </div>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="products" v-loading="isLoading" class="meta-table" style="width: 100%">
          <el-table-column prop="productCode" label="Mã SP" width="140">
            <template #default="{row}">
              <span class="text-[#5D6C7B] font-mono bg-[#F1F4F7] px-2 py-1 rounded-[6px]">{{ row.productCode }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="productName" label="Tên sản phẩm" min-width="250">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.productName }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="categoryName" label="Danh mục" width="180">
            <template #default="{row}"><span class="text-[#5D6C7B]">{{ row.categoryName }}</span></template>
          </el-table-column>
          
          <el-table-column prop="unit" label="ĐVT" width="100" align="center">
            <template #default="{row}"><span class="text-[#1C2B33] font-medium">{{ row.unit }}</span></template>
          </el-table-column>
          
          <el-table-column prop="price" label="Giá bán" width="150" align="right">
            <template #default="{row}">
              <span class="text-[#0064E0] font-bold">{{ row.price ? row.price.toLocaleString() : 0 }} ₫</span>
            </template>
          </el-table-column>
          
          <el-table-column label="Min - Max" width="140" align="center">
            <template #default="{row}">
              <span class="text-[14px] text-[#8595A4] font-medium">{{ row.minStock || 0 }} - {{ row.maxStock || 0 }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Trạng thái" width="140" align="center">
            <template #default="{row}">
              <el-tag :class="row.status === 'active' ? 'meta-tag-success' : 'meta-tag-info'">
                {{ row.status?.toUpperCase() }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="Thao tác" width="160" align="center" fixed="right">
            <template #default="scope">
              <div class="flex justify-center gap-2">
                <el-button class="meta-btn-secondary-sm" @click="openEditDialog(scope.row)">Sửa</el-button>
                <el-button class="meta-btn-danger-sm" @click="handleStopSelling(scope.row.productId)">Ngừng bán</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-8 flex justify-end">
          <el-pagination background layout="prev, pager, next" :total="total" @current-change="(p) => { page = p-1; fetchData(); }" />
        </div>
      </el-card>

      <el-dialog v-model="dialogVisible" :title="isEditing ? 'Sửa sản phẩm' : 'Thêm sản phẩm mới'" width="700px" class="meta-dialog" destroy-on-close>
        <div class="flex flex-col gap-5 pt-2">
          <div class="grid grid-cols-2 gap-6">
            <div class="flex flex-col gap-2">
              <label class="text-[14px] font-bold text-[#1C2B33]">Mã sản phẩm</label>
              <input v-model="form.productCode" class="meta-native-input" placeholder="VD: SP001" />
            </div>
            <div class="flex flex-col gap-2">
              <label class="text-[14px] font-bold text-[#1C2B33]">Tên sản phẩm</label>
              <input v-model="form.productName" class="meta-native-input" placeholder="Nhập tên sản phẩm" />
            </div>
          </div>
          
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Danh mục phân loại</label>
            <el-tree-select v-model="form.categoryId" :data="categories" :props="{ label: 'categoryName', value: 'categoryId' }" check-strictly class="meta-input w-full" placeholder="Chọn danh mục" />
          </div>
          
          <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div class="flex flex-col gap-2"><label class="text-[14px] font-bold text-[#1C2B33]">Đơn vị</label><input v-model="form.unit" class="meta-native-input" placeholder="Cái, Hộp..." /></div>
            <div class="flex flex-col gap-2"><label class="text-[14px] font-bold text-[#1C2B33]">Giá bán (₫)</label><el-input-number v-model="form.price" :min="0" :controls="false" class="meta-input-number w-full" /></div>
            <div class="flex flex-col gap-2"><label class="text-[14px] font-bold text-[#1C2B33]">SKU</label><input v-model="form.sku" class="meta-native-input" placeholder="Tự tạo nếu trống" /></div>
            <div class="flex flex-col gap-2"><label class="text-[14px] font-bold text-[#1C2B33]">Mã vạch</label><input v-model="form.barcode" class="meta-native-input" placeholder="Quét barcode" /></div>
          </div>

          <div class="bg-[#F7F8FA] p-6 rounded-[16px] border border-[#DEE3E9] mt-2">
            <h4 class="text-[16px] font-bold text-[#1C2B33] mb-4 flex items-center gap-2"><span>📍</span> Cảnh báo định mức tồn kho</h4>
            <div class="grid grid-cols-2 gap-6">
              <div class="flex flex-col gap-2">
                <label class="text-[14px] font-bold text-[#5D6C7B]">Tồn kho Tối thiểu (Min)</label>
                <el-input-number v-model="form.minStock" :min="0" class="meta-input-number w-full" />
                <span class="text-[12px] text-[#8595A4] leading-tight">Cảnh báo "Sắp hết hàng" nếu rớt xuống dưới mức này.</span>
              </div>
              <div class="flex flex-col gap-2">
                <label class="text-[14px] font-bold text-[#5D6C7B]">Tồn kho Tối đa (Max)</label>
                <el-input-number v-model="form.maxStock" :min="0" class="meta-input-number w-full" />
                <span class="text-[12px] text-[#8595A4] leading-tight">Cảnh báo "Vượt mức". Để 0 nếu không giới hạn.</span>
              </div>
            </div>
          </div>
        </div>
        <template #footer>
          <div class="flex gap-3 justify-end pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary" @click="dialogVisible = false">Hủy bỏ</el-button>
            <el-button class="meta-btn-primary" @click="handleSave">Xác nhận lưu</el-button>
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

const products = ref([]);
const categories = ref([]);
const isLoading = ref(false);
const dialogVisible = ref(false);
const isEditing = ref(false);
const page = ref(0);
const total = ref(0);

const form = ref({ productId: null, categoryId: null, productCode: '', productName: '', unit: '', price: 0, barcode: '', sku: '', imageUrl: '', minStock: 0, maxStock: 0 });

const fetchData = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/manager/products', { params: { page: page.value, size: 10 } });
    products.value = res.data.data.content || res.data.data;
    total.value = res.data.data.totalElements || products.value.length;

    const resCat = await api.get('/manager/categories/tree');
    categories.value = resCat.data.data;
  } catch (error) {
    ElMessage.error('Lỗi tải dữ liệu. Xem log console.');
  } finally {
    isLoading.value = false;
  }
};

const openAddDialog = () => {
  isEditing.value = false; 
  form.value = { productId: null, categoryId: null, productCode: '', productName: '', unit: '', price: 0, barcode: '', sku: '', minStock: 0, maxStock: 0 }; 
  dialogVisible.value = true;
};

const openEditDialog = (row) => {
  isEditing.value = true; 
  form.value = { ...row }; 
  dialogVisible.value = true;
};

const handleSave = async () => {
  if (form.value.maxStock > 0 && form.value.minStock >= form.value.maxStock) {
    return ElMessage.warning('Tồn kho tối đa (Max) phải lớn hơn Tồn kho tối thiểu (Min)');
  }
  if (!form.value.barcode || form.value.barcode.trim() === '') form.value.barcode = 'BC-' + Date.now();
  if (!form.value.sku || form.value.sku.trim() === '') form.value.sku = 'SKU-' + Date.now();

  try {
    if (isEditing.value) {
      await api.put(`/manager/products/${form.value.productId}`, form.value);
      ElMessage.success('Cập nhật thành công');
    } else {
      await api.post('/manager/products', form.value);
      ElMessage.success('Thêm sản phẩm thành công');
    }
    dialogVisible.value = false;
    fetchData();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Lỗi lưu sản phẩm');
  }
};

const handleStopSelling = (id) => {
  ElMessageBox.confirm('Bạn có chắc chắn muốn ngừng kinh doanh sản phẩm này?', 'Xác nhận', { type: 'warning', confirmButtonText: 'Ngừng bán', cancelButtonText: 'Hủy' }).then(async () => {
    await api.delete(`/manager/products/${id}`);
    ElMessage.success('Đã ngừng kinh doanh');
    fetchData();
  }).catch(() => {});
};

const handleImportExcel = async (file) => {
  const formData = new FormData();
  formData.append('file', file.raw);
  try {
    await api.post('/manager/products/import', formData);
    ElMessage.success('Import thành công');
    fetchData();
  } catch (error) {
    ElMessage.error('Lỗi file excel không đúng định dạng');
  }
};

onMounted(fetchData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }

:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }
:deep(.meta-btn-secondary) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-secondary:hover) { background-color: rgba(70, 90, 105, 0.05) !important; }

:deep(.meta-btn-secondary-sm) { background-color: rgba(0, 100, 224, 0.1) !important; color: #0064E0 !important; border: none !important; border-radius: 100px !important; font-weight: 600; padding: 6px 16px !important; transition: all 0.2s ease; }
:deep(.meta-btn-danger-sm) { background-color: rgba(228, 30, 63, 0.1) !important; color: #E41E3F !important; border: none !important; border-radius: 100px !important; font-weight: 600; padding: 6px 16px !important; transition: all 0.2s ease; }

.meta-native-input { width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid #CED0D4; font-family: 'Montserrat', sans-serif; font-size: 14px; color: #1C2B33; transition: border-color 0.2s, box-shadow 0.2s; }
.meta-native-input:focus { outline: none; border-color: #0064E0; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2); }

:deep(.meta-input .el-input__wrapper), :deep(.meta-input-number .el-input__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; padding: 8px 12px; }
:deep(.meta-input .el-input__wrapper.is-focus), :deep(.meta-input-number .el-input__wrapper.is-focus) { border-color: #0064E0 !important; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 20px 0 !important; }

:deep(.meta-tag-success), :deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; border: none; padding: 4px 12px; height: auto; }
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-info) { background-color: #F1F4F7; color: #5D6C7B; }

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) { background-color: #0064E0 !important; border-radius: 8px; }
:deep(.meta-dialog) { border-radius: 20px !important; font-family: 'Montserrat', sans-serif; }
:deep(.meta-dialog .el-dialog__header) { border-bottom: 1px solid #DEE3E9; padding-bottom: 16px; margin-right: 0; }
:deep(.meta-dialog .el-dialog__title) { font-weight: 600; color: #1C2B33; }
</style>