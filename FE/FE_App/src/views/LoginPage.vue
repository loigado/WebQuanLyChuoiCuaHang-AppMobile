<template>
  <ion-page>
    <ion-content class="ion-padding ion-text-center">
      <div class="login-container">
        <img src="https://ionicframework.com/docs/img/demos/avatar.svg" class="logo" />
        <h1 class="title">Đăng Nhập</h1>
        <p class="subtitle">Hệ thống quản lý chi nhánh</p>

        <ion-list lines="none">
          <ion-item class="custom-input">
            <ion-icon slot="start" :icon="personOutline"></ion-icon>
            <ion-input label="Tài khoản" label-placement="floating" v-model="form.username" placeholder="Nhập username"></ion-input>
          </ion-item>
          <ion-item class="custom-input ion-margin-top">
            <ion-icon slot="start" :icon="lockClosedOutline"></ion-icon>
            <ion-input label="Mật khẩu" label-placement="floating" type="password" v-model="form.password" placeholder="Nhập mật khẩu"></ion-input>
          </ion-item>
        </ion-list>

        <ion-button expand="block" shape="round" class="ion-margin-top login-btn" @click="handleLogin">
          ĐĂNG NHẬP
        </ion-button>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { reactive } from 'vue'; 
import { useRouter } from 'vue-router';
import { IonPage, IonContent, IonList, IonItem, IonInput, IonIcon, IonButton, toastController, loadingController } from '@ionic/vue';
import { personOutline, lockClosedOutline } from 'ionicons/icons';
import apiClient from '@/services/api';

const router = useRouter();
const form = reactive({ username: '', password: '' });

const handleLogin = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    showToast('Vui lòng nhập đầy đủ thông tin', 'warning');
    return;
  }

  const loading = await loadingController.create({ message: 'Đang xác thực...' });
  await loading.present();

  try {
    // 1. Gọi đúng Endpoint của AuthController.java (/api/auth/login)
    const response = await apiClient.post('/auth/login', form);
    
    // 2. Lấy object JwtResponse nằm bên trong ApiResponse.data
    const result = response.data.data;
    
    if (!result || !result.token) {
      showToast('Lỗi: Không nhận được Token từ Server', 'danger');
      await loading.dismiss();
      return;
    }

    // 3. Lưu chính xác các biến khớp với JwtResponse của Backend
    localStorage.setItem('token', result.token); 
    localStorage.setItem('user_id', result.id);             // Backend trả về 'id'
    localStorage.setItem('full_name', result.fullName);     // Backend trả về 'fullname'
    localStorage.setItem('role', result.role);              // Backend trả về 'role'
    localStorage.setItem('branch_id', result.branchId);     // Backend trả về 'branchId' (rất cần cho check-in)

    await loading.dismiss();
    showToast('Đăng nhập thành công!', 'success');
    router.push('/home');
    
  } catch (error: any) {
    await loading.dismiss();
    console.error("Lỗi đăng nhập:", error);
    let message = error.response?.data?.message || 'Sai tài khoản hoặc mật khẩu';
    showToast(message, 'danger');
  }
};

const showToast = async (msg: string, color: string) => {
  const toast = await toastController.create({ message: msg, duration: 2000, color: color, position: 'top' });
  await toast.present();
};
</script>

<style scoped>
.login-container { margin-top: 15%; padding: 0 10px; }
.logo { width: 100px; margin-bottom: 20px; border-radius: 50%; }
.title { font-weight: bold; font-size: 26px; color: #3880ff; margin-bottom: 5px; }
.subtitle { color: #666; margin-bottom: 30px; }
.custom-input { --background: #f4f5f8; --border-radius: 12px; --padding-start: 15px; margin-bottom: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.login-btn { height: 55px; font-weight: bold; font-size: 16px; }
</style>