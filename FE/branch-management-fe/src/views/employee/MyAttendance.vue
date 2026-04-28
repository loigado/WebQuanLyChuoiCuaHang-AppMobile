<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="mb-12">
        <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Chấm công cá nhân</h2>
        <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Check-in/Check-out bằng GPS và ảnh xác thực</p>
      </div>

      <!-- Check-in / Check-out Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
        <!-- CHECK-IN -->
        <el-card shadow="never" class="meta-card border-t-[4px] border-t-[#31A24C]">
          <div class="flex flex-col items-center gap-6 py-4">
            <div class="w-20 h-20 rounded-full bg-[#E6F4EA] text-[#31A24C] flex items-center justify-center text-[36px]">📥</div>
            <h3 class="text-[22px] font-bold text-[#1C2B33]">Check-in</h3>
            <p class="text-[14px] text-[#5D6C7B] text-center">Chụp ảnh selfie và xác nhận vị trí GPS tại cửa hàng</p>
            
            <div class="w-full">
              <label class="text-[14px] font-bold text-[#1C2B33] block mb-2">📷 Ảnh xác thực</label>
              <input type="file" accept="image/*" capture="user" @change="onPhotoSelected($event, 'checkin')" 
                class="meta-native-input w-full" />
              <div v-if="checkinPhoto" class="mt-3 flex justify-center">
                <img :src="checkinPreview" class="max-h-40 rounded-[12px] border border-[#DEE3E9]" />
              </div>
            </div>

            <div v-if="gpsStatus" class="text-[13px] px-4 py-2 rounded-[8px] w-full text-center"
              :class="gpsStatus === 'ok' ? 'bg-[#E6F4EA] text-[#007D1E]' : 'bg-[#FCE8EB] text-[#E41E3F]'">
              {{ gpsStatus === 'ok' ? `📍 Vị trí: ${latitude?.toFixed(5)}, ${longitude?.toFixed(5)}` : '❌ Không lấy được GPS' }}
            </div>

            <el-button class="meta-btn-primary w-full" :loading="loadingCheckin" @click="handleCheckin" :disabled="!checkinPhoto || gpsStatus !== 'ok'">
              ✅ Xác nhận Check-in
            </el-button>
          </div>
        </el-card>

        <!-- CHECK-OUT -->
        <el-card shadow="never" class="meta-card border-t-[4px] border-t-[#E41E3F]">
          <div class="flex flex-col items-center gap-6 py-4">
            <div class="w-20 h-20 rounded-full bg-[#FCE8EB] text-[#E41E3F] flex items-center justify-center text-[36px]">📤</div>
            <h3 class="text-[22px] font-bold text-[#1C2B33]">Check-out</h3>
            <p class="text-[14px] text-[#5D6C7B] text-center">Kết thúc ca làm việc và xác nhận vị trí GPS</p>
            
            <div class="w-full">
              <label class="text-[14px] font-bold text-[#1C2B33] block mb-2">📷 Ảnh xác thực (tuỳ chọn)</label>
              <input type="file" accept="image/*" capture="user" @change="onPhotoSelected($event, 'checkout')"
                class="meta-native-input w-full" />
            </div>

            <div v-if="gpsStatus" class="text-[13px] px-4 py-2 rounded-[8px] w-full text-center"
              :class="gpsStatus === 'ok' ? 'bg-[#E6F4EA] text-[#007D1E]' : 'bg-[#FCE8EB] text-[#E41E3F]'">
              {{ gpsStatus === 'ok' ? `📍 Vị trí: ${latitude?.toFixed(5)}, ${longitude?.toFixed(5)}` : '❌ Không lấy được GPS' }}
            </div>

            <el-button class="meta-btn-danger w-full" :loading="loadingCheckout" @click="handleCheckout" :disabled="gpsStatus !== 'ok'">
              🚪 Xác nhận Check-out
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- Lịch sử chấm công -->
      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <h3 class="text-[20px] font-medium text-[#1C2B33] mb-6">📅 Lịch sử chấm công gần đây</h3>
        <el-table :data="history" v-loading="loadingHistory" class="meta-table" style="width: 100%">
          <el-table-column label="Check-in" min-width="200">
            <template #default="{row}">
              <span class="text-[#1C2B33]">{{ formatDate(row.checkInTime || row.check_in) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Check-out" min-width="200">
            <template #default="{row}">
              <span class="text-[#5D6C7B]">{{ formatDate(row.checkOutTime || row.check_out) || '---' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Tổng giờ" width="120" align="center">
            <template #default="{row}">
              <span class="text-[#0064E0] font-bold text-[18px]">{{ row.totalHours || row.total_hours || '---' }}</span>
              <span class="text-[#5D6C7B] text-[13px]"> h</span>
            </template>
          </el-table-column>
          <el-table-column label="Trạng thái" width="140" align="center">
            <template #default="{row}">
              <el-tag :class="getStatusClass(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Chi tiết" width="120" align="center" fixed="right">
            <template #default="{row}">
              <el-button circle size="small" type="primary" @click="openDetail(row)">🔍</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- DETAIL DIALOG -->
      <el-dialog v-model="detailDialogVisible" title="Chi tiết lượt chấm công" width="550px" class="meta-dialog" destroy-on-close>
        <div v-if="currentDetail" class="pt-2">
          <div class="bg-[#F7F8FA] p-4 rounded-[12px] text-[13px] space-y-2 mb-4 border border-[#DEE3E9]">
            <div class="flex justify-between items-center">
              <span class="font-bold text-[#1C2B33] text-[14px]">Ngày chấm công: {{ formatDate(currentDetail.checkInTime || currentDetail.check_in).split(' ')[1] }}</span>
              <span class="font-mono text-[11px] text-[#5D6C7B]">GPS: {{ currentDetail.latitude?.toFixed(4) }}, {{ currentDetail.longitude?.toFixed(4) }}</span>
            </div>
            
            <div class="grid grid-cols-2 gap-4 py-2 border-y border-[#DEE3E9] border-dashed">
              <div class="flex flex-col gap-1">
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold">📥 Giờ vào ca:</span>
                <span class="text-[#1C2B33] font-medium">{{ formatDate(currentDetail.checkInTime || currentDetail.check_in) }}</span>
              </div>
              <div class="flex flex-col gap-1">
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold">📤 Giờ kết ca:</span>
                <span class="text-[#1C2B33] font-medium">{{ formatDate(currentDetail.checkOutTime || currentDetail.check_out) || '---' }}</span>
              </div>
              <div class="flex flex-col gap-1">
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold">⏱️ Tổng công:</span>
                <span class="text-[#0064E0] font-bold">{{ currentDetail.totalHours || currentDetail.total_hours || 0 }} h</span>
              </div>
              <div class="flex flex-col gap-1">
                <span class="text-[10px] text-[#5D6C7B] uppercase font-bold">🎯 Trạng thái:</span>
                <el-tag :class="getStatusClass(currentDetail.status)" size="small" style="width: fit-content; font-weight: bold;">{{ getStatusText(currentDetail.status) }}</el-tag>
              </div>
            </div>
          </div>

          <!-- Ảnh ngang (Ép bằng CSS inline) -->
          <div style="display: flex !important; flex-direction: row !important; justify-content: center !important; gap: 20px !important;">
            <div v-if="currentDetail.checkInPhotoUrl || currentDetail.check_in_photo_url" style="text-align: center;">
              <p style="font-size: 10px; font-weight: bold; color: #8595A4; margin-bottom: 4px; text-transform: uppercase;">Check-in</p>
              <div style="background: black; border-radius: 8px; padding: 2px; border: 1px solid #DEE3E9;">
                <el-image :src="'http://localhost:8081' + (currentDetail.checkInPhotoUrl || currentDetail.check_in_photo_url)" 
                  style="width: 100px; height: 100px; display: block;"
                  class="rounded-md object-cover" 
                  :preview-src-list="['http://localhost:8081' + (currentDetail.checkInPhotoUrl || currentDetail.check_in_photo_url)]" 
                  fit="cover" />
              </div>
            </div>
            <div v-if="currentDetail.checkOutPhotoUrl || currentDetail.check_out_photo_url" style="text-align: center;">
              <p style="font-size: 10px; font-weight: bold; color: #8595A4; margin-bottom: 4px; text-transform: uppercase;">Check-out</p>
              <div style="background: black; border-radius: 8px; padding: 2px; border: 1px solid #DEE3E9;">
                <el-image :src="'http://localhost:8081' + (currentDetail.checkOutPhotoUrl || currentDetail.check_out_photo_url)" 
                  style="width: 100px; height: 100px; display: block;"
                  class="rounded-md object-cover" 
                  :preview-src-list="['http://localhost:8081' + (currentDetail.checkOutPhotoUrl || currentDetail.check_out_photo_url)]" 
                  fit="cover" />
              </div>
            </div>
          </div>
          
          <div v-if="currentDetail.rejectReason || currentDetail.reject_reason" class="mt-4 p-3 bg-rose-50 border border-rose-100 rounded-xl">
             <span class="text-rose-600 font-bold text-[12px] block mb-1">Lý do từ chối:</span>
             <span class="text-rose-700 italic text-[12px]">{{ currentDetail.rejectReason || currentDetail.reject_reason }}</span>
          </div>
        </div>
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
const latitude = ref(null);
const longitude = ref(null);
const gpsStatus = ref(null);
const checkinPhoto = ref(null);
const checkinPreview = ref(null);
const checkoutPhoto = ref(null);
const loadingCheckin = ref(false);
const loadingCheckout = ref(false);
const loadingHistory = ref(false);
const history = ref([]);
const detailDialogVisible = ref(false);
const currentDetail = ref(null);

const openDetail = (row) => {
  currentDetail.value = row;
  detailDialogVisible.value = true;
};

const branchId = auth.user?.branchId || auth.branchId || sessionStorage.getItem('branchId');

// Lấy GPS
const getLocation = () => {
  if (!navigator.geolocation) {
    gpsStatus.value = 'error';
    return;
  }
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      latitude.value = pos.coords.latitude;
      longitude.value = pos.coords.longitude;
      gpsStatus.value = 'ok';
    },
    () => { gpsStatus.value = 'error'; },
    { enableHighAccuracy: true, timeout: 10000 }
  );
};

const onPhotoSelected = (event, type) => {
  const file = event.target.files[0];
  if (!file) return;
  if (type === 'checkin') {
    checkinPhoto.value = file;
    checkinPreview.value = URL.createObjectURL(file);
  } else {
    checkoutPhoto.value = file;
  }
};

const handleCheckin = async () => {
  if (!branchId) return ElMessage.error('Không xác định được chi nhánh');
  loadingCheckin.value = true;
  try {
    const formData = new FormData();
    formData.append('branchId', branchId);
    formData.append('latitude', latitude.value);
    formData.append('longitude', longitude.value);
    formData.append('photo', checkinPhoto.value);
    
    await api.post('/manager/attendances/check-in', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    ElMessage.success('Check-in thành công!');
    checkinPhoto.value = null;
    checkinPreview.value = null;
    fetchHistory();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Lỗi check-in');
  } finally {
    loadingCheckin.value = false;
  }
};

const handleCheckout = async () => {
  if (!branchId) return ElMessage.error('Không xác định được chi nhánh');
  loadingCheckout.value = true;
  try {
    const formData = new FormData();
    formData.append('branchId', branchId);
    formData.append('latitude', latitude.value);
    formData.append('longitude', longitude.value);
    if (checkoutPhoto.value) formData.append('photo', checkoutPhoto.value);
    
    await api.post('/manager/attendances/check-out', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    ElMessage.success('Check-out thành công!');
    checkoutPhoto.value = null;
    fetchHistory();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Lỗi check-out');
  } finally {
    loadingCheckout.value = false;
  }
};

const fetchHistory = async () => {
  loadingHistory.value = true;
  try {
    const res = await api.get('/manager/attendances', { params: { page: 0, size: 10 } });
    history.value = res.data?.data?.content || [];
  } catch (e) { console.error(e); }
  finally { loadingHistory.value = false; }
};

const formatDate = (d) => {
  if (!d) return '';
  return new Date(d).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

const getStatusClass = (s) => {
  if (!s) return 'meta-tag-info';
  const lower = s.toLowerCase();
  if (lower === 'present' || lower === 'approved') return 'meta-tag-success';
  if (lower === 'rejected') return 'meta-tag-error';
  if (lower === 'abnormal' || lower === 'late') return 'meta-tag-warning';
  return 'meta-tag-info';
};

const getStatusText = (s) => {
  if (!s) return '---';
  const map = { present: 'Hợp lệ', late: 'Đi muộn', abnormal: 'Bất thường', approved: 'Đã duyệt', rejected: 'Từ chối' };
  return map[s.toLowerCase()] || s;
};

onMounted(() => { getLocation(); fetchHistory(); });
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }
:deep(.meta-card) { border-radius: 20px !important; border-left: none; border-right: none; border-bottom: none; background-color: #FFFFFF !important; padding: 24px !important; box-shadow: 0 12px 28px rgba(0,0,0,0.05) !important; }
:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 16px; padding: 12px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; }
:deep(.meta-btn-danger) { border-radius: 100px !important; background-color: #E41E3F !important; border-color: #E41E3F !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 16px; padding: 12px 24px !important; height: auto; transition: all 0.2s ease; }
.meta-native-input { width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid #CED0D4; font-family: 'Montserrat', sans-serif; font-size: 14px; color: #1C2B33; }
:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 20px 0 !important; }
:deep(.meta-tag-success), :deep(.meta-tag-warning), :deep(.meta-tag-error), :deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; border: none; padding: 6px 16px; height: auto; }
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-warning) { background-color: rgba(255, 226, 0, 0.15); color: #9C6C00; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }
:deep(.meta-tag-info) { background-color: rgba(0, 100, 224, 0.15); color: #0064E0; }
</style>
