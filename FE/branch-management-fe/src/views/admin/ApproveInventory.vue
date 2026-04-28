<template>
  <div class="px-8 py-16 md:px-16 bg-[#FFFFFF] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Xét Duyệt Phiếu Yêu Cầu Kho</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Dành cho Admin: Phê duyệt hoặc từ chối các yêu cầu nhập, xuất, điều chuyển</p>
        </div>
      </div>

      <div class="border border-[#DEE3E9] rounded-[20px] overflow-hidden shadow-[0_2px_4px_0_rgba(0,0,0,0.05)]">
        <el-table :data="requests" v-loading="isLoading" class="meta-table" style="width: 100%">
          
          <el-table-column type="expand">
            <template #default="props">
              <div class="m-6 p-8 bg-[#F7F8FA] rounded-[16px]">
                <h4 class="text-[18px] font-bold text-[#1C2B33] mb-6 flex items-center gap-2">
                  <span>📦</span> Danh sách hàng hóa trong phiếu:
                </h4>
                <div class="border border-[#DEE3E9] rounded-[12px] overflow-hidden bg-white">
                  <el-table :data="props.row.details && props.row.details.length > 0 ? props.row.details : [props.row]" class="meta-table-inner" style="width: 100%">
                    <el-table-column type="index" label="STT" width="80" align="center" />
                    <el-table-column label="Mã SP" prop="productCode" width="160">
                      <template #default="{row}">
                        <span class="text-[#5D6C7B] font-mono text-[14px] bg-[#F1F4F7] px-2 py-1 rounded">{{ row.productCode }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="Tên sản phẩm" prop="productName" min-width="250">
                      <template #default="{row}"><span class="text-[16px] font-medium text-[#1C2B33]">{{ row.productName }}</span></template>
                    </el-table-column>
                    <el-table-column label="Số lượng" width="180" align="center">
                      <template #default="{row}">
                        <span class="text-[#0064E0] font-bold text-[18px]">{{ row.quantity }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="Đơn vị tính" prop="productUnit" width="150" align="center">
                      <template #default="{row}">
                        <span class="text-[#5D6C7B]">{{ row.productUnit }}</span>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Mã Phiếu" prop="transactionCode" width="160" align="center">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.transactionCode || '#' + row.transactionId }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="Loại Y/C" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="row.transactionType?.toUpperCase() === 'IMPORT' ? 'meta-tag-success' : (row.transactionType?.toUpperCase() === 'EXPORT' ? 'meta-tag-warning' : 'meta-tag-info')">
                {{ row.transactionType?.toUpperCase() === 'TRANSFER' ? 'ĐIỀU CHUYỂN' : row.transactionType?.toUpperCase() }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="Kho Xử Lý" min-width="260">
            <template #default="{row}">
              <div v-if="row.transactionType?.toUpperCase().includes('TRANSFER')" class="flex items-center gap-2">
                <span class="text-[#1C2B33] font-medium">{{ row.fromBranchName || (row.fromBranchId ? 'Kho ID ' + row.fromBranchId : '?') }}</span> 
                <span class="text-[#5D6C7B]">→</span> 
                <span class="text-[#1C2B33] font-medium">{{ row.toBranchName || (row.toBranchId ? 'Kho ID ' + row.toBranchId : '?') }}</span>
              </div>
              <div v-else-if="row.transactionType?.toUpperCase() === 'IMPORT'">
                <span class="text-[#5D6C7B] mr-2">Nhập vào:</span><span class="text-[#1C2B33] font-medium">{{ row.toBranchName || 'Kho ID ' + row.toBranchId }}</span>
              </div>
              <div v-else>
                <span class="text-[#5D6C7B] mr-2">Xuất từ:</span><span class="text-[#1C2B33] font-medium">{{ row.fromBranchName || 'Kho ID ' + row.fromBranchId }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Số lượng" width="140" align="center">
            <template #default="{row}">
              <span class="text-[#5D6C7B] bg-[#F1F4F7] px-3 py-1.5 rounded-[100px] text-[14px]">
                {{ row.details ? row.details.length : 1 }} món
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="Lý do tạo phiếu" prop="reason" min-width="200" show-overflow-tooltip>
             <template #default="{row}">
               <span class="text-[#5D6C7B]">{{ row.reason }}</span>
             </template>
          </el-table-column>

          <el-table-column label="Thao tác" width="220" align="center" fixed="right">
            <template #default="{row}">
              <div class="flex justify-center gap-2">
                <el-button class="meta-btn-primary-sm" @click="handleApproval(row.transactionId, 'APPROVED')">Duyệt</el-button>
                <el-button class="meta-btn-danger-sm" @click="handleApproval(row.transactionId, 'REJECTED')">Từ chối</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="!isLoading && requests.length === 0" class="py-16 text-center text-[#5D6C7B]">
          <span class="text-[32px] block mb-4">📭</span>
          <span class="text-[16px]">Không có phiếu nào đang chờ duyệt</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios.js';
import { ElMessage, ElMessageBox } from 'element-plus';

const requests = ref([]);
const isLoading = ref(false);

// ✅ ĐÃ SỬA: Gọi đúng endpoint StockApprovalController
const fetchPendingRequests = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/admin/stock/approve/pending');
    requests.value = res.data?.data?.content || res.data?.data || [];
  } catch (error) {
    ElMessage.error('Lỗi tải danh sách phiếu chờ duyệt');
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

// ✅ ĐÃ SỬA: Gọi đúng endpoint accept/reject của StockApprovalController
const handleApproval = (id, newStatus) => {
  if (newStatus === 'APPROVED') {
    ElMessageBox.confirm(
      'Bạn có chắc chắn muốn DUYỆT phiếu yêu cầu này không?', 
      'Xác nhận duyệt', 
      { confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy' }
    ).then(async () => {
      try {
        await api.post(`/admin/stock/approve/${id}/accept`);
        ElMessage.success('Đã duyệt phiếu thành công!');
        fetchPendingRequests();
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Lỗi khi duyệt phiếu');
      }
    }).catch(() => {});
  } else {
    ElMessageBox.prompt(
      'Vui lòng nhập lý do từ chối phiếu này:', 
      'Từ chối phiếu', 
      {
        confirmButtonText: 'Xác nhận từ chối',
        cancelButtonText: 'Hủy bỏ',
        inputPattern: /\S+/, 
        inputErrorMessage: 'Lý do từ chối không được để trống'
      }
    ).then(async ({ value }) => {
      try {
        await api.post(`/admin/stock/approve/${id}/reject`, { rejectionReason: value });
        ElMessage.success('Đã từ chối phiếu thành công!');
        fetchPendingRequests();
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Lỗi khi từ chối phiếu');
      }
    }).catch(() => {});
  }
};

onMounted(fetchPendingRequests);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');

.font-meta { font-family: 'Montserrat', sans-serif; }

/* Table Overrides */
:deep(.meta-table th.el-table__cell), :deep(.meta-table-inner th.el-table__cell) {
  background-color: #FFFFFF !important;
  color: #5D6C7B !important;
  font-weight: 500 !important;
  border-bottom: 1px solid #DEE3E9 !important;
  padding: 24px 0 16px 0 !important;
  font-size: 14px;
}
:deep(.meta-table td.el-table__cell), :deep(.meta-table-inner td.el-table__cell) {
  border-bottom: 1px solid #F1F4F7 !important;
  padding: 20px 0 !important;
}

/* Row Expand Adjustments */
:deep(.el-table__expanded-cell) {
  padding: 0 !important;
  background-color: #FFFFFF !important;
}
:deep(.el-table__expand-icon) {
  color: #0064E0;
}

/* Action Buttons */
:deep(.meta-btn-primary-sm) {
  border-radius: 100px !important;
  background-color: #0064E0 !important;
  border-color: #0064E0 !important;
  color: #FFFFFF !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 14px;
  padding: 8px 20px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-primary-sm:hover) { background-color: #0143B5 !important; }

:deep(.meta-btn-danger-sm) {
  background-color: transparent !important;
  color: #E41E3F !important;
  border: 1px solid #E41E3F !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 14px;
  padding: 8px 20px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-danger-sm:hover) { background-color: rgba(228, 30, 63, 0.05) !important; }

/* Tags */
:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-info) {
  border-radius: 100px;
  font-weight: 700;
  border: none;
  padding: 6px 16px;
  height: auto;
}
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-info) { background-color: rgba(0, 100, 224, 0.15); color: #0064E0; }
</style>