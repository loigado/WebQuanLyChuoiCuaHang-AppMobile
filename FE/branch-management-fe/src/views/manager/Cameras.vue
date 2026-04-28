<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Hệ thống Giám sát Camera</h2>
          <div class="flex items-center gap-3 mt-2">
            <p class="text-[18px] text-[#5D6C7B] font-normal">Quản lý trực tiếp không gian cửa hàng</p>
            <el-tag v-if="!isAdmin" class="meta-tag-info border-0">Chi nhánh: {{ auth.branchId }}</el-tag>
          </div>
        </div>
        <el-button v-if="isAdmin" class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="openAddModal">
          <span class="mr-2 text-[18px]">+</span> Cấu hình Camera mới
        </el-button>
      </div>

      <div v-loading="isLoading" class="min-h-[400px]">
        <div v-if="!isLoading && cameras.length === 0" class="py-20 text-center text-[#5D6C7B] bg-white rounded-[24px] border border-[#DEE3E9]">
          <span class="text-[48px] block mb-4">📹</span>
          <span class="text-[18px]">Không có camera nào được tìm thấy trên hệ thống.</span>
        </div>

        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8" v-for="cam in cameras" :key="cam.cameraId" class="mb-6">
            <el-card shadow="never" :body-style="{ padding: '0px' }" class="meta-camera-card">
              <div class="meta-video-wrapper">
                <video :id="'video-' + cam.cameraId" controls autoplay muted class="meta-video-player"></video>
                <div class="meta-live-badge">
                  <div class="meta-pulse-dot"></div> LIVE
                </div>
              </div>
              
              <div class="p-5 bg-white">
                <div class="text-[18px] font-bold text-[#1C2B33] mb-1">{{ cam.cameraName }}</div>
                <div class="text-[14px] text-[#5D6C7B] flex items-center gap-1">
                  <span>📍</span> {{ cam.branchName || 'Toàn hệ thống' }}
                </div>
                
                <div class="mt-4 pt-4 border-t border-[#F1F4F7]" v-if="isAdmin">
                  <el-button class="meta-btn-outline-danger w-full" @click="handleDelete(cam.cameraId)">
                    Gỡ bỏ camera
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <el-dialog v-model="showModal" title="Cấu hình luồng Camera mới" width="450px" class="meta-dialog" destroy-on-close>
        <div class="flex flex-col gap-5 pt-2">
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Định danh Camera</label>
            <input v-model="form.cameraName" class="meta-native-input" placeholder="VD: cam_cua_chinh_q1" />
          </div>
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Thuộc chi nhánh</label>
            <el-select v-model="form.branchId" placeholder="-- Chọn vị trí lắp đặt --" class="meta-input w-full">
              <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
            </el-select>
          </div>
          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">RTSP URL <span class="font-normal text-[#5D6C7B]">(Nguồn luồng)</span></label>
            <input v-model="form.streamUrl" class="meta-native-input" placeholder="rtsp://admin:password@ip:554/stream" />
          </div>
        </div>
        <template #footer>
          <div class="flex gap-3 justify-end pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary flex-1" @click="showModal = false">Hủy bỏ</el-button>
            <el-button class="meta-btn-primary flex-1" @click="saveCamera" :loading="isSaving">Lưu cấu hình</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { cameraApi } from '@/api/camera';
import { branchApi } from '@/api/branch';
import { ElMessage, ElMessageBox } from 'element-plus';
import Hls from 'hls.js';

const auth = useAuthStore();
const cameras = ref([]);
const branches = ref([]);
const showModal = ref(false);
const isLoading = ref(false);
const isSaving = ref(false);

const isAdmin = computed(() => { return auth.role && (auth.role.includes('ADMIN')); });
const form = ref({ cameraName: '', branchId: null, streamUrl: '' });

const initVideo = (cam) => {
  const video = document.getElementById('video-' + cam.cameraId);
  if (!video) return;
  const hlsUrl = cam.hlsUrl || `http://localhost:8888/${cam.cameraName}/index.m3u8`; 

  if (Hls.isSupported()) {
    const hls = new Hls();
    hls.loadSource(hlsUrl);
    hls.attachMedia(video);
    hls.on(Hls.Events.ERROR, (event, data) => {
      if (data.fatal) console.warn(`Camera lỗi.`);
    });
  } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
    video.src = hlsUrl;
  }
};

const loadCameras = async () => {
  isLoading.value = true;
  try {
    let res;
    if (isAdmin.value) { res = await cameraApi.adminGetAll(); } 
    else {
      let currentBranchId = auth.branchId || sessionStorage.getItem('branchId');
      if (!currentBranchId || currentBranchId === 'null') return;
      res = await cameraApi.managerGetByBranch(currentBranchId);
    }
    cameras.value = res.data?.data || [];
    await nextTick();
    if (Array.isArray(cameras.value)) cameras.value.forEach(cam => initVideo(cam));
  } catch (error) { console.error("Lỗi:", error); } 
  finally { isLoading.value = false; }
};

const openAddModal = async () => {
  try {
    const res = await branchApi.getAll();
    branches.value = res.data?.data || [];
    form.value = { cameraName: '', branchId: null, streamUrl: '' }; 
    showModal.value = true;
  } catch (e) { ElMessage.error("Lỗi tải chi nhánh"); }
};

const saveCamera = async () => {
  if (!form.value.cameraName || !form.value.branchId || !form.value.streamUrl) return ElMessage.warning("Điền đủ thông tin");
  isSaving.value = true;
  try {
    await cameraApi.create(form.value);
    ElMessage.success("Cấu hình thành công");
    showModal.value = false;
    loadCameras();
  } catch (error) { ElMessage.error("Lỗi server"); } 
  finally { isSaving.value = false; }
};

const handleDelete = (id) => {
  ElMessageBox.confirm('Chắc chắn gỡ bỏ?', 'Cảnh báo', { confirmButtonText: 'Xóa', cancelButtonText: 'Hủy', type: 'warning' }).then(async () => {
    await cameraApi.delete(id);
    ElMessage.success("Đã gỡ bỏ");
    loadCameras();
  }).catch(() => {});
};

onMounted(loadCameras);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-camera-card) { 
  border-radius: 20px !important; 
  border: 1px solid #DEE3E9 !important; 
  overflow: hidden; 
  transition: transform 0.3s ease, box-shadow 0.3s ease; 
  background: #fff;
}
:deep(.meta-camera-card:hover) { transform: translateY(-4px); box-shadow: 0 20px 40px rgba(0,0,0,0.1) !important; }

/* FIXED CSS CHO VIDEO TỶ LỆ 16:9 */
.meta-video-wrapper {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* Tỷ lệ 16:9 */
  background-color: #1C1E21;
}

.meta-video-player {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.meta-live-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background-color: #E41E3F;
  color: white;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.meta-pulse-dot {
  width: 6px;
  height: 6px;
  background-color: white;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.95); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.5; }
  100% { transform: scale(0.95); opacity: 1; }
}

:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-secondary) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto;}
:deep(.meta-btn-outline-danger) { background-color: transparent !important; color: #E41E3F !important; border: 1px solid rgba(228, 30, 63, 0.2) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px; padding: 10px 16px !important; transition: all 0.2s ease; }
:deep(.meta-btn-outline-danger:hover) { background-color: rgba(228, 30, 63, 0.05) !important; }

.meta-native-input { width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid #CED0D4; font-family: 'Montserrat', sans-serif; font-size: 14px; color: #1C2B33;}
.meta-native-input:focus { outline: none; border-color: #0064E0; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2); }
:deep(.meta-input .el-input__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; padding: 8px 12px; }
:deep(.meta-input .el-input__wrapper.is-focus) { border-color: #0064E0 !important; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; background-color: rgba(0, 100, 224, 0.1); color: #0064E0; padding: 0 12px; height: 26px; line-height: 26px; }
:deep(.meta-dialog) { border-radius: 20px !important; font-family: 'Montserrat', sans-serif; }
</style>