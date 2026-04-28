<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="mb-12">
        <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Quản lý Điều chuyển kho</h2>
        <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Duyệt, xuất hàng, nhận hàng cho các phiếu điều chuyển liên chi nhánh</p>
      </div>

      <!-- Workflow Steps -->
      <el-card shadow="never" class="meta-card mb-8">
        <div class="flex items-center justify-center gap-2 text-[14px] text-[#5D6C7B] py-2 flex-wrap">
          <span class="px-3 py-1.5 rounded-[8px] bg-[#FFF3CD] text-[#856404] font-bold">① Pending</span>
          <span class="text-[20px]">→</span>
          <span class="px-3 py-1.5 rounded-[8px] bg-[#D1ECF1] text-[#0C5460] font-bold">② Approved</span>
          <span class="text-[20px]">→</span>
          <span class="px-3 py-1.5 rounded-[8px] bg-[#CCE5FF] text-[#004085] font-bold">③ Shipping</span>
          <span class="text-[20px]">→</span>
          <span class="px-3 py-1.5 rounded-[8px] bg-[#D4EDDA] text-[#155724] font-bold">④ Completed</span>
        </div>
      </el-card>

      <!-- Bảng dữ liệu -->
      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <div class="flex gap-4 mb-6">
          <el-select v-model="filterStatus" placeholder="Lọc trạng thái" clearable @change="applyFilter" class="meta-input w-[220px]">
            <el-option label="Tất cả" value="" />
            <el-option label="Chờ duyệt" value="Pending" />
            <el-option label="Đã duyệt" value="Approved" />
            <el-option label="Đang vận chuyển" value="Shipping" />
            <el-option label="Hoàn thành" value="Completed" />
            <el-option label="Từ chối" value="Rejected" />
          </el-select>
        </div>

        <el-table :data="filteredTransfers" v-loading="loading" class="meta-table" style="width: 100%">
          <el-table-column prop="requestCode" label="Mã phiếu" width="200">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.requestCode }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Sản phẩm" min-width="200">
            <template #default="{row}">
              <span class="text-[#1C2B33] font-medium">{{ row.product?.productName || '---' }}</span>
              <br/><span class="text-[12px] text-[#5D6C7B]">SL:&nbsp;{{ row.quantity }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Kho nguồn → Kho đích" min-width="300">
            <template #default="{row}">
              <div class="flex items-center gap-2">
                <span class="text-[#E41E3F] font-medium">{{ row.fromBranch?.branchName || '---' }}</span>
                <span class="text-[18px] text-[#5D6C7B]">→</span>
                <span class="text-[#31A24C] font-medium">{{ row.toBranch?.branchName || '---' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Ưu tiên" width="120" align="center">
            <template #default="{row}">
              <el-tag :class="getPriorityClass(row.priority)">{{ row.priority || 'Normal' }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="getStatusClass(row.status)">{{ statusMap[row.status] || row.status }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Thao tác" width="300" align="center" fixed="right">
            <template #default="{row}">
              <div class="flex justify-center gap-2 flex-wrap">
                <!-- Pending: Duyệt / Từ chối (Manager kho nguồn) -->
                <template v-if="row.status === 'Pending' && row.fromBranch?.branchId === currentBranchId">
                  <el-button class="meta-btn-success-sm" @click="handleApprove(row.requestId)">✅ Duyệt</el-button>
                  <el-button class="meta-btn-danger-sm" @click="handleReject(row.requestId)">❌ Từ chối</el-button>
                </template>

                <!-- Approved: Xác nhận xuất hàng (Manager kho nguồn) -->
                <template v-if="row.status === 'Approved' && row.fromBranch?.branchId === currentBranchId">
                  <el-button class="meta-btn-primary-sm" @click="handleShip(row.requestId)">📦 Xuất hàng</el-button>
                </template>

                <!-- Shipping: Xác nhận nhận hàng (Manager kho đích) -->
                <template v-if="row.status === 'Shipping' && row.toBranch?.branchId === currentBranchId">
                  <el-button class="meta-btn-success-sm" @click="handleReceive(row.requestId)">📥 Nhận hàng</el-button>
                </template>

                <!-- Trường hợp không có quyền xử lý hoặc trạng thái kết thúc -->
                <template v-else-if="row.status === 'Completed' || row.status === 'Rejected'">
                  <span class="text-[13px] text-[#5D6C7B] italic">{{ row.status === 'Completed' ? 'Đã hoàn tất' : 'Đã từ chối' }}</span>
                </template>
                <template v-else>
                  <span class="text-[12px] text-[#5D6C7B] italic bg-[#F1F4F7] px-2 py-1 rounded">Chờ đối tác</span>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/api/axios';
import { ElMessage, ElMessageBox } from 'element-plus';

import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const currentBranchId = computed(() => auth.branchId);

const transfers = ref([]);
const loading = ref(false);
const filterStatus = ref('');

const filteredTransfers = computed(() => {
  if (!filterStatus.value) return transfers.value;
  return transfers.value.filter(t => t.status === filterStatus.value);
});

const statusMap = {
  Pending: 'Chờ duyệt', Approved: 'Đã duyệt', Rejected: 'Từ chối',
  Shipping: 'Đang vận chuyển', Completed: 'Hoàn thành', Cancelled: 'Đã hủy'
};

const fetchTransfers = async () => {
  loading.value = true;
  try {
    const res = await api.get('/manager/stock/transfer');
    transfers.value = res.data?.data || [];
  } catch (e) {
    ElMessage.error('Lỗi tải danh sách điều chuyển');
    console.error(e);
  } finally { loading.value = false; }
};

const handleApprove = (id) => {
  ElMessageBox.confirm('Duyệt phiếu điều chuyển này? Kho nguồn sẽ bị trừ hàng.', 'Xác nhận duyệt', {
    confirmButtonText: 'Duyệt', cancelButtonText: 'Hủy', type: 'warning'
  }).then(async () => {
    try {
      await api.post(`/manager/stock/transfer/${id}/approve`);
      ElMessage.success('Đã duyệt phiếu! Kho nguồn đã bị trừ hàng.');
      fetchTransfers();
    } catch (e) { ElMessage.error(e.response?.data?.message || 'Lỗi duyệt'); }
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
    } catch (e) { ElMessage.error(e.response?.data?.message || 'Lỗi từ chối'); }
  }).catch(() => {});
};

const handleShip = (id) => {
  ElMessageBox.confirm('Xác nhận hàng đã được xuất và đang vận chuyển?', 'Xác nhận xuất hàng', {
    confirmButtonText: 'Xuất hàng', cancelButtonText: 'Hủy', type: 'info'
  }).then(async () => {
    try {
      await api.post(`/manager/stock/transfer/${id}/ship`);
      ElMessage.success('Đã xác nhận xuất hàng! Trạng thái → Shipping');
      fetchTransfers();
    } catch (e) { ElMessage.error(e.response?.data?.message || 'Lỗi xác nhận xuất'); }
  }).catch(() => {});
};

const handleReceive = (id) => {
  ElMessageBox.confirm('Xác nhận đã nhận hàng thành công? Kho đích sẽ được cộng hàng.', 'Xác nhận nhận hàng', {
    confirmButtonText: 'Nhận hàng', cancelButtonText: 'Hủy', type: 'success'
  }).then(async () => {
    try {
      await api.post(`/manager/stock/transfer/${id}/receive`);
      ElMessage.success('Đã nhận hàng! Kho đích đã được cập nhật.');
      fetchTransfers();
    } catch (e) { ElMessage.error(e.response?.data?.message || 'Lỗi nhận hàng'); }
  }).catch(() => {});
};

const applyFilter = () => { /* computed tự filter */ };

const getStatusClass = (s) => {
  const map = { Pending: 'meta-tag-warning', Approved: 'meta-tag-info', Shipping: 'meta-tag-info', Completed: 'meta-tag-success', Rejected: 'meta-tag-error', Cancelled: 'meta-tag-error' };
  return map[s] || 'meta-tag-info';
};

const getPriorityClass = (p) => {
  if (!p) return 'meta-tag-info';
  const map = { Urgent: 'meta-tag-error', High: 'meta-tag-warning', Normal: 'meta-tag-info', Low: 'meta-tag-success' };
  return map[p] || 'meta-tag-info';
};

onMounted(fetchTransfers);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }
:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }
:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 20px 0 !important; }
:deep(.meta-btn-success-sm) { border-radius: 100px !important; background-color: #31A24C !important; border-color: #31A24C !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px; padding: 6px 16px !important; height: auto; }
:deep(.meta-btn-danger-sm) { border-radius: 100px !important; background-color: #E41E3F !important; border-color: #E41E3F !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px; padding: 6px 16px !important; height: auto; }
:deep(.meta-btn-primary-sm) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px; padding: 6px 16px !important; height: auto; }
:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error), :deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; border: none; padding: 6px 16px; height: auto; font-size: 12px; }
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
:deep(.meta-tag-info) { background-color: rgba(0, 100, 224, 0.15); color: #0064E0; }
:deep(.meta-input .el-select__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; }
</style>
