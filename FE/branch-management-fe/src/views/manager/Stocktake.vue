<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Kiểm Kê Kho Định Kỳ</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Đối soát số lượng thực tế tại quầy và cân bằng với số liệu hệ thống</p>
        </div>
        <div class="flex gap-4 items-center">
          <el-select v-if="isAdmin" v-model="selectedBranch" placeholder="Chọn chi nhánh" class="meta-input w-56" @change="fetchCurrentStock">
            <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
          </el-select>
          <el-button class="meta-btn-secondary" @click="fetchCurrentStock">
            <span class="mr-2">↻</span> Tải lại tồn kho máy
          </el-button>
          <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="handleComplete" :loading="submitting">
            <span class="mr-2">✓</span> Hoàn tất & Cân bằng
          </el-button>
        </div>
      </div>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <div class="mb-6 text-[14px] text-[#5D6C7B] bg-[#E8F3FF] border border-[#ADD4E0] p-4 rounded-[12px] flex items-center gap-2">
          <span>ℹ️</span> Nhập số lượng đếm được thực tế vào cột <b>"Đếm thực tế"</b>. Hệ thống sẽ tự động đối soát và phát sinh phiếu bù trừ.
        </div>

        <el-table :data="stockItems" v-loading="loading" class="meta-table" style="width: 100%">
          <el-table-column label="Sản phẩm" min-width="300">
            <template #default="{row}">
              <div class="font-medium text-[#1C2B33] text-[16px]">{{ row.productName }}</div>
              <div class="text-[14px] text-[#5D6C7B] font-mono mt-1">Mã: {{ row.productCode }}</div>
            </template>
          </el-table-column>

          <el-table-column label="Đơn vị" width="120" align="center" prop="unit">
            <template #default="{row}"><span class="text-[#5D6C7B]">{{ row.unit }}</span></template>
          </el-table-column>

          <el-table-column label="Tồn sổ sách (A)" width="160" align="center">
            <template #default="{row}">
              <span class="text-[#1C2B33] font-bold text-[18px]">{{ row.systemQuantity }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Đếm thực tế (B)" width="200" align="center">
            <template #default="{row}">
              <el-input-number v-model="row.actualQuantity" :min="0" :controls="false" class="meta-input-number w-full text-center" @change="calculateDiff(row)" />
            </template>
          </el-table-column>

          <el-table-column label="Chênh lệch (B-A)" width="180" align="center">
            <template #default="{row}">
              <span class="text-[20px] font-black" :class="getDiffClass(row.diff)">
                {{ row.diff > 0 ? '+' : '' }}{{ row.diff }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="Lý do sai lệch" min-width="250">
            <template #default="{row}">
              <input v-model="row.note" class="meta-native-input w-full" placeholder="Ví dụ: Hàng móp méo, đếm sót..." />
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '@/api/axios';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const isAdmin = computed(() => auth.role === 'ADMIN' || auth.role === 'ROLE_ADMIN');

const stockItems = ref([]);
const branches = ref([]);
const selectedBranch = ref(auth.user?.branchId || null);

const loading = ref(false);
const submitting = ref(false);

const fetchBranches = async () => {
  if (isAdmin.value) {
    try {
      const res = await api.get('/admin/branches/active');
      branches.value = res.data?.data || [];
      if (!selectedBranch.value && branches.value.length > 0) {
        selectedBranch.value = branches.value[0].branchId;
      }
    } catch (e) { console.error(e); }
  }
};

const fetchCurrentStock = async () => {
  if (!selectedBranch.value && !isAdmin.value) {
    selectedBranch.value = auth.user?.branchId;
  }
  
  loading.value = true;
  try {
    const res = await api.get('/manager/inventory-detail', {
      params: { branchId: selectedBranch.value }
    });
    stockItems.value = res.data?.data.map(item => ({
      ...item,
      actualQuantity: item.quantity, 
      systemQuantity: item.quantity,
      diff: 0,
      note: ''
    })) || [];
  } catch (error) {
    ElMessage.error('Không thể tải dữ liệu tồn kho');
  } finally {
    loading.value = false;
  }
};

const calculateDiff = (row) => {
  row.diff = row.actualQuantity - row.systemQuantity;
};

const getDiffClass = (diff) => {
  if (diff > 0) return 'text-[#31A24C]'; // Xanh lá nếu thừa kho
  if (diff < 0) return 'text-[#E41E3F]'; // Đỏ nếu mất/thiếu hàng
  return 'text-[#CED0D4]';               // Xám nếu khớp
};

const handleComplete = async () => {
  if (stockItems.value.length === 0) return;

  try {
    await ElMessageBox.confirm(
      'Hệ thống sẽ tự động chốt số liệu và tạo các phiếu xuất/nhập phụ để cân bằng kho theo số đếm thực tế. Bạn chắc chắn chứ?',
      'Xác nhận chốt kiểm kê',
      { confirmButtonText: 'Chốt & Cân bằng', cancelButtonText: 'Hủy', type: 'warning' }
    );

    submitting.value = true;
    await api.post('/manager/stock/stocktake/complete', {
      branchId: selectedBranch.value,
      details: stockItems.value.map(i => ({
        productId: i.productId,
        systemQuantity: i.systemQuantity,
        actualQuantity: i.actualQuantity,
        note: i.note
      }))
    });

    ElMessage.success('Đã hoàn tất quy trình kiểm kê định kỳ!');
    fetchCurrentStock(); 
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Lỗi khi xử lý: ' + (error.response?.data?.message || error.message));
    }
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  await fetchBranches();
  fetchCurrentStock();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 24px; }

:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }
:deep(.meta-btn-secondary) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-secondary:hover) { background-color: rgba(70, 90, 105, 0.05) !important; }

.meta-native-input { padding: 10px 14px; border-radius: 8px; border: 1px solid #CED0D4; font-family: 'Montserrat', sans-serif; font-size: 14px; color: #1C2B33; transition: border-color 0.2s, box-shadow 0.2s; }
.meta-native-input:focus { outline: none; border-color: #0064E0; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2); }

:deep(.meta-input-number .el-input__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; padding: 8px 12px; }
:deep(.meta-input-number .el-input__wrapper.is-focus) { border-color: #0064E0 !important; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important; }
:deep(.meta-input-number .el-input__inner) { font-family: 'Montserrat', sans-serif !important; font-weight: bold; font-size: 16px; color: #0064E0; }

:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 16px 0 !important; }
</style>