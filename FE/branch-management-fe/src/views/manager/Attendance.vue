<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Duyệt Chấm Công Nhân Sự</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Xử lý các lượt Check-in/out và xác thực hình ảnh tại cửa hàng</p>
        </div>
        <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="fetchData">
          <span class="mr-2 text-[16px]">↻</span> Làm mới danh sách
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card mb-8">
        <div class="flex flex-wrap items-center gap-6">
          <div class="flex items-center gap-4">
            <span class="text-[14px] font-bold text-[#1C2B33] whitespace-nowrap">Trạng thái:</span>
            <el-radio-group v-model="filterStatus" @change="handleFilterChange" class="meta-radio-group">
              <el-radio-button label="">Tất cả</el-radio-button>
              <el-radio-button label="abnormal">Cần duyệt</el-radio-button>
              <el-radio-button label="present">Hợp lệ</el-radio-button>
              <el-radio-button label="late">Đi muộn</el-radio-button>
              <el-radio-button label="approved">Đã duyệt</el-radio-button>
              <el-radio-button label="rejected">Từ chối</el-radio-button>
            </el-radio-group>
          </div>

          <div class="flex items-center gap-4">
            <span class="text-[14px] font-bold text-[#1C2B33] whitespace-nowrap">Thời gian:</span>
            <el-date-picker v-model="fromDate" type="date" placeholder="Từ ngày" format="DD/MM/YYYY" value-format="YYYY-MM-DD" class="meta-input !w-44" @change="handleFilterChange" />
            <el-date-picker v-model="toDate" type="date" placeholder="Đến ngày" format="DD/MM/YYYY" value-format="YYYY-MM-DD" class="meta-input !w-44" @change="handleFilterChange" />
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="attendances" v-loading="isLoading" class="meta-table" style="width: 100%">
          <el-table-column label="Nhân viên" min-width="200">
            <template #default="{row}">
              <div class="font-medium text-[#1C2B33] text-[16px]">{{ row.employeeName }}</div>
              <div class="text-[14px] text-[#5D6C7B] font-mono mt-1">{{ row.employeeCode }}</div>
            </template>
          </el-table-column>
          
          <el-table-column label="Giờ Check-in" width="180">
            <template #default="{row}"><span class="text-[#5D6C7B]">{{ formatDate(row.checkInTime) }}</span></template>
          </el-table-column>

          <el-table-column label="Giờ Check-out" width="180">
            <template #default="{row}"><span class="text-[#5D6C7B]">{{ formatDate(row.checkOutTime) }}</span></template>
          </el-table-column>

          <el-table-column label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="getStatusTagClass(row.status)">
                {{ formatStatus(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Lý do từ chối" min-width="200" show-overflow-tooltip>
            <template #default="{row}">
              <span class="text-[#E41E3F] italic text-[14px]">{{ row.rejectReason || '---' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Thao tác" width="220" align="center" fixed="right">
            <template #default="{row}">
              <div class="flex justify-center gap-2">
                <el-button class="meta-btn-secondary-sm" @click="openDetail(row.id)">Chi tiết</el-button>
                <el-button v-if="row.status?.toLowerCase() !== 'approved' && row.status?.toLowerCase() !== 'rejected'" class="meta-btn-success-sm" @click="handleApprove(row.id)">Duyệt</el-button>
                <el-button v-if="row.status?.toLowerCase() !== 'approved' && row.status?.toLowerCase() !== 'rejected'" class="meta-btn-danger-sm" @click="handleReject(row.id)">Từ chối</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-8 flex justify-end">
          <el-pagination background layout="prev, pager, next" :total="total" :page-size="size" @current-change="handlePageChange" />
        </div>
      </el-card>

      <el-dialog v-model="detailDialogVisible" title="Hồ sơ xác thực chấm công" width="600px" class="meta-dialog" destroy-on-close>
        <div v-if="currentDetail" class="pt-2">
          <!-- Thông tin tóm tắt siêu gọn -->
          <div class="bg-[#F7F8FA] p-4 rounded-[12px] text-[13px] space-y-2 mb-4 border border-[#DEE3E9]">
            <div class="flex justify-between items-center">
              <span class="font-bold text-[#1C2B33] text-[14px]">{{ currentDetail.employeeName }}</span>
              <span class="font-mono text-[12px] text-[#5D6C7B]">Mã NV: {{ currentDetail.employeeCode }}</span>
            </div>
            
            <div class="grid grid-cols-2 gap-4 py-2 border-y border-[#DEE3E9] border-dashed">
              <div class="flex flex-col gap-1">
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold tracking-tight">📥 Giờ vào ca:</span>
                <span class="text-[#1C2B33] font-medium">{{ formatDate(currentDetail.checkInTime) }}</span>
              </div>
              <div class="flex flex-col gap-1">
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold tracking-tight">📤 Giờ kết ca:</span>
                <span class="text-[#1C2B33] font-medium">{{ formatDate(currentDetail.checkOutTime) || '---' }}</span>
              </div>
              <div class="flex flex-col gap-1">
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold tracking-tight">⏱️ Tổng công:</span>
                <span class="text-[#0064E0] font-bold">{{ currentDetail.totalHours || 0 }} h</span>
              </div>
              <div class="flex flex-col gap-1">
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold tracking-tight">🎯 Trạng thái:</span>
                <el-tag :class="getStatusTagClass(currentDetail.status)" size="small" style="width: fit-content; font-weight: bold;">{{ formatStatus(currentDetail.status) }}</el-tag>
              </div>
            </div>

            <div class="pt-2 border-t border-[#DEE3E9] flex justify-between items-center">
              <div class="flex flex-col gap-1">
                <div class="flex items-center gap-2">
                  <span class="text-[#5D6C7B] text-[11px] font-bold">📍 Tọa độ GPS:</span>
                  <span class="font-mono text-[11px] text-[#1C2B33] font-bold">{{ currentDetail.latitude }}, {{ currentDetail.longitude }}</span>
                </div>
                <el-link type="primary" :href="'https://www.google.com/maps?q=' + currentDetail.latitude + ',' + currentDetail.longitude" target="_blank" class="text-[11px] font-bold underline">
                  Xem vị trí trên Google Maps
                </el-link>
              </div>
              <div class="text-right">
                <p class="text-[10px] text-[#5D6C7B] uppercase font-bold mb-0.5">Sai lệch:</p>
                <span class="text-[11px] text-[#D6311F] font-bold bg-rose-50 px-2 py-0.5 rounded-full border border-rose-100">{{ currentDetail.distance?.toFixed(0) }} mét</span>
              </div>
            </div>
          </div>

          <!-- Ảnh xác thực (Ép nằm ngang) -->
          <div style="display: flex !important; flex-direction: row !important; justify-content: center !important; gap: 20px !important;">
            <div v-if="currentDetail.checkInPhotoUrl" style="text-align: center;">
              <p style="font-size: 10px; font-weight: bold; color: #8595A4; margin-bottom: 4px; text-transform: uppercase;">Check-in</p>
              <div style="background: black; border-radius: 8px; padding: 2px; border: 1px solid #DEE3E9;">
                <el-image :src="'http://localhost:8081' + currentDetail.checkInPhotoUrl" 
                  style="width: 100px; height: 100px; display: block;"
                  class="rounded-md object-cover" 
                  :preview-src-list="['http://localhost:8081' + currentDetail.checkInPhotoUrl]" 
                  fit="cover" />
              </div>
            </div>
            <div v-if="currentDetail.checkOutPhotoUrl" style="text-align: center;">
              <p style="font-size: 10px; font-weight: bold; color: #8595A4; margin-bottom: 4px; text-transform: uppercase;">Check-out</p>
              <div style="background: black; border-radius: 8px; padding: 2px; border: 1px solid #DEE3E9;">
                <el-image :src="'http://localhost:8081' + currentDetail.checkOutPhotoUrl" 
                  style="width: 100px; height: 100px; display: block;"
                  class="rounded-md object-cover" 
                  :preview-src-list="['http://localhost:8081' + currentDetail.checkOutPhotoUrl]" 
                  fit="cover" />
              </div>
            </div>
          </div>
        </div>
        <template #footer>
          <div class="flex gap-3 justify-end pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary" @click="detailDialogVisible = false">Đóng</el-button>
            <el-button v-if="currentDetail?.status?.toLowerCase() !== 'approved' && currentDetail?.status?.toLowerCase() !== 'rejected'" class="meta-btn-danger" @click="handleReject(currentDetail.id)">Từ chối</el-button>
            <el-button v-if="currentDetail?.status?.toLowerCase() !== 'approved' && currentDetail?.status?.toLowerCase() !== 'rejected'" class="meta-btn-primary" @click="handleApprove(currentDetail.id)">Duyệt hợp lệ</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { attendanceApi } from '@/api/attendance'; 
import { ElMessage, ElMessageBox } from 'element-plus';

const attendances = ref([]);
const total = ref(0);
const page = ref(0);
const size = ref(10);
const isLoading = ref(false);
const filterStatus = ref(''); 
const fromDate = ref(null);
const toDate = ref(null);
const detailDialogVisible = ref(false);
const currentDetail = ref(null);

const fetchData = async () => {
  isLoading.value = true;
  try {
    const res = await attendanceApi.getAttendances(page.value, size.value, filterStatus.value || null, fromDate.value, toDate.value);
    const responseData = res.data?.data;
    attendances.value = responseData?.content || [];
    total.value = responseData?.totalElements || 0;
  } catch (error) {
    ElMessage.error('Lỗi tải danh sách chấm công');
  } finally {
    isLoading.value = false;
  }
};

const handlePageChange = (val) => { page.value = val - 1; fetchData(); };
const handleFilterChange = () => { page.value = 0; fetchData(); };

const openDetail = async (id) => {
  try {
    const res = await attendanceApi.getDetail(id);
    currentDetail.value = res.data?.data;
    detailDialogVisible.value = true;
  } catch (error) { ElMessage.error('Không thể tải chi tiết'); }
};

const handleApprove = (id) => {
  ElMessageBox.confirm('Bạn có chắc chắn muốn duyệt lượt chấm công này?', 'Xác nhận', { confirmButtonText: 'Duyệt', cancelButtonText: 'Hủy', type: 'success' }).then(async () => {
    try { await attendanceApi.approve(id); ElMessage.success("Đã duyệt thành công"); fetchData(); detailDialogVisible.value = false; } catch (e) { ElMessage.error("Lỗi khi duyệt"); }
  }).catch(() => {});
};

const handleReject = (id) => {
  ElMessageBox.prompt('Vui lòng nhập lý do từ chối:', 'Xác nhận từ chối', { confirmButtonText: 'Từ chối', cancelButtonText: 'Hủy', inputPattern: /\S+/, inputErrorMessage: 'Lý do không được để trống' }).then(async ({ value }) => {
    try { await attendanceApi.reject(id, value); ElMessage.success("Đã từ chối chấm công"); fetchData(); detailDialogVisible.value = false; } catch (e) { ElMessage.error("Lỗi khi từ chối"); }
  }).catch(() => {});
};

const formatDate = (dateString) => {
  if (!dateString) return '---';
  return new Date(dateString).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

const getStatusTagClass = (status) => {
  if (!status) return 'meta-tag-info';
  const s = status.toLowerCase();
  if (s === 'approved' || s === 'present') return 'meta-tag-success';
  if (s === 'rejected') return 'meta-tag-error';
  if (s === 'abnormal' || s === 'late') return 'meta-tag-warning';
  return 'meta-tag-info';
};

const formatStatus = (status) => {
  if (!status) return '---';
  const map = { 'present': 'Hợp lệ', 'late': 'Đi muộn', 'abnormal': 'Cần duyệt', 'approved': 'Đã duyệt', 'rejected': 'Từ chối' };
  return map[status.toLowerCase()] || status.toUpperCase();
};

onMounted(() => { fetchData(); });
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }

/* Radio Button Styling like Meta Segmented Control */
:deep(.meta-radio-group .el-radio-button__inner) {
  border: 1px solid #CED0D4;
  font-family: 'Montserrat', sans-serif;
  color: #5D6C7B;
  padding: 10px 20px;
  box-shadow: none !important;
}
:deep(.meta-radio-group .el-radio-button:first-child .el-radio-button__inner) { border-radius: 8px 0 0 8px; }
:deep(.meta-radio-group .el-radio-button:last-child .el-radio-button__inner) { border-radius: 0 8px 8px 0; }
:deep(.meta-radio-group .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #F1F4F7;
  color: #1C2B33;
  font-weight: 600;
  border-color: #CED0D4;
}

/* Buttons */
:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }

:deep(.meta-btn-danger) { border-radius: 100px !important; background-color: #E41E3F !important; border-color: #E41E3F !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-danger:hover) { background-color: #C80A28 !important; transform: scale(1.02); }

:deep(.meta-btn-secondary) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }

:deep(.meta-btn-secondary-sm) { background-color: rgba(0, 100, 224, 0.1) !important; color: #0064E0 !important; border: none !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px; padding: 8px 16px !important; transition: all 0.2s ease; }
:deep(.meta-btn-success-sm) { background-color: rgba(49, 162, 76, 0.1) !important; color: #007D1E !important; border: none !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px; padding: 8px 16px !important; transition: all 0.2s ease; }
:deep(.meta-btn-danger-sm) { background-color: rgba(228, 30, 63, 0.1) !important; color: #E41E3F !important; border: none !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px; padding: 8px 16px !important; transition: all 0.2s ease; }

/* Table */
:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 20px 0 !important; }

/* Tags */
:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error), :deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; border: none; padding: 6px 16px; height: auto; }
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
:deep(.meta-tag-info) { background-color: rgba(0, 100, 224, 0.15); color: #0064E0; }

/* Pagination */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) { background-color: #0064E0 !important; border-radius: 8px; }
:deep(.el-pagination.is-background .el-pager li) { border-radius: 8px; font-family: 'Montserrat', sans-serif; }

/* Dialog */
:deep(.meta-dialog) { border-radius: 20px !important; font-family: 'Montserrat', sans-serif; }
:deep(.meta-dialog .el-dialog__header) { border-bottom: 1px solid #DEE3E9; padding-bottom: 16px; margin-right: 0; }
:deep(.meta-dialog .el-dialog__title) { font-weight: 600; color: #1C2B33; }
</style>