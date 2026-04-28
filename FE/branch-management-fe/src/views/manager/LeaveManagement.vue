<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Duyệt Đơn Nghỉ Phép</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Quản lý và phê duyệt thời gian vắng mặt của nhân sự chi nhánh</p>
        </div>
      </div>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="leaveRequests" v-loading="loading" class="meta-table" style="width: 100%">
          <el-table-column prop="fullName" label="Nhân viên" min-width="200">
            <template #default="{row}"><span class="font-medium text-[#1C2B33]">{{ row.fullName }}</span></template>
          </el-table-column>
          
          <el-table-column label="Loại phép" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="getLeaveTypeTagClass(row.leaveType)">
                {{ getLeaveTypeName(row.leaveType) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="Thời gian nghỉ" min-width="250">
            <template #default="{row}">
              <div class="text-[#5D6C7B] text-[14px]">
                <span class="font-medium text-[#1C2B33]">{{ formatDate(row.startDate) }}</span> 
                <span class="mx-2 text-[#CED0D4]">➔</span> 
                <span class="font-medium text-[#1C2B33]">{{ formatDate(row.endDate) }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="reason" label="Lý do chi tiết" min-width="300" show-overflow-tooltip>
            <template #default="{row}"><span class="text-[#5D6C7B]">{{ row.reason }}</span></template>
          </el-table-column>
          
          <el-table-column label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="getStatusTagClass(row.status)">
                {{ formatStatus(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="Thao tác" width="200" align="center" fixed="right">
            <template #default="scope">
              <div v-if="auth.role === 'ROLE_MANAGER' && scope.row.status === 'pending'" class="flex gap-2 justify-center">
                <el-button class="meta-btn-success-sm" @click="handleApprove(scope.row.leaveId)">Duyệt đơn</el-button>
                <el-button class="meta-btn-danger-sm" @click="openRejectDialog(scope.row.leaveId)">Từ chối</el-button>
              </div>
              <div v-else-if="scope.row.status !== 'pending'">
                 <span v-if="scope.row.status === 'approved'" class="text-[#31A24C] font-bold text-[14px]">Đã được duyệt</span>
                 <span v-if="scope.row.status === 'rejected'" class="text-[#E41E3F] font-bold text-[14px]" :title="scope.row.rejectReason">Bị từ chối</span>
              </div>
              <div v-else>
                 <span class="text-[#8595A4] text-[13px] italic bg-[#F1F4F7] px-3 py-1 rounded-md">Quyền Manager</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="!loading && leaveRequests.length === 0" class="py-16 text-center text-[#5D6C7B]">
          <span class="text-[32px] block mb-4">🏖️</span>
          <span class="text-[16px]">Hiện không có đơn xin nghỉ phép nào cần xử lý.</span>
        </div>
      </el-card>

      <el-dialog v-model="rejectDialogVisible" title="Lý do từ chối đơn phép" width="450px" class="meta-dialog" destroy-on-close>
        <div class="flex flex-col gap-2 pt-2">
          <label class="text-[14px] font-bold text-[#1C2B33]">Chi tiết lý do <span class="text-[#E41E3F]">*</span></label>
          <textarea v-model="rejectReason" class="meta-native-input resize-none" rows="4" placeholder="Nhập lý do phản hồi cho nhân viên..."></textarea>
        </div>
        <template #footer>
          <div class="flex gap-3 justify-end pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary" @click="rejectDialogVisible = false">Hủy bỏ</el-button>
            <el-button class="meta-btn-danger" @click="handleReject">Xác nhận từ chối</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { leaveApi } from '@/api/leaveRequest';
import { useAuthStore } from '@/stores/auth';
import { ElMessage, ElMessageBox } from 'element-plus';

const auth = useAuthStore();
const leaveRequests = ref([]);
const loading = ref(false);
const rejectDialogVisible = ref(false);
const rejectReason = ref('');
const selectedLeaveId = ref(null);

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await leaveApi.getForManager({ page: 0, size: 100 });
    leaveRequests.value = res.data?.data || [];
  } catch (error) {
    ElMessage.error("Không thể tải danh sách đơn xin nghỉ");
  } finally {
    loading.value = false;
  }
};

const handleApprove = (leaveId) => {
  ElMessageBox.confirm('Bạn có chắc chắn muốn DUYỆT đơn nghỉ phép này?', 'Xác nhận duyệt', { confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy', type: 'success' }).then(async () => {
    try {
      await leaveApi.approve(leaveId);
      ElMessage.success("Đã duyệt đơn thành công");
      fetchData();
    } catch (error) {
      ElMessage.error("Lỗi khi duyệt đơn");
    }
  }).catch(() => {});
};

const openRejectDialog = (leaveId) => {
  selectedLeaveId.value = leaveId;
  rejectReason.value = '';
  rejectDialogVisible.value = true;
};

const handleReject = async () => {
  if (!rejectReason.value.trim()) return ElMessage.warning("Vui lòng nhập lý do từ chối!");
  try {
    await leaveApi.reject(selectedLeaveId.value, rejectReason.value);
    ElMessage.success("Đã từ chối đơn nghỉ phép");
    rejectDialogVisible.value = false;
    fetchData();
  } catch (error) {
    ElMessage.error("Lỗi khi từ chối đơn");
  }
};

const getStatusTagClass = (s) => {
  if (s === 'approved') return 'meta-tag-success';
  if (s === 'rejected') return 'meta-tag-error';
  return 'meta-tag-warning';
};

const formatStatus = (s) => {
  if (s === 'approved') return 'Đã duyệt';
  if (s === 'rejected') return 'Từ chối';
  return 'Chờ duyệt';
};

const getLeaveTypeName = (type) => {
  const map = { 'Annual': 'Phép năm', 'Sick': 'Nghỉ ốm', 'Private': 'Việc riêng' };
  return map[type] || type;
};

const getLeaveTypeTagClass = (t) => {
  if (t === 'Sick' || t === 'Nghỉ bệnh') return 'meta-tag-info';
  return 'meta-tag-primary';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
};

onMounted(fetchData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }

:deep(.meta-btn-danger) { border-radius: 100px !important; background-color: #E41E3F !important; border-color: #E41E3F !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-danger:hover) { background-color: #C80A28 !important; transform: scale(1.02); }

:deep(.meta-btn-secondary) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-secondary:hover) { background-color: rgba(70, 90, 105, 0.05) !important; }

:deep(.meta-btn-success-sm) { background-color: rgba(49, 162, 76, 0.1) !important; color: #007D1E !important; border: none !important; border-radius: 100px !important; font-weight: 600; padding: 8px 16px !important; transition: all 0.2s ease; }
:deep(.meta-btn-success-sm:hover) { background-color: rgba(49, 162, 76, 0.2) !important; }
:deep(.meta-btn-danger-sm) { background-color: rgba(228, 30, 63, 0.1) !important; color: #E41E3F !important; border: none !important; border-radius: 100px !important; font-weight: 600; padding: 8px 16px !important; transition: all 0.2s ease; }
:deep(.meta-btn-danger-sm:hover) { background-color: rgba(228, 30, 63, 0.2) !important; }

.meta-native-input { width: 100%; padding: 12px 16px; border-radius: 8px; border: 1px solid #CED0D4; font-family: 'Montserrat', sans-serif; font-size: 14px; color: #1C2B33; transition: border-color 0.2s, box-shadow 0.2s; }
.meta-native-input:focus { outline: none; border-color: #E41E3F; box-shadow: 0 0 0 3px rgba(228, 30, 63, 0.2); }

:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 20px 0 !important; }

:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error), :deep(.meta-tag-primary), :deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; border: none; padding: 6px 16px; height: auto; }
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
:deep(.meta-tag-primary) { background-color: rgba(0, 100, 224, 0.15); color: #0064E0; }
:deep(.meta-tag-info) { background-color: #F1F4F7; color: #5D6C7B; }

:deep(.meta-dialog) { border-radius: 20px !important; font-family: 'Montserrat', sans-serif; }
:deep(.meta-dialog .el-dialog__header) { border-bottom: 1px solid #DEE3E9; padding-bottom: 16px; margin-right: 0; }
:deep(.meta-dialog .el-dialog__title) { font-weight: 600; color: #1C2B33; }
</style>