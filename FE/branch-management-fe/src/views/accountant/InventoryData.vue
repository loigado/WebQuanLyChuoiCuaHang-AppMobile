<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Dữ liệu kho toàn hệ thống</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Quản lý và theo dõi số lượng, giá trị tồn kho của kế toán</p>
        </div>
        <el-button type="primary" class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="handleExport">
          <span class="mr-2 text-[16px]">📦</span> Xuất báo cáo tài chính
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card mb-8">
        <div class="flex flex-wrap gap-6 items-center">
          <span class="text-[14px] font-bold text-[#1C2B33]">Bộ lọc:</span>
          <el-select 
            v-model="selectedBranch" 
            placeholder="Tất cả chi nhánh" 
            clearable 
            class="w-64 meta-input"
            @change="fetchData"
          >
            <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
          </el-select>
          <el-input 
            v-model="search" 
            placeholder="Tìm kiếm theo tên sản phẩm..." 
            class="w-80 meta-input"
            clearable
          >
            <template #prefix><span class="text-[#65676B]">🔍</span></template>
          </el-input>
        </div>
      </el-card>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table 
          :data="inventory" 
          style="width: 100%" 
          v-loading="loading"
          class="meta-table"
        >
          <el-table-column prop="branchName" label="Chi nhánh" width="220">
            <template #default="{row}">
              <span class="text-[#5D6C7B]">{{ row.branchName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="Tên sản phẩm" min-width="280">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.productName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="Tồn kho" width="140" align="center">
            <template #default="{row}">
              <el-tag 
                :class="row.quantity > 10 ? 'meta-tag-info' : 'meta-tag-error'"
                effect="plain"
              >
                {{ row.quantity }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="unitPrice" label="Giá vốn" width="180" align="right">
            <template #default="{row}">
              <span class="text-[#5D6C7B]">{{ row.unitPrice?.toLocaleString() }} ₫</span>
            </template>
          </el-table-column>
          <el-table-column label="Thành tiền" width="200" align="right">
            <template #default="{row}">
              <span class="font-medium text-[#1C2B33]">{{ (row.quantity * row.unitPrice)?.toLocaleString() }} ₫</span>
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

const inventory = ref([]);
const branches = ref([]);
const selectedBranch = ref(null);
const search = ref('');
const loading = ref(false);

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await api.get('/accountant/inventory', { params: { branchId: selectedBranch.value } });
    inventory.value = res.data.data;
    
    const resBranch = await api.get('/admin/branches/active');
    branches.value = resBranch.data.data;
  } catch (error) {
    console.error("Lỗi tải dữ liệu kế toán:", error);
  } finally {
    loading.value = false;
  }
};

const handleExport = () => {
  console.log("Xuất báo cáo");
};

onMounted(fetchData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');

.font-meta {
  font-family: 'Montserrat', sans-serif;
}

/* Button Overrides */
:deep(.meta-btn-primary) {
  border-radius: 100px !important;
  background-color: #0064E0 !important;
  border-color: #0064E0 !important;
  color: #FFFFFF !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 14px;
  padding: 10px 22px !important;
  height: auto;
  letter-spacing: -0.14px;
  transition: all 0.2s ease;
}
:deep(.meta-btn-primary:hover) {
  background-color: #0143B5 !important;
  transform: scale(1.02);
}
:deep(.meta-btn-primary:active) {
  background-color: #004BB9 !important;
  transform: scale(0.98);
}

/* Card Overrides */
:deep(.meta-card) {
  border-radius: 20px !important;
  border: none !important;
  background-color: #FFFFFF !important;
  padding: 10px;
}

/* Input Overrides */
:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper) {
  border-radius: 8px !important;
  border: 1px solid #CED0D4 !important;
  box-shadow: none !important;
  font-family: 'Montserrat', sans-serif;
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
:deep(.meta-tag-info), :deep(.meta-tag-error) {
  border-radius: 100px;
  font-weight: 700;
  padding: 0 16px;
  border: none;
}
:deep(.meta-tag-info) {
  background-color: rgba(0, 145, 255, 0.15);
  color: #0064E0;
}
:deep(.meta-tag-error) {
  background-color: rgba(255, 123, 145, 0.15);
  color: #E41E3F;
}
</style>