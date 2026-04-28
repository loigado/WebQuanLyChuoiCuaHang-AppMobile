<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Đơn xin nghỉ phép</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Gửi đơn nghỉ phép và theo dõi trạng thái duyệt</p>
        </div>
        <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="showDialog = true">
          <span class="mr-2 text-[18px]">+</span> Tạo đơn nghỉ phép
        </el-button>
      </div>

      <!-- Danh sách đơn -->
      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="requests" v-loading="loading" class="meta-table" style="width: 100%">
          <el-table-column label="Loại nghỉ" width="160">
            <template #default="{row}">
              <el-tag :class="row.leaveType === 'Sick' ? 'meta-tag-error' : (row.leaveType === 'Annual' ? 'meta-tag-success' : 'meta-tag-info')">
                {{ leaveTypeMap[row.leaveType] || row.leaveType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Từ ngày" width="160">
            <template #default="{row}"><span class="text-[#1C2B33] font-medium">{{ row.startDate }}</span></template>
          </el-table-column>
          <el-table-column label="Đến ngày" width="160">
            <template #default="{row}"><span class="text-[#1C2B33] font-medium">{{ row.endDate }}</span></template>
          </el-table-column>
          <el-table-column label="Lý do" min-width="250" show-overflow-tooltip>
            <template #default="{row}"><span class="text-[#5D6C7B]">{{ row.reason }}</span></template>
          </el-table-column>
          <el-table-column label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="row.status === 'approved' ? 'meta-tag-success' : (row.status === 'rejected' ? 'meta-tag-error' : 'meta-tag-warning')">
                {{ statusMap[row.status] || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Lý do từ chối" min-width="200" show-overflow-tooltip>
            <template #default="{row}">
              <span v-if="row.rejectReason" class="text-[#E41E3F] italic text-[14px]">{{ row.rejectReason }}</span>
              <span v-else class="text-[#5D6C7B]">---</span>
            </template>
          </el-table-column>
          <el-table-column label="Ngày tạo" width="180">
            <template #default="{row}"><span class="text-[#5D6C7B] text-[13px]">{{ formatDate(row.createdAt) }}</span></template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- Dialog tạo đơn -->
      <el-dialog v-model="showDialog" title="Tạo đơn xin nghỉ phép" width="550px" class="meta-dialog" destroy-on-close>
        <div class="flex flex-col gap-6 pt-2">
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Loại nghỉ phép</label>
            <el-select v-model="form.leaveType" placeholder="Chọn loại" class="meta-input w-full">
              <el-option label="Nghỉ phép năm" value="Annual" />
              <el-option label="Nghỉ bệnh" value="Sick" />
              <el-option label="Nghỉ việc riêng" value="Private" />
            </el-select>
          </div>
          <div class="flex gap-4">
            <div class="flex-1 flex flex-col gap-2">
              <label class="text-[14px] font-bold text-[#1C2B33]">Từ ngày</label>
              <el-date-picker v-model="form.startDate" type="date" placeholder="Chọn ngày bắt đầu" 
                format="DD/MM/YYYY" value-format="YYYY-MM-DD" class="meta-input w-full" />
            </div>
            <div class="flex-1 flex flex-col gap-2">
              <label class="text-[14px] font-bold text-[#1C2B33]">Đến ngày</label>
              <el-date-picker v-model="form.endDate" type="date" placeholder="Chọn ngày kết thúc" 
                format="DD/MM/YYYY" value-format="YYYY-MM-DD" class="meta-input w-full" />
            </div>
          </div>
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Lý do xin nghỉ</label>
            <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="Nhập lý do chi tiết..." class="meta-input" />
          </div>
        </div>
        <template #footer>
          <div class="flex gap-3 justify-end pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary" @click="showDialog = false">Hủy</el-button>
            <el-button class="meta-btn-primary" :loading="submitting" @click="submitRequest">Gửi đơn</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus';

const auth = useAuthStore();
const requests = ref([]);
const loading = ref(false);
const showDialog = ref(false);
const submitting = ref(false);

const form = ref({ leaveType: 'Annual', startDate: '', endDate: '', reason: '' });

const leaveTypeMap = { Annual: 'Nghỉ phép năm', Sick: 'Nghỉ bệnh', Private: 'Nghỉ việc riêng' };
const statusMap = { pending: 'Chờ duyệt', approved: 'Đã duyệt', rejected: 'Bị từ chối' };

const fetchRequests = async () => {
  loading.value = true;
  try {
    // ✅ ĐÃ SỬA: Employee xem đơn của chính mình qua /leave-requests/my
    const res = await api.get('/leave-requests/my', { params: { page: 0, size: 20 } });
    requests.value = res.data?.data?.content || res.data?.data || [];
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
};

const submitRequest = async () => {
  if (!form.value.startDate || !form.value.endDate) return ElMessage.warning('Chọn ngày bắt đầu và kết thúc');
  if (!form.value.reason.trim()) return ElMessage.warning('Nhập lý do xin nghỉ');

  submitting.value = true;
  try {
    const userId = auth.user?.id || auth.user?.userId;
    await api.post('/leave-requests', {
      user: { userId },
      leaveType: form.value.leaveType,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      reason: form.value.reason
    });
    ElMessage.success('Gửi đơn nghỉ phép thành công!');
    showDialog.value = false;
    form.value = { leaveType: 'Annual', startDate: '', endDate: '', reason: '' };
    fetchRequests();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Lỗi gửi đơn');
  } finally {
    submitting.value = false;
  }
};

const formatDate = (d) => {
  if (!d) return '---';
  return new Date(d).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

onMounted(fetchRequests);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }
:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }
:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; }
:deep(.meta-btn-secondary) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; }
:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper), :deep(.meta-input .el-textarea__inner) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; }
:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 20px 0 !important; }
:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error), :deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; border: none; padding: 6px 16px; height: auto; }
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
:deep(.meta-tag-info) { background-color: rgba(0, 100, 224, 0.15); color: #0064E0; }
:deep(.meta-dialog) { border-radius: 20px !important; font-family: 'Montserrat', sans-serif; }
:deep(.meta-dialog .el-dialog__header) { border-bottom: 1px solid #DEE3E9; padding-bottom: 16px; margin-right: 0; }
:deep(.meta-dialog .el-dialog__title) { font-weight: 600; color: #1C2B33; }
</style>
