<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[800px] mx-auto">
      <div class="mb-10 text-center">
        <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">
          {{ isEdit ? 'Cập nhật chi nhánh' : 'Thêm chi nhánh mới' }}
        </h2>
        <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Thiết lập thông tin điểm bán và tọa độ GPS</p>
      </div>

      <el-card shadow="never" class="meta-card-form shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <form @submit.prevent="saveBranch" class="flex flex-col gap-6">
          
          <div class="flex flex-col md:flex-row gap-6">
            <div class="flex-1 flex flex-col gap-2">
              <label class="text-[14px] font-bold text-[#1C2B33]">Mã chi nhánh</label>
              <input v-model="form.branchCode" :disabled="isEdit" class="meta-native-input disabled:bg-[#F1F4F7] disabled:text-[#8595A4]" placeholder="Ví dụ: HCM01" required />
            </div>
            <div class="flex-[2] flex flex-col gap-2">
              <label class="text-[14px] font-bold text-[#1C2B33]">Tên chi nhánh</label>
              <input v-model="form.branchName" class="meta-native-input" placeholder="Ví dụ: Chi nhánh Quận 1" required />
            </div>
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-[14px] font-bold text-[#1C2B33]">Địa chỉ chi tiết</label>
            <input v-model="form.address" class="meta-native-input" placeholder="Số nhà, Tên đường, Quận, Thành phố..." required />
          </div>

          <div class="p-6 bg-[#F7F8FA] rounded-[16px] border border-[#DEE3E9]">
            <h4 class="text-[16px] font-bold text-[#1C2B33] mb-4 flex items-center gap-2">
              <span>📍</span> Tọa độ bản đồ (Phục vụ chấm công)
            </h4>
            <div class="flex flex-col md:flex-row gap-6">
              <div class="flex-1 flex flex-col gap-2">
                <label class="text-[14px] font-bold text-[#5D6C7B]">Vĩ độ (Latitude)</label>
                <input type="number" step="any" v-model="form.latitude" class="meta-native-input bg-white" required />
              </div>
              <div class="flex-1 flex flex-col gap-2">
                <label class="text-[14px] font-bold text-[#5D6C7B]">Kinh độ (Longitude)</label>
                <input type="number" step="any" v-model="form.longitude" class="meta-native-input bg-white" required />
              </div>
            </div>
          </div>

          <div class="flex gap-4 mt-8 pt-6 border-t border-[#DEE3E9]">
            <button type="button" @click="$router.back()" class="meta-btn-secondary flex-1">Hủy bỏ</button>
            <button type="submit" class="meta-btn-primary flex-[2]">{{ isEdit ? 'Lưu thay đổi' : 'Tạo chi nhánh' }}</button>
          </div>
        </form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { branchApi } from '@/api/branch';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const isEdit = ref(false);

const form = ref({
  branchCode: '',
  branchName: '',
  address: '',
  latitude: 0,
  longitude: 0
});

onMounted(async () => {
  if (route.params.id) {
    isEdit.value = true;
    try {
      const res = await branchApi.getById(route.params.id);
      form.value = res.data.data;
    } catch (error) {
      ElMessage.error("Không thể tải thông tin chi nhánh");
    }
  }
});

const saveBranch = async () => {
  try {
    if (isEdit.value) {
      await branchApi.update(route.params.id, form.value);
      ElMessage.success("Cập nhật thành công!");
    } else {
      await branchApi.create(form.value);
      ElMessage.success("Thêm chi nhánh thành công!");
    }
    router.push('/admin/branches');
  } catch (e) {
    ElMessage.error("Lỗi: " + (e.response?.data?.message || "Không thể lưu"));
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');

.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card-form) {
  border-radius: 20px !important;
  border: none !important;
  background-color: #FFFFFF !important;
  padding: 32px;
}

.meta-native-input {
  width: 100%;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #CED0D4;
  font-family: 'Montserrat', sans-serif;
  font-size: 16px;
  color: #1C2B33;
  transition: all 0.2s ease;
}
.meta-native-input:focus {
  outline: none;
  border-color: #0064E0;
  box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2);
}
.meta-native-input::placeholder { color: #65676B; }

.meta-btn-primary {
  background-color: #0064E0;
  color: #FFFFFF;
  border-radius: 100px;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 16px;
  padding: 12px 24px;
  transition: all 0.2s ease;
}
.meta-btn-primary:hover { background-color: #0143B5; transform: scale(1.02); }

.meta-btn-secondary {
  background-color: transparent;
  color: #1C2B33;
  border: 2px solid rgba(10, 19, 23, 0.12);
  border-radius: 100px;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 16px;
  padding: 12px 24px;
  transition: all 0.2s ease;
}
.meta-btn-secondary:hover { background-color: rgba(70, 90, 105, 0.1); }
</style>