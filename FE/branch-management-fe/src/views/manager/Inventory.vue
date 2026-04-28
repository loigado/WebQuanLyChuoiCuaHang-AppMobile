<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-10 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Quản Trị Tồn Kho Chi Tiết</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Giám sát tồn kho khả dụng và cảnh báo định mức hàng hóa</p>
        </div>
        <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="fetchInventory" :loading="isLoading">
          <span class="mr-2 text-[16px]">↻</span> Làm mới dữ liệu
        </el-button>
      </div>

      <el-row :gutter="24" class="mb-10">
        <el-col :xs="24" :sm="12" :lg="6" class="mb-6 lg:mb-0">
          <el-card shadow="never" class="meta-stat-card border-l-[6px] border-l-[#0064E0]">
            <div class="flex flex-col justify-center h-full">
              <span class="text-[12px] font-bold text-[#5D6C7B] uppercase tracking-wider mb-1">Tổng mặt hàng</span>
              <span class="text-[32px] font-medium text-[#1C2B33] leading-none">{{ totalProducts }}</span>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :lg="6" class="mb-6 lg:mb-0">
          <el-card shadow="never" class="meta-stat-card border-l-[6px] border-l-[#E41E3F]">
            <div class="flex flex-col justify-center h-full">
              <span class="text-[12px] font-bold text-[#5D6C7B] uppercase tracking-wider mb-1">Cảnh báo: Dưới Min</span>
              <span class="text-[32px] font-medium text-[#E41E3F] leading-none">{{ lowStockCount }}</span>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :lg="6" class="mb-6 lg:mb-0">
          <el-card shadow="never" class="meta-stat-card border-l-[6px] border-l-[#F7B928]">
            <div class="flex flex-col justify-center h-full">
              <span class="text-[12px] font-bold text-[#5D6C7B] uppercase tracking-wider mb-1">Vượt Max định mức</span>
              <span class="text-[32px] font-medium text-[#F7B928] leading-none">{{ overStockCount }}</span>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="never" class="meta-stat-card border-l-[6px] border-l-[#7B61FF]">
            <div class="flex flex-col justify-center h-full">
              <span class="text-[12px] font-bold text-[#5D6C7B] uppercase tracking-wider mb-1">Hàng đang giữ chỗ</span>
              <span class="text-[32px] font-medium text-[#7B61FF] leading-none">{{ reservedCount }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="meta-card mb-6">
        <div class="flex flex-wrap gap-4 items-center">
          <span class="text-[14px] font-bold text-[#1C2B33]">Bộ lọc:</span>
          
          <el-select v-if="isAdmin" v-model="selectedBranch" placeholder="Chọn chi nhánh" clearable class="meta-input w-64" @change="fetchInventory">
            <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
          </el-select>

          <el-select v-model="selectedCategory" placeholder="Tất cả danh mục" clearable class="meta-input w-56" @change="fetchInventory">
            <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
          </el-select>

          <el-input v-model="searchQuery" placeholder="Tìm tên hoặc mã SP..." class="meta-input w-72" clearable>
            <template #prefix><span class="text-[#65676B]">🔍</span></template>
          </el-input>
        </div>
      </el-card>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="filteredInventory" v-loading="isLoading" class="meta-table" style="width: 100%">
          
          <el-table-column label="Sản phẩm" min-width="250">
            <template #default="{row}">
              <div class="font-medium text-[#1C2B33] text-[16px]">{{ row.productName }}</div>
              <div class="text-[13px] text-[#5D6C7B] font-mono mt-1">Mã SP: {{ row.productCode }}</div>
            </template>
          </el-table-column>

          <el-table-column label="Danh mục" width="180">
            <template #default="{row}">
              <span class="text-[14px] text-[#5D6C7B] bg-[#F1F4F7] px-3 py-1 rounded-full">{{ row.categoryName || 'Không phân loại' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Tồn Hệ Thống" width="160" align="center">
            <template #default="{row}">
              <span class="text-[#1C2B33] font-bold text-[18px]">{{ row.quantity }}</span>
              <span class="text-[#5D6C7B] text-[13px]">&nbsp;{{ row.unit }}</span>
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
              <span class="text-[22px] font-black" :class="getAvailableColor(row)">
                {{ row.availableQuantity ?? (row.quantity - (row.reservedQuantity || 0)) }}
              </span>
              <span class="text-[#5D6C7B] text-[13px]">&nbsp;{{ row.unit }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="getStatusClass(row.alertStatus)">
                {{ getStatusLabel(row.alertStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Lịch sử" width="100" align="center" fixed="right">
            <template #default="{row}">
              <el-button class="meta-btn-primary-sm !p-2" @click="viewHistory(row.productId)">
                📊 Thẻ kho
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
import { Refresh, Search, Calendar, InfoFilled } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const auth = useAuthStore();
const isAdmin = computed(() => auth.role === 'ADMIN' || auth.role === 'ROLE_ADMIN');
const selectedBranch = ref(auth.user?.branchId);
const branches = ref([]);

// --- STATE ---
const inventory = ref([]);
const categories = ref([]);
const isLoading = ref(false);
const searchQuery = ref('');
const selectedCategory = ref(null);

// --- COMPUTED: BỘ LỌC & THỐNG KÊ ---
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

// --- FETCH DATA ---
const fetchCategories = async () => {
  try {
    const res = await api.get('/manager/categories');
    categories.value = res.data?.data || res.data?.data?.content || [];
  } catch (error) {
    console.error("Lỗi tải danh mục");
  }
};

const fetchBranches = async () => {
  if (isAdmin.value) {
    try {
      const res = await api.get('/admin/branches/active');
      branches.value = res.data?.data || [];
    } catch (e) { console.error(e); }
  }
};

const fetchInventory = async () => {
  isLoading.value = true;
  try {
    const params = {
      branchId: selectedBranch.value,
      categoryId: selectedCategory.value
    };
    const res = await api.get('/manager/inventory/advanced-report', { params });
    inventory.value = res.data?.data || [];
  } catch (error) {
    ElMessage.error('Lỗi tải dữ liệu tồn kho');
  } finally {
    isLoading.value = false;
  }
};

// --- HELPER UI ---
const getAvailableColor = (row) => {
  if (row.availableQuantity < 0) return 'text-red-600';
  if (row.alertStatus === 'LOW_STOCK' || row.alertStatus === 'OUT_OF_STOCK') return 'text-orange-500';
  if (row.alertStatus === 'OVER_STOCK') return 'text-yellow-600';
  return 'text-green-600';
};

const getStatusClass = (status) => {
  if (status === 'OUT_OF_STOCK' || status === 'CRITICAL_LOW') return 'meta-tag-error';
  if (status === 'LOW_STOCK') return 'meta-tag-warning';
  if (status === 'OVERSTOCK') return 'meta-tag-info';
  return 'meta-tag-success';
};

const getStatusLabel = (status) => {
  switch (status) {
    case 'NEGATIVE_STOCK': return 'Âm Kho (Lỗi)';
    case 'OUT_OF_STOCK': return 'Hết hàng';
    case 'LOW_STOCK': return 'Sắp hết (Dưới Min)';
    case 'OVER_STOCK': return 'Vượt mức (Trên Max)';
    default: return 'Bình thường';
  }
};

const viewHistory = (productId) => {
  // Lấy branchId từ phần tử đầu tiên của danh sách tồn kho hoặc từ thông tin User
  const branchId = inventory.value[0]?.branchId || auth.user?.branchId;
  
  router.push({ 
    path: '/manager/inventory-history', 
    query: { 
      productId: productId,
      branchId: branchId // ✅ Truyền thêm branchId để làm context tính toán +/-
    } 
  });
};

onMounted(() => {
  fetchCategories();
  fetchInventory();
});
</script>