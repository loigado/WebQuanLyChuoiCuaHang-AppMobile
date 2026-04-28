<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1000px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Cấu hình hệ thống</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Thiết lập các thông số hoạt động cốt lõi của ứng dụng</p>
        </div>
        <div class="flex gap-4">
          <el-button class="meta-btn-secondary" @click="handleResetDefault">
            Khôi phục mặc định
          </el-button>
          <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" :loading="saving" @click="handleSaveAll">
            Lưu tất cả thay đổi
          </el-button>
        </div>
      </div>

      <div class="flex flex-col gap-8">
        <el-card shadow="never" class="meta-card-form shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
          <h3 class="text-[24px] font-medium text-[#1C2B33] mb-6 border-b border-[#DEE3E9] pb-4 flex items-center gap-3">
            <span class="text-[28px]">📍</span> Cấu hình Chấm công & GPS
          </h3>
          
          <div class="flex flex-col md:flex-row gap-10">
            <div class="flex-1">
              <label class="text-[16px] font-bold text-[#1C2B33] block mb-2">Bán kính cho phép chấm công</label>
              <p class="text-[14px] text-[#5D6C7B] mb-4">Nhân viên phải đứng trong vùng bán kính này (tính bằng mét) để có thể Check-in/out thành công qua ứng dụng.</p>
              <el-input-number 
                v-model="settings.gps_radius" 
                :min="10" 
                :max="1000" 
                class="meta-input-number w-full"
              />
            </div>
            
            <div class="flex-1">
              <label class="text-[16px] font-bold text-[#1C2B33] block mb-2">Thời gian vào làm mặc định</label>
              <p class="text-[14px] text-[#5D6C7B] mb-4">Hệ thống sẽ lấy mốc thời gian này để tính toán xem nhân viên có đi làm trễ hay không.</p>
              <el-time-picker 
                v-model="settings.checkin_time" 
                format="HH:mm" 
                value-format="HH:mm" 
                placeholder="Chọn giờ"
                class="meta-input w-full"
              />
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="meta-card-form shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
          <h3 class="text-[24px] font-medium text-[#1C2B33] mb-6 border-b border-[#DEE3E9] pb-4 flex items-center gap-3">
            <span class="text-[28px]">📦</span> Cấu hình Kho hàng
          </h3>
          
          <div class="w-full md:w-1/2 pr-0 md:pr-5">
            <label class="text-[16px] font-bold text-[#1C2B33] block mb-2">Ngưỡng cảnh báo tồn kho thấp</label>
            <p class="text-[14px] text-[#5D6C7B] mb-4">Hệ thống sẽ tự động gửi cảnh báo nếu số lượng của bất kỳ sản phẩm nào rơi xuống dưới mức này.</p>
            <el-input-number 
              v-model="settings.inventory_threshold" 
              :min="1" 
              class="meta-input-number w-full"
            />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { configApi } from '@/api/systemConfig';
import { ElMessage, ElMessageBox } from 'element-plus';

const DEFAULT_SETTINGS = {
  gps_radius: 100,
  checkin_time: '08:00',
  inventory_threshold: 10
};

const saving = ref(false);
const settings = ref({ ...DEFAULT_SETTINGS });

const loadSettings = async () => {
  try {
    const res = await configApi.getAll();
    if (res.data && res.data.data) {
      const dbData = res.data.data;
      dbData.forEach(item => {
        if (settings.value.hasOwnProperty(item.configKey)) {
          if (item.configKey === 'gps_radius' || item.configKey === 'inventory_threshold') {
            settings.value[item.configKey] = Number(item.configValue);
          } else {
            settings.value[item.configKey] = item.configValue;
          }
        }
      });
    }
  } catch (error) {
    ElMessage.error("Không thể tải cấu hình từ máy chủ");
  }
};

const handleResetDefault = () => {
  ElMessageBox.confirm(
    'Bạn có chắc chắn muốn đưa các cấu hình về giá trị mặc định? (Cần nhấn Lưu để áp dụng)',
    'Xác nhận khôi phục',
    { confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy', type: 'warning' }
  ).then(() => {
    settings.value = { ...DEFAULT_SETTINGS };
    ElMessage.info("Đã đưa về mặc định, vui lòng nhấn Lưu để cập nhật Database");
  }).catch(() => {});
};

const handleSaveAll = async () => {
  saving.value = true;
  try {
    const formattedConfigs = {};
    Object.keys(settings.value).forEach(key => {
      formattedConfigs[key] = String(settings.value[key]);
    });
    await configApi.bulkUpdate(formattedConfigs); 
    ElMessage.success("Đã cập nhật cấu hình hệ thống thành công");
  } catch (error) {
    ElMessage.error("Lưu thất bại, vui lòng kiểm tra lại kết nối");
  } finally {
    saving.value = false;
  }
};

onMounted(loadSettings);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');

.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card-form) {
  border-radius: 20px !important;
  border: none !important;
  background-color: #FFFFFF !important;
  padding: 40px !important;
}

/* Buttons */
:deep(.meta-btn-primary) {
  border-radius: 100px !important;
  background-color: #0064E0 !important;
  border-color: #0064E0 !important;
  color: #FFFFFF !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 14px;
  padding: 10px 24px !important;
  height: auto;
  letter-spacing: -0.14px;
  transition: all 0.2s ease;
}
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }

:deep(.meta-btn-secondary) {
  background-color: transparent !important;
  color: #1C2B33 !important;
  border: 2px solid rgba(10, 19, 23, 0.12) !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 14px;
  padding: 10px 24px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-secondary:hover) {
  background-color: rgba(70, 90, 105, 0.1) !important;
}

/* Inputs */
:deep(.meta-input .el-input__wrapper), :deep(.meta-input-number .el-input__wrapper) {
  border-radius: 8px !important;
  border: 1px solid #CED0D4 !important;
  box-shadow: none !important;
  padding: 8px 12px;
  height: 48px;
}
:deep(.meta-input .el-input__wrapper.is-focus), :deep(.meta-input-number .el-input__wrapper.is-focus) {
  border-color: #0064E0 !important;
  box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important;
}

/* Fix Input Number Layout */
:deep(.meta-input-number .el-input-number__decrease), :deep(.meta-input-number .el-input-number__increase) {
  background-color: #F1F4F7;
  border: none;
  height: 46px;
}
</style>