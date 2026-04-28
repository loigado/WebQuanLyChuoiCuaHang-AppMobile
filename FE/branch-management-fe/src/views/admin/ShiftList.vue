<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col lg:flex-row justify-between items-start lg:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Quản lý ca làm việc</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Thiết lập lịch làm việc và phân công nhân sự tại cửa hàng</p>
        </div>
        
        <div class="flex flex-wrap gap-4 items-center bg-white p-2 rounded-[100px] shadow-[0_2px_4px_0_rgba(0,0,0,0.05)] pl-4">
          <el-select v-if="isAdmin" v-model="selectedBranch" placeholder="Chọn chi nhánh" class="w-48 meta-input-pill" @change="handleBranchChange">
            <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
          </el-select>

          <el-date-picker v-model="selectedDate" type="date" placeholder="Chọn ngày" format="DD/MM/YYYY" value-format="YYYY-MM-DD" @change="fetchData" class="w-40 meta-input-pill" :clearable="false" />
          
          <el-button v-if="auth.role === 'ROLE_MANAGER'" class="meta-btn-primary" @click="openCreateDialog">
            <span class="mr-1 text-[16px]">+</span> Tạo ca mới
          </el-button>
        </div>
      </div>

      <div v-loading="loading" class="min-h-[400px]">
        <div v-if="shifts.length === 0" class="py-20 text-center text-[#5D6C7B] bg-white rounded-[24px] border border-[#DEE3E9]">
          <span class="text-[48px] block mb-4">📅</span>
          <span class="text-[18px]">Chưa có ca làm việc nào cho ngày này.</span>
        </div>
        
        <div v-else class="custom-shift-grid">
          <el-card v-for="s in shifts" :key="s.shiftId" shadow="never" class="meta-shift-card">
            
            <div class="shift-header">
              <div>
                <div class="shift-title-wrapper">
                  <h3 class="shift-title">{{ s.shiftName }}</h3>
                  <el-tag :class="getShiftTypeTag(s.shiftType)" effect="plain" round>
                    {{ s.shiftType?.toUpperCase() === 'MORNING' ? 'SÁNG' : (s.shiftType?.toUpperCase() === 'AFTERNOON' ? 'CHIỀU' : 'TỐI') }}
                  </el-tag>
                </div>
                <div class="shift-time">
                  <span>🕒</span> {{ s.startTime.substring(0,5) }} - {{ s.endTime.substring(0,5) }}
                </div>
              </div>
              <button v-if="auth.role === 'ROLE_MANAGER'" class="btn-icon-danger" @click="handleDeleteShift(s.shiftId)" title="Xóa ca">✕</button>
            </div>

            <div class="shift-body">
              <p class="shift-section-title">Nhân sự phụ trách ({{ s.assignedCount }})</p>
              
              <div class="user-list-wrapper">
                <div v-for="user in s.assignedUsers" :key="user.userShiftId" class="user-item">
                  <div class="user-info">
                    <div class="user-avatar">{{ user.fullName.charAt(0).toUpperCase() }}</div>
                    <div>
                      <div class="user-name">{{ user.fullName }}</div>
                      <div class="user-role">{{ user.role }}</div>
                    </div>
                  </div>
                  
                  <div class="user-actions" v-if="auth.role === 'ROLE_MANAGER'">
                    <button class="btn-icon-secondary" @click="handleEditAssign(s, user)" title="Đổi NV">✎</button>
                    <button class="btn-icon-danger" @click="handleRemove(s.shiftId, user.userShiftId)" title="Xóa khỏi ca">✕</button>
                  </div>
                </div>
              </div>

              <button v-if="auth.role === 'ROLE_MANAGER'" class="btn-dashed-full" @click="openAssignDialog(s)">
                + Gán nhân sự
              </button>
            </div>
          </el-card>
        </div>
      </div>

      <el-dialog v-model="createDialog" title="Thiết lập ca làm việc mới" width="450px" class="meta-dialog" destroy-on-close>
        <div class="flex flex-col gap-4 pt-2">
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Tên ca</label>
            <input v-model="newShift.shiftName" class="meta-native-input" placeholder="Ví dụ: Ca Sáng 1" />
          </div>
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Loại ca</label>
            <el-select v-model="newShift.shiftType" class="meta-input w-full">
              <el-option label="Sáng (Morning)" value="morning" />
              <el-option label="Chiều (Afternoon)" value="afternoon" />
              <el-option label="Tối (Evening)" value="evening" />
            </el-select>
          </div>
          <div class="flex gap-4">
            <div class="flex-1 flex flex-col gap-2">
              <label class="text-[14px] font-bold text-[#1C2B33]">Bắt đầu</label>
              <el-time-picker v-model="newShift.startTime" format="HH:mm" value-format="HH:mm:ss" class="meta-input w-full" />
            </div>
            <div class="flex-1 flex flex-col gap-2">
              <label class="text-[14px] font-bold text-[#1C2B33]">Kết thúc</label>
              <el-time-picker v-model="newShift.endTime" format="HH:mm" value-format="HH:mm:ss" class="meta-input w-full" />
            </div>
          </div>
        </div>
        <template #footer>
          <div class="flex gap-3 pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary flex-1" @click="createDialog = false">Hủy</el-button>
            <el-button class="meta-btn-primary flex-1" @click="submitCreateShift" :loading="submitting">Xác nhận tạo</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="assignDialog" :title="'Phân ca: ' + currentShift?.shiftName" width="400px" class="meta-dialog">
        <div class="mt-4 mb-2 flex flex-col gap-2">
          <label class="text-[14px] font-bold text-[#1C2B33]">Chọn nhân viên</label>
          <el-select v-model="selectedUserId" placeholder="Tìm kiếm nhân viên..." class="meta-input w-full" filterable>
            <el-option v-for="u in employees" :key="u.userId" :label="u.fullName + ' (' + u.role + ')'" :value="u.userId" />
          </el-select>
        </div>
        <template #footer>
          <div class="flex gap-3 pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary flex-1" @click="assignDialog = false">Hủy</el-button>
            <el-button class="meta-btn-primary flex-1" @click="submitAssign" :disabled="!selectedUserId">Xác nhận</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="editAssignDialog" title="Thay đổi nhân sự" width="400px" class="meta-dialog">
        <div class="mt-4 mb-2 flex flex-col gap-2">
          <label class="text-[14px] font-bold text-[#1C2B33]">Chọn nhân sự thay thế</label>
          <el-select v-model="editSelectedUserId" placeholder="Tìm kiếm..." class="meta-input w-full" filterable>
            <el-option v-for="u in employees" :key="u.userId" :label="u.fullName + ' (' + u.role + ')'" :value="u.userId" />
          </el-select>
        </div>
        <template #footer>
          <div class="flex gap-3 pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary flex-1" @click="editAssignDialog = false">Hủy</el-button>
            <el-button class="meta-btn-primary flex-1" @click="submitEditAssign" :disabled="!editSelectedUserId">Lưu</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { shiftApi } from '@/api/shift';
import api from '@/api/axios.js'; 
import { useAuthStore } from '@/stores/auth';
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';

const auth = useAuthStore();
const shifts = ref([]);
const employees = ref([]);
const branches = ref([]); 
const selectedDate = ref(dayjs().format('YYYY-MM-DD'));
const loading = ref(false);
const submitting = ref(false);

const createDialog = ref(false);
const assignDialog = ref(false);
const editAssignDialog = ref(false); 

const currentShift = ref(null);
const selectedUserId = ref(null);
const currentEditUserShiftId = ref(null); 
const editSelectedUserId = ref(null); 

const newShift = ref({ shiftName: '', shiftType: 'morning', startTime: '08:00:00', endTime: '12:00:00', branchId: null, date: null });

const isAdmin = computed(() => auth.role === 'ADMIN' || auth.role === 'ROLE_ADMIN');
const selectedBranch = ref(auth.user?.branch?.branchId || 1);

const fetchBranches = async () => {
  if (isAdmin.value) {
    try {
      const res = await api.get('/admin/branches/active');
      branches.value = res.data?.data || [];
      if (branches.value.length > 0 && !auth.user?.branch?.branchId) {
        selectedBranch.value = branches.value[0].branchId;
      }
    } catch (error) { console.error("Lỗi tải chi nhánh"); }
  }
};

const handleBranchChange = () => { fetchData(); fetchEmployees(); };

const fetchData = async () => {
  loading.value = true;
  try {
    const branchId = selectedBranch.value; 
    const dateStr = dayjs(selectedDate.value).format('YYYY-MM-DD');
    const res = await shiftApi.getByBranch(branchId, dateStr, dateStr);
    if (res?.data?.success) shifts.value = res.data.data || [];
  } catch (error) { shifts.value = []; } 
  finally { loading.value = false; }
};

const fetchEmployees = async () => {
  try {
    const currentBranchId = selectedBranch.value;
    const resEmp = await api.get('/manager/employees', { params: { branchId: currentBranchId } });
    if (resEmp?.data?.success) employees.value = resEmp.data.data;
  } catch (error) { console.error("Lỗi tải nhân viên"); }
};

const getShiftTypeTag = (type) => {
  const t = type?.toUpperCase();
  if (t === 'MORNING') return 'meta-tag-info';
  if (t === 'AFTERNOON') return 'meta-tag-warning';
  return 'meta-tag-dark';
};

const openCreateDialog = () => {
  newShift.value = { shiftName: '', shiftType: 'morning', startTime: '08:00:00', endTime: '12:00:00', branchId: selectedBranch.value, date: selectedDate.value };
  createDialog.value = true;
};

const submitCreateShift = async () => {
  if (!newShift.value.shiftName) return ElMessage.warning("Vui lòng nhập tên ca");
  submitting.value = true;
  try {
    await shiftApi.create(newShift.value);
    ElMessage.success("Đã tạo ca thành công!");
    createDialog.value = false;
    fetchData();
  } catch (e) { ElMessage.error("Lỗi tạo ca"); } 
  finally { submitting.value = false; }
};

const handleDeleteShift = (shiftId) => {
  ElMessageBox.confirm('Chắc chắn xóa ca làm việc này?', 'Cảnh báo', { type: 'warning', confirmButtonText: 'Xóa', cancelButtonText: 'Hủy' }).then(async () => {
    await shiftApi.deleteShift(shiftId);
    ElMessage.success("Xóa thành công!");
    fetchData();
  }).catch(() => {});
};

const openAssignDialog = (shift) => {
  currentShift.value = shift; selectedUserId.value = null; assignDialog.value = true;
};

const submitAssign = async () => {
  try {
    await shiftApi.assignUser(currentShift.value.shiftId, selectedUserId.value);
    ElMessage.success("Phân ca thành công");
    assignDialog.value = false;
    fetchData();
  } catch (e) { ElMessage.error("Lỗi gán ca"); }
};

const handleEditAssign = (shift, user) => {
  currentEditUserShiftId.value = user.userShiftId; editSelectedUserId.value = user.userId; editAssignDialog.value = true;
};

const submitEditAssign = async () => {
  try {
    await shiftApi.updateUserAssign(currentEditUserShiftId.value, editSelectedUserId.value);
    ElMessage.success("Đổi thành công!");
    editAssignDialog.value = false;
    fetchData();
  } catch (e) { ElMessage.error("Lỗi đổi nhân viên"); }
};

const handleRemove = (shiftId, userShiftId) => {
  ElMessageBox.confirm('Hủy phân ca?', 'Xác nhận').then(async () => {
    await shiftApi.removeAssign(shiftId, userShiftId);
    ElMessage.success("Đã hủy phân ca");
    fetchData();
  }).catch(() => {});
};

onMounted(async () => {
  await fetchBranches(); 
  fetchData();
  fetchEmployees();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

/* ==============================================================
   GRID THUẦN (Bảo vệ giao diện 100% không vỡ dù Tailwind lỗi)
   ============================================================== */
.custom-shift-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  align-items: stretch;
}

:deep(.meta-shift-card) {
  border-radius: 20px !important;
  border: 1px solid #DEE3E9 !important;
  background-color: #FFFFFF !important;
  box-shadow: 0 12px 28px 0 rgba(0,0,0,0.05) !important;
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.meta-shift-card .el-card__body) {
  padding: 24px !important;
  display: flex;
  flex-direction: column;
  flex: 1;
}

/* Card Header */
.shift-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #F1F4F7;
  padding-bottom: 16px;
  margin-bottom: 16px;
}
.shift-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}
.shift-title {
  font-size: 20px;
  font-weight: 700;
  color: #1C2B33;
  margin: 0;
}
.shift-time {
  font-size: 14px;
  color: #5D6C7B;
  font-weight: 500;
}

/* Card Body */
.shift-body {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.shift-section-title {
  font-size: 14px;
  font-weight: 700;
  color: #1C2B33;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 12px 0;
}
.user-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
  flex: 1;
}

/* User Item Style */
.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #F7F8FA;
  border: 1px solid #DEE3E9;
  border-radius: 12px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #E8F3FF;
  color: #0064E0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}
.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1C2B33;
  line-height: 1.2;
}
.user-role {
  font-size: 12px;
  color: #5D6C7B;
  margin-top: 2px;
}
.user-actions {
  display: flex;
  gap: 4px;
}

/* Icons & Buttons */
.btn-icon-danger, .btn-icon-secondary {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
}
.btn-icon-danger { color: #65676B; }
.btn-icon-danger:hover { color: #E41E3F; background-color: rgba(228, 30, 63, 0.1); }
.btn-icon-secondary { color: #65676B; }
.btn-icon-secondary:hover { color: #0064E0; background-color: rgba(0, 100, 224, 0.1); }

.btn-dashed-full {
  width: 100%;
  background-color: transparent;
  color: #0064E0;
  border: 2px dashed #CED0D4;
  border-radius: 12px;
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 0;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: auto;
}
.btn-dashed-full:hover {
  border-color: #0064E0;
  background-color: #E8F3FF;
}

/* ==============================================================
   OTHER STYLES
   ============================================================== */
:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }
:deep(.meta-btn-secondary) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }

:deep(.meta-input-pill .el-input__wrapper), :deep(.meta-input-pill .el-select__wrapper) { border-radius: 100px !important; border: none !important; box-shadow: none !important; background-color: transparent !important; padding: 8px 16px; }
:deep(.meta-input-pill .el-input__wrapper.is-focus), :deep(.meta-input-pill .el-select__wrapper.is-focus) { background-color: #F1F4F7 !important; }

.meta-native-input { width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid #CED0D4; font-family: 'Montserrat', sans-serif; font-size: 14px; color: #1C2B33; }
.meta-native-input:focus { outline: none; border-color: #0064E0; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2); }

:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; padding: 8px 12px; }
:deep(.meta-input .el-input__wrapper.is-focus), :deep(.meta-input .el-select__wrapper.is-focus) { border-color: #0064E0 !important; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-tag-info), :deep(.meta-tag-warning), :deep(.meta-tag-dark) { font-weight: 700; border-radius: 100px; padding: 0 12px; height: 24px; line-height: 22px; border: 1px solid transparent; }
:deep(.meta-tag-info) { color: #0064E0; background-color: rgba(0, 100, 224, 0.1); border-color: rgba(0, 100, 224, 0.2); }
:deep(.meta-tag-warning) { color: #D6311F; background-color: rgba(214, 49, 31, 0.1); border-color: rgba(214, 49, 31, 0.2); }
:deep(.meta-tag-dark) { color: #1C2B33; background-color: rgba(28, 43, 51, 0.1); border-color: rgba(28, 43, 51, 0.2); }

:deep(.meta-dialog) { border-radius: 20px !important; font-family: 'Montserrat', sans-serif; }
:deep(.meta-dialog .el-dialog__header) { margin-right: 0; border-bottom: 1px solid #DEE3E9; padding-bottom: 16px; }
:deep(.meta-dialog .el-dialog__title) { font-weight: 600; color: #1C2B33; }
</style>