<template>
  <div class="inventory-enterprise" v-loading="loading">
    <!-- Header with Stats -->
    <div class="dashboard-header mb-10">
      <div class="welcome-section">
        <h2 class="text-3xl font-bold text-slate-900">Điều phối Kho Hệ thống</h2>
        <p class="text-slate-500 mt-1">Dữ liệu hợp nhất từ {{ branchSummary.length }} chi nhánh trên toàn quốc</p>
      </div>
      <div class="header-actions">
        <el-button @click="fetchData" class="refresh-btn">
          <i class="mr-2">🔄</i> Cập nhật dữ liệu
        </el-button>
        <el-button type="primary" class="export-btn">
          <i>📥</i> Xuất báo cáo Excel
        </el-button>
      </div>
    </div>

    <!-- Quick Insights -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
      <div v-for="item in branchSummary" :key="item.branchId" class="stat-card">
        <div class="stat-icon">🏢</div>
        <div class="stat-info">
          <span class="stat-label">{{ item.branchName }}</span>
          <h3 class="stat-value">{{ item.totalQuantity.toLocaleString() }} <small class="unit">đv</small></h3>
          <div class="stat-progress">
            <div class="progress-bar" :style="{ width: calculateProgress(item.totalQuantity) + '%' }"></div>
          </div>
        </div>
      </div>
      
      <!-- Total Aggregate Card -->
      <div class="stat-card total-highlight">
        <div class="stat-icon bg-blue-500 text-white">📦</div>
        <div class="stat-info">
          <span class="stat-label text-blue-100">Tổng tồn kho hệ thống</span>
          <h3 class="stat-value text-white">{{ totalSystemQuantity.toLocaleString() }}</h3>
          <p class="text-blue-100 text-xs mt-2">Đang phân bổ tại {{ branches.length }} kho</p>
        </div>
      </div>
    </div>

    <!-- Detailed Inventory Section -->
    <div class="data-section">
      <div class="section-header">
        <div class="flex items-center gap-4">
          <h3 class="text-xl font-bold text-slate-800">Danh mục tồn kho chi tiết</h3>
          <el-tag type="info" effect="plain" round>{{ inventoryDetails.length }} sản phẩm</el-tag>
        </div>
        
        <div class="filter-group">
          <el-select v-model="selectedBranch" placeholder="Chọn chi nhánh" clearable class="enterprise-select" @change="fetchDetails">
            <template #prefix>📍</template>
            <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
          </el-select>
          
          <div class="search-wrapper">
             <i>🔍</i>
             <input type="text" v-model="searchQuery" placeholder="Tìm tên hoặc mã SP..." />
          </div>
        </div>
      </div>

      <el-table 
        :data="filteredDetails" 
        class="enterprise-table"
        stripe
      >
        <el-table-column prop="productCode" label="Mã Định Danh" width="160">
          <template #default="{row}">
            <span class="code-badge">{{ row.productCode }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="Thông tin Sản phẩm" min-width="300">
          <template #default="{row}">
            <div class="product-info">
              <div class="name">{{ row.productName }}</div>
              <div class="branch-tag">{{ row.branchName }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="Số lượng Tồn" width="200" align="right">
          <template #default="{row}">
            <div class="quantity-cell">
              <span class="val" :class="{ 'low-stock': row.quantity <= 5 }">{{ row.quantity }}</span>
              <span class="unit">{{ row.unit }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Đang chờ xuất" width="180" align="right">
          <template #default="{row}">
            <span class="reserved-val" v-if="row.reservedQuantity > 0">- {{ row.reservedQuantity }}</span>
            <span class="text-slate-300" v-else>0</span>
          </template>
        </el-table-column>

        <el-table-column label="Tình trạng" width="200" align="center">
          <template #default="{row}">
            <div :class="getStatusClass(row.quantity)">
              <span class="dot"></span>
              {{ row.quantity > 5 ? 'Đủ hàng' : 'Cần nhập thêm' }}
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/axios';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const branchSummary = ref([]);
const inventoryDetails = ref([]);
const branches = ref([]);
const selectedBranch = ref(null);
const searchQuery = ref('');

const totalSystemQuantity = computed(() => {
  return branchSummary.value.reduce((sum, item) => sum + item.totalQuantity, 0);
});

const filteredDetails = computed(() => {
  if (!searchQuery.value) return inventoryDetails.value;
  const q = searchQuery.value.toLowerCase();
  return inventoryDetails.value.filter(item => 
    item.productName.toLowerCase().includes(q) || 
    item.productCode.toLowerCase().includes(q)
  );
});

const calculateProgress = (val) => {
  const max = Math.max(...branchSummary.value.map(i => i.totalQuantity), 1);
  return (val / max) * 100;
};

const getStatusClass = (qty) => {
  return qty > 5 ? 'status-pill success' : 'status-pill warning';
};

const fetchData = async () => {
  loading.value = true;
  try {
    const branchesRes = await api.get('/admin/branches/active').catch(() => null);
    branches.value = branchesRes?.data?.data || [];

    const summaryRes = await api.get('/admin/inventory/summary-by-branch');
    branchSummary.value = summaryRes.data?.data || [];
    
    await fetchDetails();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "Lỗi tải dữ liệu");
  } finally {
    loading.value = false;
  }
};

const fetchDetails = async () => {
  try {
    const res = await api.get('/admin/inventory/details', {
      params: { branchId: selectedBranch.value }
    });
    inventoryDetails.value = res.data?.data || [];
  } catch (error) {
    console.error(error);
  }
};

onMounted(fetchData);
</script>

<style scoped>
.inventory-enterprise {
  animation: fadeIn 0.5s ease-out;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions { display: flex; gap: 12px; }

.refresh-btn { border-radius: 10px; font-weight: 600; color: #64748B; }
.export-btn { border-radius: 10px; font-weight: 600; background-color: #0064E0; }

/* STAT CARDS */
.stat-card {
  background: white;
  padding: 24px;
  border-radius: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 20px;
  border: 1px solid #F1F5F9;
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-4px); }

.total-highlight {
  background: linear-gradient(135deg, #0064E0 0%, #0046B5 100%);
  border: none;
}

.stat-icon {
  width: 56px;
  height: 56px;
  background: #F1F5F9;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-info { flex: 1; }
.stat-label { font-size: 13px; font-weight: 600; color: #94A3B8; }
.stat-value { font-size: 24px; font-weight: 800; color: #0F172A; margin: 4px 0; }
.unit { font-size: 12px; font-weight: 500; color: #94A3B8; }

.stat-progress {
  height: 6px;
  background: #F1F5F9;
  border-radius: 10px;
  margin-top: 12px;
  overflow: hidden;
}
.progress-bar { height: 100%; background: #0064E0; border-radius: 10px; }

/* DATA SECTION */
.data-section {
  background: white;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.04);
  border: 1px solid #F1F5F9;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.filter-group { display: flex; gap: 16px; }

.enterprise-select { width: 220px; }
:deep(.el-input__wrapper) { border-radius: 12px !important; background: #F8FAFC !important; box-shadow: none !important; border: 1px solid #E2E8F0 !important; }

.search-wrapper {
  position: relative;
  width: 280px;
}
.search-wrapper i { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #94A3B8; }
.search-wrapper input {
  width: 100%;
  padding: 10px 16px 10px 40px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  font-size: 14px;
}

/* TABLE STYLES */
.enterprise-table {
  border-radius: 16px;
  overflow: hidden;
}

.code-badge {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  font-weight: 700;
  color: #0064E0;
  background: #EEF2FF;
  padding: 4px 10px;
  border-radius: 6px;
}

.product-info .name { font-weight: 600; color: #0F172A; }
.product-info .branch-tag { font-size: 12px; color: #64748B; margin-top: 2px; }

.quantity-cell .val { font-size: 18px; font-weight: 700; color: #0F172A; }
.quantity-cell .low-stock { color: #EF4444; }
.quantity-cell .unit { font-size: 12px; color: #94A3B8; margin-left: 4px; }

.reserved-val { color: #F97316; font-weight: 600; font-size: 14px; }

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 600;
}
.status-pill.success { background: #F0FDF4; color: #16A34A; }
.status-pill.warning { background: #FEF2F2; color: #DC2626; }
.status-pill .dot { width: 6px; height: 6px; border-radius: 50%; }
.success .dot { background: #16A34A; }
.warning .dot { background: #DC2626; }

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
