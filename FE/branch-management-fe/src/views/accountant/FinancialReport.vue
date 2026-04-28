<template>
  <div class="fintech-dashboard" v-loading="loading">
    <!-- Header with Balance Overview -->
    <div class="header-banner">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-6 w-full">
        <div class="title-section">
          <h2 class="text-3xl font-extrabold text-white tracking-tight">Trung tâm Tài chính Kho</h2>
          <p class="text-blue-100 mt-2 flex items-center gap-2">
             <i class="opacity-70">🗓️</i> {{ dateRangeLabel }}
          </p>
        </div>
        
        <div class="period-picker">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="→"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="premium-range-picker"
            @change="fetchFinancialData"
          />
        </div>
      </div>
    </div>

    <!-- Main Balance Card -->
    <div class="main-stats-grid mb-12">
      <div class="balance-card">
        <div class="flex justify-between items-start mb-6">
          <span class="label">Tổng giá trị tồn kho hiện tại</span>
          <div class="chip">Real-time</div>
        </div>
        <div class="flex items-baseline gap-3">
          <h1 class="amount">{{ (financialData.totalInventoryValue || 0).toLocaleString() }}</h1>
          <span class="currency">VNĐ</span>
        </div>
        <div class="footer-stats mt-8">
           <div class="stat-item">
             <span class="l">Tỉ lệ xoay vòng</span>
             <span class="v">84%</span>
           </div>
           <div class="stat-item">
             <span class="l">Vốn lưu động</span>
             <span class="v text-emerald-400">Tốt</span>
           </div>
        </div>
      </div>

      <div class="transaction-summary">
        <div class="summary-box">
          <div class="icon-circle bg-emerald-100 text-emerald-600">📥</div>
          <div>
            <span class="lbl">Số lượng giao dịch</span>
            <div class="val">{{ financialData.recentTransactions?.length || 0 }}</div>
          </div>
        </div>
        <div class="summary-box">
          <div class="icon-circle bg-amber-100 text-amber-600">⚖️</div>
          <div>
            <span class="lbl">Trạng thái phiếu</span>
            <div class="val">Đã quyết toán</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Transaction Ledger -->
    <div class="ledger-section">
      <div class="section-header flex justify-between items-center mb-8">
        <h3 class="text-xl font-bold text-slate-800">Sổ cái Giao dịch Kho</h3>
        <el-button type="primary" class="export-btn-ledger" @click="exportExcel">
           📥 Tải sao kê giao dịch
        </el-button>
      </div>

      <el-table :data="financialData.recentTransactions" class="fintech-table" stripe>
        <el-table-column label="Thời gian" width="180">
          <template #default="{row}">
            <div class="date-cell">
              <span class="d">{{ formatDate(row.date) }}</span>
              <span class="t">{{ formatTime(row.date) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="Loại hình" width="160">
          <template #default="{row}">
             <span :class="getTypeClass(row.type)">
               {{ formatType(row.type) }}
             </span>
          </template>
        </el-table-column>

        <el-table-column prop="note" label="Nội dung diễn giải" min-width="250">
           <template #default="{row}">
             <span class="text-slate-600 italic text-sm">{{ row.note || 'Không có ghi chú' }}</span>
           </template>
        </el-table-column>

        <el-table-column label="Giá trị giao dịch" width="220" align="right">
          <template #default="{row}">
            <div class="amount-cell">
              <span class="prefix" :class="row.amount >= 0 ? 'text-emerald-500' : 'text-rose-500'">
                {{ row.amount >= 0 ? '+' : '' }}
              </span>
              <span class="val">{{ row.amount.toLocaleString() }}</span>
              <span class="u">đ</span>
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
const financialData = ref({});
const dateRange = ref([]);

const dateRangeLabel = computed(() => {
  if (!dateRange.value || dateRange.value.length === 0) return 'Dữ liệu trọn đời';
  return `${dateRange.value[0]} → ${dateRange.value[1]}`;
});

const fetchFinancialData = async () => {
  loading.value = true;
  try {
    const params = {};
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }
    const res = await api.get('/accountant/financial-summary', { params });
    financialData.value = res.data?.data || {};
  } catch (error) {
    ElMessage.error("Lỗi tải dữ liệu tài chính");
  } finally {
    loading.value = false;
  }
};

const formatDate = (d) => new Date(d).toLocaleDateString();
const formatTime = (d) => new Date(d).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

const formatType = (type) => {
  const maps = { 'STOCK_IN': 'Nhập kho', 'STOCK_OUT': 'Xuất kho', 'TRANSFER': 'Điều chuyển' };
  return maps[type] || type;
};

const getTypeClass = (type) => {
  if (type === 'STOCK_IN') return 'badge-fintech success';
  if (type === 'STOCK_OUT') return 'badge-fintech danger';
  return 'badge-fintech info';
};

const exportExcel = async () => {
  window.open('http://localhost:8081/api/reports/inventory-excel', '_blank');
};

onMounted(fetchFinancialData);
</script>

<style scoped>
.fintech-dashboard {
  animation: slideUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

/* HEADER BANNER */
.header-banner {
  background: linear-gradient(135deg, #0F172A 0%, #1E293B 100%);
  padding: 48px 40px;
  border-radius: 32px;
  margin-bottom: -60px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.premium-range-picker {
  border-radius: 14px !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: white !important;
}
:deep(.el-range-input) { color: white !important; background: transparent !important; }
:deep(.el-range-separator) { color: rgba(255, 255, 255, 0.5) !important; }

/* STATS GRID */
.main-stats-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
  padding: 0 20px;
  position: relative;
  z-index: 5;
}

.balance-card {
  background: white;
  padding: 40px;
  border-radius: 28px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.08);
  border: 1px solid #F1F5F9;
}
.balance-card .label { font-size: 14px; font-weight: 700; color: #64748B; text-transform: uppercase; letter-spacing: 1px; }
.balance-card .chip { background: #EEF2FF; color: #0064E0; padding: 4px 12px; border-radius: 100px; font-size: 11px; font-weight: 800; }
.balance-card .amount { font-size: 56px; font-weight: 900; letter-spacing: -2px; color: #0F172A; }
.balance-card .currency { font-size: 24px; font-weight: 600; color: #94A3B8; }

.stat-item { display: flex; flex-direction: column; gap: 4px; }
.stat-item .l { font-size: 12px; color: #94A3B8; }
.stat-item .v { font-size: 16px; font-weight: 700; color: #0F172A; }

.footer-stats { display: flex; gap: 60px; border-top: 1px solid #F1F5F9; padding-top: 24px; }

/* TRANSACTION SUMMARY */
.transaction-summary { display: flex; flex-direction: column; gap: 20px; }
.summary-box {
  background: white;
  padding: 24px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.04);
  border: 1px solid #F1F5F9;
}
.icon-circle { width: 50px; height: 50px; border-radius: 15px; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.lbl { font-size: 12px; font-weight: 600; color: #94A3B8; }
.val { font-size: 20px; font-weight: 800; color: #0F172A; }

/* LEDGER SECTION */
.ledger-section {
  background: white;
  border-radius: 32px;
  padding: 40px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
  border: 1px solid #F1F5F9;
}

.export-btn-ledger { border-radius: 14px; padding: 12px 24px; font-weight: 700; background: #0F172A; border: none; }

.fintech-table { border-radius: 20px; overflow: hidden; }

.date-cell { display: flex; flex-direction: column; }
.date-cell .d { font-weight: 600; color: #1E293B; }
.date-cell .t { font-size: 12px; color: #94A3B8; }

.badge-fintech { padding: 6px 14px; border-radius: 10px; font-size: 12px; font-weight: 700; }
.badge-fintech.success { background: #DCFCE7; color: #15803D; }
.badge-fintech.danger { background: #FFE4E6; color: #BE123C; }
.badge-fintech.info { background: #F1F5F9; color: #475569; }

.amount-cell { display: flex; align-items: baseline; justify-content: flex-end; gap: 2px; }
.amount-cell .prefix { font-weight: 800; font-size: 18px; }
.amount-cell .val { font-size: 18px; font-weight: 800; color: #0F172A; }
.amount-cell .u { font-size: 12px; font-weight: 600; color: #94A3B8; }

@keyframes slideUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>