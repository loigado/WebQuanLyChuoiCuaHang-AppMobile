<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-10 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Lịch Sử Biến Động Kho</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Theo dõi chi tiết các phiếu nhập, xuất và điều chuyển hàng hóa</p>
        </div>
      </div>

      <!-- TABS -->
      <el-tabs v-model="activeTab" class="meta-tabs mb-6" @tab-change="onTabChange">

        <!-- ==================== TAB 1: NHẬP / XUẤT (CHỈ ĐỌC) ==================== -->
        <el-tab-pane label="📋 Phiếu Nhập / Xuất" name="stock">
          <el-card shadow="never" class="meta-card mb-4">
            <div class="flex flex-wrap gap-4 items-center">
              <span class="text-[14px] font-bold text-[#1C2B33]">Bộ lọc:</span>
              <el-select v-model="filterStatus" placeholder="Tất cả trạng thái" clearable @change="fetchHistory" class="meta-input w-48">
                <el-option label="Đã duyệt" value="approved" />
                <el-option label="Chờ duyệt" value="pending" />
                <el-option label="Từ chối" value="rejected" />
              </el-select>

              <el-date-picker v-model="fromDate" type="date" placeholder="Từ ngày" format="DD/MM/YYYY" value-format="YYYY-MM-DD" class="meta-input !w-44" @change="fetchHistory" />
              <el-date-picker v-model="toDate" type="date" placeholder="Đến ngày" format="DD/MM/YYYY" value-format="YYYY-MM-DD" class="meta-input !w-44" @change="fetchHistory" />

              <el-button class="meta-btn-primary" @click="fetchHistory">Lọc dữ liệu</el-button>
            </div>
          </el-card>

          <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
            <el-table :data="historyData" v-loading="isLoading" class="meta-table" style="width: 100%">
              <el-table-column prop="createdAt" label="Thời gian" width="180">
                <template #default="{row}">
                  <span class="text-[#5D6C7B]">{{ formatDateTime(row.createdAt) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="transactionCode" label="Mã phiếu" width="160">
                <template #default="{row}">
                  <span class="font-medium text-[#1C2B33]">{{ row.transactionCode }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Loại" width="140" align="center">
                <template #default="{row}">
                  <el-tag :class="getTypeTagClass(row.transactionType)">{{ row.transactionType?.toUpperCase() }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="Chi tiết hàng hóa" min-width="280">
                <template #default="{row}">
                  <div class="flex flex-col gap-1">
                    <div v-for="item in row.details" :key="item.productId" class="text-[14px]">
                      <span class="text-[#5D6C7B]">•</span> <b class="text-[#1C2B33]">{{ item.productName }}</b>:&nbsp;
                      <span class="text-[#0064E0] font-bold">{{ item.quantity }}</span>
                      <span class="text-[#5D6C7B]">&nbsp;{{ item.productUnit }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="Lý do / Ghi chú" min-width="200">
                <template #default="{row}">
                  <span class="text-[#5D6C7B] italic text-[13px]">{{ row.reason || row.note || '---' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Kho liên quan" min-width="220">
                <template #default="{row}">
                  <div class="flex flex-col gap-1">
                    <div v-if="row.fromBranchName" class="text-[13px]"><span class="text-[#5D6C7B]">Từ:</span> <span class="text-[#D6311F] font-bold">{{ row.fromBranchName }}</span></div>
                    <div v-if="row.toBranchName" class="text-[13px]"><span class="text-[#5D6C7B]">Đến:</span> <span class="text-[#31A24C] font-bold">{{ row.toBranchName }}</span></div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="Trạng thái" width="140" align="center" fixed="right">
                <template #default="{row}">
                  <el-tag :class="statusMap[row.status]?.class || 'meta-tag-info'">
                    {{ statusMap[row.status]?.label || row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <div class="mt-8 flex justify-end">
              <el-pagination background v-model:current-page="currentPage" :page-size="pageSize"
                layout="prev, pager, next" :total="totalElements" @current-change="fetchHistory" />
            </div>
          </el-card>
        </el-tab-pane>

        <!-- ==================== TAB 2: THẺ KHO CHI TIẾT (MỚI) ==================== -->
        <el-tab-pane label="📋 Thẻ Kho Chi Tiết" name="card">
          <el-card shadow="never" class="meta-card mb-4">
            <div class="flex flex-wrap gap-4 items-center">
              <span class="text-[14px] font-bold text-[#1C2B33]">Chọn sản phẩm:</span>
              <el-select v-model="selectedProductId" placeholder="Tìm tên hoặc mã SP..." filterable clearable class="meta-input w-80" @change="fetchStockCard">
                <el-option v-for="p in productList" :key="p.productId" :label="p.productName + ' (' + p.productCode + ')'" :value="p.productId" />
              </el-select>

              <el-date-picker v-model="cardFromDate" type="date" placeholder="Từ ngày" format="DD/MM/YYYY" value-format="YYYY-MM-DD" class="meta-input !w-44" @change="fetchStockCard" />
              <el-date-picker v-model="cardToDate" type="date" placeholder="Đến ngày" format="DD/MM/YYYY" value-format="YYYY-MM-DD" class="meta-input !w-44" @change="fetchStockCard" />
              
              <el-button class="meta-btn-primary" @click="fetchStockCard" :disabled="!selectedProductId">Xem thẻ kho</el-button>
            </div>
          </el-card>

          <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
            <div v-if="!selectedProductId" class="py-20 text-center text-[#5D6C7B]">
              <span class="text-[40px] block mb-2">🔍</span>
              <p>Vui lòng chọn một sản phẩm để xem lịch sử biến động chi tiết</p>
            </div>
            <el-table v-else :data="cardData" v-loading="cardLoading" class="meta-table" style="width: 100%">
              <el-table-column prop="createdAt" label="Thời gian" width="180">
                <template #default="{row}">{{ formatDateTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="Mã tham chiếu" width="160">
                <template #default="{row}"><b>{{ row.transactionCode }}</b></template>
              </el-table-column>
              <el-table-column label="Loại biến động" width="160">
                <template #default="{row}">
                  <el-tag :class="getTypeTagClass(row.transactionType)">{{ formatType(row.transactionType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="Thay đổi (+/-)" width="150" align="center">
                <template #default="{row}">
                  <span :class="getQuantityClass(row)" class="font-bold text-[16px]">
                    {{ getQuantityDisplay(row) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="Ghi chú / Đối tác" min-width="200">
                <template #default="{row}">
                  <span class="text-[13px] text-[#5D6C7B]">{{ row.reason || row.fromBranchName || row.toBranchName || '---' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <!-- ==================== TAB 3: ĐIỀU CHUYỂN LIÊN CHI NHÁNH ==================== -->
        <el-tab-pane label="🔄 Điều Chuyển Liên Chi Nhánh" name="transfer">
          <!-- Workflow Steps Banner -->
          <div class="flex items-center justify-center gap-3 text-[13px] mb-6 flex-wrap">
            <span class="px-3 py-1.5 rounded-lg bg-[#FFF3CD] text-[#856404] font-bold">① Tạo phiếu</span>
            <span class="text-[#5D6C7B]">→</span>
            <span class="px-3 py-1.5 rounded-lg bg-[#D1ECF1] text-[#0C5460] font-bold">② Duyệt (Approved)</span>
            <span class="text-[#5D6C7B]">→</span>
            <span class="px-3 py-1.5 rounded-lg bg-[#CCE5FF] text-[#004085] font-bold">③ Xuất hàng (Shipping)</span>
            <span class="text-[#5D6C7B]">→</span>
            <span class="px-3 py-1.5 rounded-lg bg-[#D4EDDA] text-[#155724] font-bold">④ Nhận hàng (Completed)</span>
          </div>

          <el-card shadow="never" class="meta-card mb-4">
            <div class="flex gap-4 items-center">
              <span class="text-[14px] font-bold text-[#1C2B33]">Lọc:</span>
              <el-select v-model="transferFilter" placeholder="Tất cả trạng thái" clearable @change="fetchTransfers" class="meta-input w-52">
                <el-option label="Chờ duyệt" value="Pending" />
                <el-option label="Đã duyệt" value="Approved" />
                <el-option label="Đang vận chuyển" value="Shipping" />
                <el-option label="Hoàn thành" value="Completed" />
                <el-option label="Từ chối" value="Rejected" />
              </el-select>
              <el-button class="meta-btn-primary" @click="fetchTransfers">Làm mới</el-button>
            </div>
          </el-card>

          <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
            <el-table :data="filteredTransfers" v-loading="transferLoading" class="meta-table" style="width: 100%"
              empty-text="Không có phiếu điều chuyển nào">
              <el-table-column label="Thời gian" width="170">
                <template #default="{row}">
                  <span class="text-[#5D6C7B] text-[13px]">{{ formatDateTime(row.createdAt) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Mã phiếu" width="160">
                <template #default="{row}">
                  <span class="font-medium text-[#1C2B33]">{{ row.requestCode || '#' + row.requestId }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Sản phẩm / SL" min-width="180">
                <template #default="{row}">
                  <span class="font-medium text-[#1C2B33]">{{ row.product?.productName || '---' }}</span>
                  <br/>
                  <span class="text-[#0064E0] font-bold">{{ row.quantity }}</span>
                  <span class="text-[#5D6C7B] text-[12px]"> đơn vị</span>
                </template>
              </el-table-column>
              <el-table-column label="Tuyến chuyển" min-width="280">
                <template #default="{row}">
                  <div class="flex items-center gap-2">
                    <span class="text-[#E41E3F] font-medium">{{ row.fromBranch?.branchName || '---' }}</span>
                    <span class="text-[20px] text-[#5D6C7B]">→</span>
                    <span class="text-[#31A24C] font-medium">{{ row.toBranch?.branchName || '---' }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="Ưu tiên" width="110" align="center">
                <template #default="{row}">
                  <el-tag :class="getPriorityClass(row.priority)">{{ row.priority || 'Normal' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="Trạng thái" width="160" align="center">
                <template #default="{row}">
                  <el-tag :class="getTransferStatusClass(row.status)">
                    {{ transferStatusMap[row.status] || row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="Hành động" width="250" align="center" fixed="right">
                <template #default="{row}">
                  <div class="flex justify-center gap-2 flex-wrap">
                    <template v-if="row.status === 'Pending' && row.fromBranch?.branchId === currentBranchId">
                      <el-button size="small" class="meta-btn-success-sm" @click="handleApprove(row.requestId)">✅ Duyệt</el-button>
                      <el-button size="small" class="meta-btn-danger-sm" @click="handleReject(row.requestId)">❌ Từ chối</el-button>
                    </template>
                    <el-button v-else-if="row.status === 'Approved' && row.fromBranch?.branchId === currentBranchId" size="small" class="meta-btn-primary-sm" @click="handleShip(row.requestId)">📦 Xác nhận xuất</el-button>
                    <el-button v-else-if="row.status === 'Shipping' && row.toBranch?.branchId === currentBranchId" size="small" class="meta-btn-success-sm" @click="handleReceive(row.requestId)">📥 Xác nhận nhận</el-button>
                    <span v-else-if="row.status === 'Waiting_Admin'" class="text-[13px] text-[#D6311F] font-medium">
                      ⏳ Chờ Admin hệ thống duyệt xuất kho
                    </span>
                    <span v-else class="text-[13px] text-[#5D6C7B] italic">
                      {{ row.status === 'Completed' ? '✓ Hoàn tất' : (row.status === 'Rejected' ? '✗ Kho xuất từ chối' : (row.status === 'Rejected_By_Admin' ? '✗ Admin từ chối xuất' : (row.status === 'Cancelled' ? '✗ Đã hủy' : '⏳ Chờ đối tác xử lý'))) }}
                    </span>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '@/api/axios.js';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const currentBranchId = computed(() => auth.branchId);

// ===== TAB STATE =====
const activeTab = ref('stock');

// ===== TAB 1: STOCK HISTORY =====
const historyData = ref([]);
const isLoading = ref(false);
const filterStatus = ref(null);
const fromDate = ref(null);
const toDate = ref(null);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);

const statusMap = {
  'approved': { label: 'Đã duyệt', class: 'meta-tag-success' },
  'pending':  { label: 'Chờ duyệt', class: 'meta-tag-warning' },
  'rejected': { label: 'Từ chối',   class: 'meta-tag-error' }
};

const fetchHistory = async () => {
  isLoading.value = true;
  try {
    const params = {
      type: filterStatus.value === 'approved' || filterStatus.value === 'pending' || filterStatus.value === 'rejected' ? null : filterStatus.value,
      status: (filterStatus.value === 'approved' || filterStatus.value === 'pending' || filterStatus.value === 'rejected') ? filterStatus.value : null,
      fromDate: fromDate.value,
      toDate: toDate.value
    };
    const res = await api.get('/manager/inventory/history', { params });
    historyData.value = res.data?.data || [];
    totalElements.value = historyData.value.length;
  } catch (error) {
    console.error('Lỗi tải lịch sử kho');
  } finally {
    isLoading.value = false;
  }
};

const getTypeTagClass = (type) => {
  if (type === 'import') return 'meta-tag-success';
  if (type === 'export') return 'meta-tag-error';
  return 'meta-tag-info';
};

// ===== TAB 2: STOCK CARD (NEW) =====
const productList = ref([]);
const selectedProductId = ref(null);
const cardData = ref([]);
const cardLoading = ref(false);
const cardFromDate = ref(null);
const cardToDate = ref(null);

const fetchProducts = async () => {
  try {
    const res = await api.get('/manager/inventory/advanced-report');
    productList.value = res.data?.data || [];
    
    // Nếu có productId từ URL, tự chọn và fetch
    if (route.query.productId) {
      selectedProductId.value = parseInt(route.query.productId);
      activeTab.value = 'card';
      fetchStockCard();
    }
  } catch (e) { console.error("Lỗi tải DS sản phẩm"); }
};

const fetchStockCard = async () => {
  if (!selectedProductId.value) return;
  cardLoading.value = true;
  try {
    const params = {
      productId: selectedProductId.value,
      fromDate: cardFromDate.value,
      toDate: cardToDate.value
    };
    const res = await api.get('/manager/inventory/history', { params });
    cardData.value = res.data?.data || [];
  } catch (e) {
    ElMessage.error("Lỗi tải thẻ kho");
  } finally {
    cardLoading.value = false;
  }
};

const formatType = (type) => {
  const t = type?.toLowerCase();
  if (t === 'import') return 'NHẬP KHO';
  if (t === 'export') return 'XUẤT KHO';
  if (t.includes('transfer_in')) return 'NHẬN ĐIỀU CHUYỂN';
  if (t.includes('transfer_out')) return 'XUẤT ĐIỀU CHUYỂN';
  if (t === 'adjustment' || t === 'stocktake') return 'KIỂM KÊ/ĐIỀU CHỈNH';
  return t.toUpperCase();
};

const getQuantityClass = (row) => {
  const type = row.transactionType?.toLowerCase();
  if (type === 'import' || type.includes('transfer_in')) return 'text-[#31A24C]';
  if (type === 'export' || type.includes('transfer_out')) return 'text-[#E41E3F]';
  return 'text-[#0064E0]';
};

const getQuantityDisplay = (row) => {
  const type = row.transactionType?.toLowerCase();
  const detail = row.details?.find(d => d.productId === selectedProductId.value) || row;
  const qty = detail.quantity;
  if (type === 'import' || type.includes('transfer_in')) return `+${qty}`;
  if (type === 'export' || type.includes('transfer_out')) return `-${qty}`;
  return qty;
};

// ===== TAB 3: TRANSFER WORKFLOW =====
const transfers = ref([]);
const transferLoading = ref(false);
const transferFilter = ref('');

const filteredTransfers = computed(() =>
  transferFilter.value
    ? transfers.value.filter(t => t.status === transferFilter.value)
    : transfers.value
);

const transferStatusMap = {
  Pending:   'Chờ duyệt',
  Approved:  'Đã duyệt',
  Shipping:  'Đang vận chuyển',
  Completed: 'Hoàn thành',
  Rejected:  'Từ chối',
  Cancelled: 'Đã hủy'
};

const getTransferStatusClass = (s) => ({
  Pending:   'meta-tag-warning',
  Approved:  'meta-tag-info',
  Shipping:  'meta-tag-info',
  Completed: 'meta-tag-success',
  Rejected:  'meta-tag-error',
  Cancelled: 'meta-tag-error'
}[s] || 'meta-tag-info');

const getPriorityClass = (p) => ({
  Urgent: 'meta-tag-error',
  High:   'meta-tag-warning',
  Normal: 'meta-tag-info',
  Low:    'meta-tag-success'
}[p] || 'meta-tag-info');

const fetchTransfers = async () => {
  transferLoading.value = true;
  try {
    const res = await api.get('/manager/stock/transfer');
    transfers.value = res.data?.data || [];
  } catch (e) {
    ElMessage.error('Không thể tải danh sách điều chuyển');
    console.error(e);
  } finally {
    transferLoading.value = false;
  }
};

const handleApprove = (id) => {
  ElMessageBox.confirm(
    'Duyệt phiếu điều chuyển này? Tồn kho kho nguồn sẽ bị trừ ngay.',
    'Xác nhận duyệt',
    { confirmButtonText: 'Duyệt', cancelButtonText: 'Hủy', type: 'warning' }
  ).then(async () => {
    try {
      await api.post(`/manager/stock/transfer/${id}/approve`);
      ElMessage.success('Đã duyệt! Kho nguồn đã được trừ hàng.');
      fetchTransfers();
    } catch (e) {
      ElMessage.error(e.response?.data?.message || 'Lỗi duyệt phiếu');
    }
  }).catch(() => {});
};

const handleReject = (id) => {
  ElMessageBox.prompt('Nhập lý do từ chối:', 'Từ chối điều chuyển', {
    confirmButtonText: 'Xác nhận', cancelButtonText: 'Hủy',
    inputPattern: /\S+/, inputErrorMessage: 'Lý do không được để trống'
  }).then(async ({ value }) => {
    try {
      await api.post(`/manager/stock/transfer/${id}/reject`, null, { params: { reason: value } });
      ElMessage.success('Đã từ chối phiếu!');
      fetchTransfers();
    } catch (e) {
      ElMessage.error(e.response?.data?.message || 'Lỗi từ chối');
    }
  }).catch(() => {});
};

const handleShip = (id) => {
  ElMessageBox.confirm(
    'Xác nhận hàng đã được xuất và đang trên đường vận chuyển?',
    'Xác nhận xuất hàng',
    { confirmButtonText: 'Xác nhận', cancelButtonText: 'Hủy', type: 'info' }
  ).then(async () => {
    try {
      await api.post(`/manager/stock/transfer/${id}/ship`);
      ElMessage.success('Trạng thái → Đang vận chuyển');
      fetchTransfers();
    } catch (e) {
      ElMessage.error(e.response?.data?.message || 'Lỗi xác nhận xuất');
    }
  }).catch(() => {});
};

const handleReceive = (id) => {
  ElMessageBox.confirm(
    'Xác nhận đã nhận hàng đầy đủ? Kho đích sẽ được cộng số lượng.',
    'Xác nhận nhận hàng',
    { confirmButtonText: 'Nhận hàng', cancelButtonText: 'Hủy', type: 'success' }
  ).then(async () => {
    try {
      await api.post(`/manager/stock/transfer/${id}/receive`);
      ElMessage.success('Đã nhận hàng! Kho đích đã cập nhật.');
      fetchTransfers();
    } catch (e) {
      ElMessage.error(e.response?.data?.message || 'Lỗi nhận hàng');
    }
  }).catch(() => {});
};

// ===== LIFECYCLE =====
const onTabChange = (tab) => {
  if (tab === 'transfer' && transfers.value.length === 0) fetchTransfers();
  if (tab === 'card' && productList.value.length === 0) fetchProducts();
};

const formatDateTime = (val) => {
  if (!val) return '---';
  return new Date(val).toLocaleString('vi-VN', {
    hour: '2-digit', minute: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric'
  });
};

onMounted(() => {
  fetchHistory();
  fetchProducts(); // Để check route query và load list
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }

/* Tabs */
:deep(.meta-tabs .el-tabs__header) { margin-bottom: 0; }
:deep(.meta-tabs .el-tabs__nav-wrap::after) { background-color: #DEE3E9; }
:deep(.meta-tabs .el-tabs__item) { font-family: 'Montserrat', sans-serif; font-size: 15px; font-weight: 500; color: #5D6C7B; padding: 0 20px 14px; }
:deep(.meta-tabs .el-tabs__item.is-active) { color: #0064E0; font-weight: 600; }
:deep(.meta-tabs .el-tabs__active-bar) { background-color: #0064E0; height: 3px; border-radius: 2px; }

/* Buttons */
:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; }
:deep(.meta-btn-success-sm) { border-radius: 100px !important; background-color: #31A24C !important; border-color: #31A24C !important; color: #FFFFFF !important; font-size: 12px; font-weight: 600; padding: 5px 14px !important; height: auto; }
:deep(.meta-btn-danger-sm) { border-radius: 100px !important; background-color: #E41E3F !important; border-color: #E41E3F !important; color: #FFFFFF !important; font-size: 12px; font-weight: 600; padding: 5px 14px !important; height: auto; }
:deep(.meta-btn-primary-sm) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-size: 12px; font-weight: 600; padding: 5px 14px !important; height: auto; }

/* Input */
:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; }

/* Table */
:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 20px 0 14px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 18px 0 !important; }

/* Tags */
:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error), :deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; border: none; padding: 5px 14px; height: auto; }
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error)   { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
:deep(.meta-tag-info)    { background-color: rgba(0, 100, 224, 0.15); color: #0064E0; }

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) { background-color: #0064E0 !important; border-radius: 8px; }
</style>