<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Quản Trị Tồn Kho Chi Tiết</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Giám sát tồn kho khả dụng và cảnh báo định mức</p>
        </div>
        <el-button class="meta-btn-outline" @click="fetchInventory" :loading="isLoading">
          <span class="mr-2 text-[16px]">↻</span> Làm mới dữ liệu
        </el-button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <el-card shadow="never" class="meta-stat-card border-t-[4px] border-t-[#0064E0]">
          <div class="text-[14px] font-bold text-[#5D6C7B] mb-2 uppercase tracking-wide">Tổng Mã Sản Phẩm</div>
          <div class="text-[40px] font-medium text-[#0064E0] leading-none">{{ totalProducts }}</div>
        </el-card>
        
        <el-card shadow="never" class="meta-stat-card border-t-[4px] border-t-[#E41E3F]">
          <div class="text-[14px] font-bold text-[#5D6C7B] mb-2 uppercase tracking-wide">Sắp hết hàng (Dưới Min)</div>
          <div class="text-[40px] font-medium text-[#E41E3F] leading-none">{{ lowStockCount }}</div>
        </el-card>

        <el-card shadow="never" class="meta-stat-card border-t-[4px] border-t-[#F7B928]">
          <div class="text-[14px] font-bold text-[#5D6C7B] mb-2 uppercase tracking-wide">Vượt định mức (Trên Max)</div>
          <div class="text-[40px] font-medium text-[#F7B928] leading-none">{{ overStockCount }}</div>
        </el-card>

        <el-card shadow="never" class="meta-stat-card border-t-[4px] border-t-[#6441D2]">
          <div class="text-[14px] font-bold text-[#5D6C7B] mb-2 uppercase tracking-wide">Đang bị giữ chỗ</div>
          <div class="text-[40px] font-medium text-[#6441D2] leading-none">{{ reservedCount }}</div>
        </el-card>
      </div>

      <el-card shadow="never" class="meta-card mb-8">
        <div class="flex flex-wrap gap-4 items-center">
          <el-input v-model="searchQuery" placeholder="Tìm tên hoặc mã SP..." class="meta-input w-72" clearable>
            <template #prefix><span class="text-[#65676B]">🔍</span></template>
          </el-input>
          
          <el-select v-model="selectedCategory" placeholder="Lọc theo danh mục" clearable class="meta-input w-56" @change="fetchInventory">
            <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
          </el-select>

          <el-select v-if="isAdmin" v-model="selectedBranch" placeholder="Tổng toàn hệ thống" clearable class="meta-input w-64" @change="fetchInventory">
            <el-option label="-- Tổng toàn hệ thống --" value="ALL" />
            <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
          </el-select>
        </div>
      </el-card>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="filteredInventory" v-loading="isLoading" class="meta-table" height="600" style="width: 100%">
          <el-table-column label="Mã SP" prop="productCode" width="120">
            <template #default="{row}">
              <span class="text-[#5D6C7B] font-mono bg-[#F1F4F7] px-2 py-1 rounded-[6px]">{{ row.productCode }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Sản phẩm" min-width="250">
            <template #default="{row}">
              <div class="font-medium text-[#1C2B33] text-[16px]">{{ row.productName }}</div>
              <div class="text-[14px] text-[#5D6C7B]">{{ row.categoryName || 'Không phân loại' }}</div>
            </template>
          </el-table-column>

          <el-table-column label="Tồn Hệ Thống" width="150" align="center">
            <template #default="{row}">
              <span class="text-[#1C2B33] font-medium text-[16px]">{{ row.quantity }}</span>
              <span class="text-[#5D6C7B] text-[14px] ml-1">{{ row.unit }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Đang Giữ Chỗ" width="150" align="center">
            <template #default="{row}">
              <el-tag v-if="row.reservedQuantity > 0" class="meta-tag-warning">- {{ row.reservedQuantity }}</el-tag>
              <span v-else class="text-[#CED0D4]">—</span>
            </template>
          </el-table-column>

          <el-table-column label="Khả Dụng" width="160" align="center">
            <template #default="{row}">
              <span class="text-[20px] font-bold" :class="getAvailableColorClass(row)">{{ row.availableQuantity }}</span>
              <span class="text-[#5D6C7B] text-[14px] ml-1">{{ row.unit }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Min - Max" width="120" align="center">
            <template #default="{row}">
              <span class="text-[14px] text-[#8595A4]">{{ row.minThreshold }} - {{ row.maxThreshold }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Trạng thái" width="160" align="center" fixed="right">
            <template #default="{row}">
              <el-tag :class="getStatusTagClass(row.alertStatus)">
                {{ getStatusLabel(row.alertStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Lịch sử" width="100" align="center" fixed="right">
            <template #default="{row}">
              <el-button class="meta-btn-secondary-sm w-10 h-10 p-0 rounded-full flex items-center justify-center" @click="viewHistory(row.productId)" title="Xem lịch sử">
                🕒
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/api/axios.js';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth'; 

const router = useRouter();
const auth = useAuthStore();

const isAdmin = computed(() => auth.role === 'ADMIN' || auth.role === 'ROLE_ADMIN');
const inventory = ref([]);
const categories = ref([]);
const branches = ref([]);

const isLoading = ref(false);
const searchQuery = ref('');
const selectedCategory = ref(null);
const selectedBranch = ref(isAdmin.value ? 'ALL' : auth.user?.branchId);

const filteredInventory = computed(() => {
  if (!searchQuery.value) return inventory.value;
  const lowerQuery = searchQuery.value.toLowerCase();
  return inventory.value.filter(item => 
    item.productName?.toLowerCase().includes(lowerQuery) || 
    item.productCode?.toLowerCase().includes(lowerQuery)
  );
});

const totalProducts = computed(() => inventory.value.length);
const lowStockCount = computed(() => inventory.value.filter(i => i.alertStatus === 'LOW_STOCK' || i.alertStatus === 'OUT_OF_STOCK').length);
const overStockCount = computed(() => inventory.value.filter(i => i.alertStatus === 'OVER_STOCK').length);
const reservedCount = computed(() => inventory.value.filter(i => i.reservedQuantity > 0).length);

const fetchCategoriesAndBranches = async () => {
  try {
    const resCat = await api.get('/manager/categories');
    categories.value = resCat.data?.data || [];
    
    if (isAdmin.value) {
      const resBranch = await api.get('/admin/branches/active');
      branches.value = resBranch.data?.data || [];
    }
  } catch (error) {
    console.error("Lỗi tải danh mục / chi nhánh");
  }
};

const fetchInventory = async () => {
  isLoading.value = true;
  try {
    const params = {};
    if (selectedCategory.value) params.categoryId = selectedCategory.value;
    if (selectedBranch.value !== 'ALL') params.branchId = selectedBranch.value;

    const res = await api.get('/manager/inventory/advanced-report', { params });
    inventory.value = res.data?.data || [];
  } catch (error) {
    ElMessage.error('Lỗi tải dữ liệu tồn kho');
  } finally {
    isLoading.value = false;
  }
};

const viewHistory = (productId) => {
  const bId = selectedBranch.value === 'ALL' ? null : selectedBranch.value;
  router.push({ path: '/manager/inventory-history', query: { productId: productId, branchId: bId } }); 
};

// UI Helpers mapped to Meta tokens
const getAvailableColorClass = (row) => {
  if (row.alertStatus === 'NEGATIVE_STOCK') return 'text-[#E41E3F]';
  if (row.alertStatus === 'OUT_OF_STOCK' || row.alertStatus === 'LOW_STOCK') return 'text-[#D6311F]';
  if (row.alertStatus === 'OVER_STOCK') return 'text-[#F7B928]'; 
  return 'text-[#31A24C]';
};

const getStatusTagClass = (status) => {
  switch (status) {
    case 'NEGATIVE_STOCK': 
    case 'OUT_OF_STOCK': return 'meta-tag-error';
    case 'LOW_STOCK': 
    case 'OVER_STOCK': return 'meta-tag-warning';
    default: return 'meta-tag-success';
  }
};

const getStatusLabel = (status) => {
  switch (status) {
    case 'NEGATIVE_STOCK': return 'Âm Kho';
    case 'OUT_OF_STOCK': return 'Hết hàng';
    case 'LOW_STOCK': return 'Dưới Min';
    case 'OVER_STOCK': return 'Vượt Max'; 
    default: return 'Bình thường';
  }
};

onMounted(() => {
  fetchCategoriesAndBranches();
  fetchInventory();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-stat-card) { border-radius: 20px !important; border-left: none; border-right: none; border-bottom: none; background-color: #FFFFFF !important; padding: 24px !important; box-shadow: 0 4px 12px rgba(0,0,0,0.02) !important; }
:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }

:deep(.meta-btn-outline) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 22px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-outline:hover) { background-color: rgba(70, 90, 105, 0.05) !important; }
:deep(.meta-btn-secondary-sm) { background-color: rgba(0, 100, 224, 0.1) !important; color: #0064E0 !important; border: none !important; transition: all 0.2s ease; }
:deep(.meta-btn-secondary-sm:hover) { background-color: rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; padding: 8px 12px; }
:deep(.meta-input .el-input__wrapper.is-focus), :deep(.meta-input .el-select__wrapper.is-focus) { border-color: #0064E0 !important; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 20px 0 !important; }

:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error) { border-radius: 100px; font-weight: 700; border: none; padding: 6px 16px; height: auto; }
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
</style>