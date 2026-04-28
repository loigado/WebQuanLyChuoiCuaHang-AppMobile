<template>
  <div class="min-h-screen bg-[#F1F4F7] flex items-center justify-center font-meta p-4">
    <div class="bg-[#FFFFFF] rounded-[24px] shadow-[0_12px_28px_0_rgba(0,0,0,0.1)] p-10 w-full max-w-[480px]">
      
      <div class="text-center mb-10">
        <div class="w-16 h-16 bg-[#E8F3FF] text-[#0064E0] rounded-[20px] flex items-center justify-center mx-auto mb-6 text-[32px]">
          🔒
        </div>
        <h2 class="text-[28px] font-medium text-[#1C2B33] tracking-tight leading-[1.2]">Đăng Nhập Hệ Thống</h2>
        <p class="text-[16px] text-[#5D6C7B] mt-2">Vui lòng nhập thông tin để tiếp tục</p>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginRef" label-position="top" @keyup.enter="handleLogin">
        <el-form-item prop="username" class="meta-form-item">
          <label class="text-[14px] font-bold text-[#1C2B33] mb-2 block">Tên đăng nhập</label>
          <el-input 
            v-model="loginForm.username" 
            placeholder="Nhập username của bạn" 
            :prefix-icon="User"
            class="meta-input w-full"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password" class="meta-form-item mt-6">
          <label class="text-[14px] font-bold text-[#1C2B33] mb-2 block">Mật khẩu</label>
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="Nhập mật khẩu" 
            show-password 
            :prefix-icon="Lock"
            class="meta-input w-full"
            size="large"
          />
        </el-form-item>

        <div class="flex items-center justify-between mb-8 mt-4">
          <el-checkbox v-model="rememberMe" class="meta-checkbox">
            <span class="text-[#5D6C7B] font-medium">Ghi nhớ đăng nhập</span>
          </el-checkbox>
        </div>

        <el-button class="meta-btn-primary w-full shadow-[0_4px_12px_0_rgba(0,100,224,0.2)]" @click="handleLogin" :loading="loading">
          Đăng nhập
        </el-button>
      </el-form>
      
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { User, Lock } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();

const loginRef = ref(null);
const loading = ref(false);
const rememberMe = ref(false);

const loginForm = reactive({
  username: '',
  password: ''
});

const rules = {
  username: [{ required: true, message: 'Vui lòng nhập tên đăng nhập', trigger: 'blur' }],
  password: [{ required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' }]
};

const handleLogin = async () => {
  if (!loginRef.value) return;
  
  await loginRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true;
        
        // ✅ ĐÃ SỬA: Gọi authStore.login() thống nhất 1 nguồn dữ liệu (sessionStorage)
        const userData = await authStore.login(loginForm.username, loginForm.password);

        ElMessage.success('Đăng nhập thành công!');
        
        const role = userData.role;
        if (role.includes('ADMIN') || role.includes('MANAGER')) {
          router.push('/'); 
        } 
        else if (role.includes('ACCOUNTANT')) {
          router.push('/accountant/inventory-data'); 
        }
        else {
          router.push('/employee/attendance');
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Tài khoản hoặc mật khẩu không đúng');
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');

.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-form-item .el-form-item__content) { line-height: normal; }

:deep(.meta-input .el-input__wrapper) {
  border-radius: 8px !important;
  border: 1px solid #CED0D4 !important;
  box-shadow: none !important;
  padding: 8px 16px;
  height: 48px;
  background-color: #FFFFFF;
}
:deep(.meta-input .el-input__wrapper.is-focus) {
  border-color: #0064E0 !important;
  box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important;
}
:deep(.meta-input .el-input__inner) {
  font-family: 'Montserrat', sans-serif;
  color: #1C2B33;
  font-size: 16px;
}

:deep(.meta-btn-primary) {
  border-radius: 100px !important;
  background-color: #0064E0 !important;
  border-color: #0064E0 !important;
  color: #FFFFFF !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 16px;
  height: 48px !important;
  transition: all 0.2s ease;
}
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }

:deep(.meta-checkbox .el-checkbox__inner) {
  border-radius: 4px;
  border-color: #CED0D4;
}
:deep(.meta-checkbox.is-checked .el-checkbox__inner) {
  background-color: #0064E0;
  border-color: #0064E0;
}
:deep(.meta-checkbox.is-checked .el-checkbox__label) { color: #1C2B33; }
</style>