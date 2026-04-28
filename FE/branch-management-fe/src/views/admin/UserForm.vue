<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[800px] mx-auto">
      <div v-if="loading" class="flex flex-col items-center justify-center h-[60vh]">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-[#0064E0]"></div>
        <p class="mt-4 text-[#5D6C7B] text-[16px]">Đang tải dữ liệu...</p>
      </div>
      
      <div v-else>
        <div class="mb-10 text-center">
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">
            {{ isEdit ? 'Cập nhật nhân sự' : 'Thêm nhân sự mới' }}
          </h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Quản lý thông tin và phân quyền hệ thống</p>
        </div>

        <el-card shadow="never" class="meta-card-form shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
          <form @submit.prevent="saveUser" class="flex flex-col gap-6">
            
            <div class="flex flex-col md:flex-row gap-6">
              <div class="flex-1 flex flex-col gap-2">
                <label class="text-[14px] font-bold text-[#1C2B33]">Họ và tên</label>
                <input v-model="form.fullName" required class="meta-native-input" placeholder="Nhập họ và tên đầy đủ" />
              </div>
              <div class="flex-1 flex flex-col gap-2">
                <label class="text-[14px] font-bold text-[#1C2B33]">Username</label>
                <input v-model="form.username" :disabled="isEdit" required class="meta-native-input disabled:bg-[#F1F4F7] disabled:text-[#8595A4]" placeholder="Tên đăng nhập hệ thống" />
              </div>
            </div>

            <div v-if="!isEdit" class="flex flex-col gap-2">
              <label class="text-[14px] font-bold text-[#1C2B33]">Mật khẩu</label>
              <input type="password" v-model="form.password" required class="meta-native-input" placeholder="Tạo mật khẩu an toàn" />
            </div>

            <div class="flex flex-col md:flex-row gap-6">
              <div class="flex-1 flex flex-col gap-2">
                <label class="text-[14px] font-bold text-[#1C2B33]">Vai trò</label>
                <el-select v-model="form.role" placeholder="Chọn vai trò" class="meta-input w-full">
                  <el-option label="ADMIN" value="ADMIN" />
                  <el-option label="MANAGER" value="MANAGER" />
                  <el-option label="ACCOUNTANT" value="ACCOUNTANT" /> 
                  <el-option label="EMPLOYEE" value="EMPLOYEE" />
                </el-select>
              </div>
            <div class="flex flex-col md:flex-row gap-6">
              <div class="flex-1 flex flex-col gap-2">
                <label class="text-[14px] font-bold text-[#1C2B33]">Chi nhánh</label>
                <el-select v-model="form.branchId" placeholder="-- Chọn chi nhánh --" required class="meta-input w-full">
                  <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
                </el-select>
              </div>
              <div class="flex-1 flex flex-col gap-2">
                <label class="text-[14px] font-bold text-[#1C2B33]">Lương theo giờ (VNĐ/h)</label>
                <input type="number" v-model="form.hourlyRate" class="meta-native-input" placeholder="Ví dụ: 25000" />
              </div>
            </div>

            <div class="flex gap-4 mt-8 pt-6 border-t border-[#DEE3E9]">
              <button type="button" class="meta-btn-secondary flex-1" @click="$router.back()">Hủy bỏ</button>
              <button type="submit" class="meta-btn-primary flex-[2]">{{ isEdit ? 'Lưu thay đổi' : 'Tạo tài khoản' }}</button>
            </div>
          </form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { userApi } from '@/api/user'; 
import { branchApi } from '@/api/branch';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const isEdit = ref(false);
const branches = ref([]);
const form = ref({ username: '', password: '', fullName: '', role: 'EMPLOYEE', branchId: null, hourlyRate: 0 });

onMounted(async () => {
  loading.value = true;
  try {
    const resB = await branchApi.getAll();
    branches.value = resB.data?.data || resB.data || [];
    
    if (route.params.id) {
      isEdit.value = true;
      const resU = await userApi.getById(route.params.id);
      const u = resU.data?.data || resU.data; 
      
      if (u) {
        form.value = { 
          username: u.username,
          fullName: u.fullName || u.full_name,
          role: u.role,
          branchId: u.branch?.branchId || u.branchId || null, 
          hourlyRate: u.hourlyRate || 0,
          password: '' 
        };
      }
    }
  } catch (error) {
    ElMessage.error("Lỗi khi tải dữ liệu");
    console.error(error);
  } finally { 
    loading.value = false; 
  }
});

const saveUser = async () => {
  try {
    const bId = form.value.branchId ? Number(form.value.branchId) : null;

    const payload = {
      username: form.value.username,
      fullName: form.value.fullName,
      role: form.value.role,
      branch: bId ? { branchId: bId } : null,
      hourlyRate: form.value.hourlyRate || 0
    };

    if (isEdit.value) {
      await userApi.update(route.params.id, payload);
      ElMessage.success("Cập nhật thành công!");
    } else {
      payload.password = form.value.password; 
      await userApi.create(payload);
      ElMessage.success("Thêm mới thành công!");
    }
    router.push('/admin/users');
  } catch (e) {
    ElMessage.error("Lỗi: " + (e.response?.data?.message || "Không thể lưu dữ liệu"));
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

:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper) {
  border-radius: 8px !important;
  border: 1px solid #CED0D4 !important;
  box-shadow: none !important;
  padding: 8px 16px;
  height: 48px;
}
:deep(.meta-input .el-input__wrapper.is-focus), :deep(.meta-input .el-select__wrapper.is-focus) {
  border-color: #0064E0 !important;
  box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important;
}

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
.meta-btn-secondary:hover {
  background-color: rgba(70, 90, 105, 0.1);
}
</style>