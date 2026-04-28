<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Chấm Công Mobile</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="handleLogout">
            <ion-icon slot="icon-only" :icon="logOutOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <!-- User Profile Header -->
      <div class="ion-text-center ion-margin-bottom">
        <h2 class="welcome-text">Chào, {{ fullName }}</h2>
        <p class="role-debug" v-if="userRole">Chức vụ: {{ userRole }}</p>
        <p class="date-text">Hôm nay: {{ todayFormatted }}</p>
      </div>

      <!-- Main Action Card (Check-in/Check-out) -->
      <ion-card class="checkin-card" @click="openCameraModal">
        <ion-card-content class="ion-text-center">
          <ion-icon :icon="locationOutline" :class="isCheckedIn ? 'icon-out' : 'icon-in'"></ion-icon>
          <h2 :class="isCheckedIn ? 'text-out' : 'text-in'">
            {{ isCheckedIn ? 'BẤM ĐỂ CHECK-OUT' : 'BẤM ĐỂ CHECK-IN' }}
          </h2>
          <p>{{ isCheckedIn ? 'Kết thúc ca làm việc của bạn' : 'Bắt đầu ca làm việc của bạn' }}</p>
        </ion-card-content>
      </ion-card>

      <!-- NEW: Estimated Payroll Card -->
      <ion-card class="payroll-card" v-if="payrollData">
        <ion-card-content>
          <div class="payroll-header">
            <span class="payroll-title">Lương dự tính (Tháng {{ payrollData.month }})</span>
            <ion-icon :icon="checkmarkDoneCircleOutline" color="success"></ion-icon>
          </div>
          <div class="salary-amount">
            {{ formatCurrency(payrollData.estimatedSalary) }}
          </div>
          <div class="payroll-footer">
            <span>{{ payrollData.totalShifts }} ca đã làm</span>
            <span>{{ payrollData.totalHours.toFixed(1) }} giờ</span>
          </div>
        </ion-card-content>
      </ion-card>

      <!-- NEW: Internal Announcements Section -->
      <div class="announcement-section" v-if="announcements.length > 0">
        <div class="section-header">
          <span class="section-title">Tin tức nội bộ</span>
          <ion-badge color="primary">{{ announcements.length }} mới</ion-badge>
        </div>
        <div class="announcement-list">
          <div v-for="item in announcements" :key="item.id" class="announcement-item" @click="showAnnouncementDetail(item)">
            <h3 class="item-title">{{ item.title }}</h3>
            <p class="item-date">{{ formatDate(item.createdAt) }}</p>
          </div>
        </div>
      </div>
      <ion-list lines="none" class="ion-margin-top menu-list">
        <ion-item button @click="router.push('/history')" class="menu-item">
          <ion-icon slot="start" :icon="timeOutline" color="primary"></ion-icon>
          <ion-label>Lịch sử chấm công</ion-label>
        </ion-item>
        
        <ion-item button @click="router.push('/schedule')" class="menu-item">
          <ion-icon slot="start" :icon="calendarOutline" color="success"></ion-icon>
          <ion-label>Lịch làm việc</ion-label>
        </ion-item>

        <ion-item button @click="router.push('/leave')" class="menu-item">
          <ion-icon slot="start" :icon="documentTextOutline" color="warning"></ion-icon>
          <ion-label>Xin phép nghỉ</ion-label>
        </ion-item>

        <!-- Manager/Admin Only -->
        <ion-item v-if="isManagerOrAdmin" button @click="router.push('/stock-approval')" class="menu-item">
          <ion-icon slot="start" :icon="checkmarkDoneCircleOutline" color="danger"></ion-icon>
          <ion-label>Duyệt phiếu kho</ion-label>
        </ion-item>
      </ion-list>

      <!-- Camera Modal -->
      <ion-modal :is-open="isCameraOpen" @didDismiss="closeCamera">
        <ion-header>
          <ion-toolbar color="dark">
            <ion-title>Chụp Ảnh Xác Nhận</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="closeCamera">Hủy</ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        
        <ion-content class="ion-padding ion-text-center">
          <div class="camera-container">
            <video ref="videoRef" autoplay playsinline class="video-feed" v-show="!photoBlob"></video>
            <canvas ref="canvasRef" style="display: none;"></canvas>
            <img :src="photoUrl" v-if="photoUrl" class="photo-preview" />
          </div>

          <p class="gps-hint">📍 Vị trí GPS sẽ được tự động gắn kèm</p>

          <ion-button expand="block" shape="round" color="primary" @click="capturePhoto" v-if="!photoBlob" class="action-btn">
            📸 CHỤP ẢNH
          </ion-button>

          <div v-else class="confirm-container">
            <ion-button expand="block" shape="round" color="success" @click="submitAttendance" class="action-btn">
              ✅ XÁC NHẬN {{ isCheckedIn ? 'CHECK-OUT' : 'CHECK-IN' }}
            </ion-button>
            <ion-button expand="block" fill="clear" color="danger" @click="retakePhoto">
              Chụp lại ảnh
            </ion-button>
          </div>
        </ion-content>
      </ion-modal>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, 
  IonIcon, IonContent, IonCard, IonCardContent, IonList, IonItem, IonLabel,
  IonModal, onIonViewWillEnter, alertController, IonBadge
} from '@ionic/vue';
import { 
  locationOutline, timeOutline, calendarOutline, logOutOutline, 
  documentTextOutline, checkmarkDoneCircleOutline 
} from 'ionicons/icons';
import { Geolocation } from '@capacitor/geolocation';
import apiClient from '@/services/api';
import { useUi } from '@/services/ui';

// --- State Management ---
const router = useRouter();
const { showToast, showLoading } = useUi();

const fullName = ref('');
const userRole = ref('');
const isCheckedIn = ref(false);
const isManagerOrAdmin = ref(false);
const todayFormatted = ref(new Date().toLocaleDateString('vi-VN', { weekday: 'long', day: 'numeric', month: 'long' }));
const payrollData = ref<any>(null);
const announcements = ref<any[]>([]);

const isCameraOpen = ref(false);
const videoRef = ref<HTMLVideoElement | null>(null);
const canvasRef = ref<HTMLCanvasElement | null>(null);
const photoBlob = ref<Blob | null>(null);
const photoUrl = ref<string | null>(null);

let mediaStream: MediaStream | null = null;

// --- Lifecycle Hooks ---
onIonViewWillEnter(() => {
  isCheckedIn.value = false; // Reset trạng thái để không bị dính người cũ
  loadUserProfile();
  fetchCurrentAttendanceStatus();
  fetchPayroll();
  fetchAnnouncements();
});

// --- Methods ---
const fetchAnnouncements = async () => {
  try {
    const response = await apiClient.get('/mobile/announcements');
    announcements.value = response.data.data || [];
  } catch (error) {
    console.warn("Failed to fetch announcements:", error);
  }
};

const showAnnouncementDetail = async (item: any) => {
  const alert = await alertController.create({
    header: item.title,
    message: item.content,
    buttons: ['Đóng']
  });
  await alert.present();
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
};

const fetchPayroll = async () => {
  try {
    const userId = localStorage.getItem('user_id');
    const response = await apiClient.get('/mobile/payroll', { params: { userId } });
    payrollData.value = response.data.data;
  } catch (error) {
    console.warn("Failed to fetch payroll:", error);
  }
};

const formatCurrency = (value: number) => {
  if (!value) return '0 VNĐ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const loadUserProfile = () => {
  fullName.value = localStorage.getItem('full_name') || 'Nhân viên';
  const role = localStorage.getItem('role') || 'EMPLOYEE';
  userRole.value = role;
  
  const r = role.toUpperCase();
  isManagerOrAdmin.value = r.includes('MANAG') || r.includes('ADMIN') || r.includes('QUAN');
};

const fetchCurrentAttendanceStatus = async () => {
  try {
    const userId = localStorage.getItem('user_id');
    if (!userId) return;
    const response = await apiClient.get('/mobile/attendance-history', { params: { userId } });
    const history = response.data.data || [];
    if (history.length > 0) {
      const latestRecord = history[0]; 
      isCheckedIn.value = !!(latestRecord.checkInTime && !latestRecord.checkOutTime);
    }
  } catch (error) {
    console.warn("Failed to fetch status:", error);
  }
};

const handleLogout = () => {
  localStorage.clear();
  router.push('/login');
};

const openCameraModal = async () => {
  isCameraOpen.value = true;
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' } });
    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream;
    }
  } catch (error) {
    showToast('Không thể truy cập Camera!', 'danger');
    isCameraOpen.value = false;
  }
};

const closeCamera = () => {
  isCameraOpen.value = false;
  stopMediaStream();
  clearPhotoData();
};

const stopMediaStream = () => {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop());
    mediaStream = null;
  }
};

const capturePhoto = () => {
  if (!canvasRef.value || !videoRef.value) return;
  const context = canvasRef.value.getContext('2d');
  const width = videoRef.value.videoWidth;
  const height = videoRef.value.videoHeight;
  canvasRef.value.width = width;
  canvasRef.value.height = height;
  context?.drawImage(videoRef.value, 0, 0, width, height);
  canvasRef.value.toBlob((blob) => {
    if (blob) {
      photoBlob.value = blob;
      photoUrl.value = URL.createObjectURL(blob);
    }
  }, 'image/jpeg', 0.6);
};

const retakePhoto = () => clearPhotoData();
const clearPhotoData = () => {
  photoBlob.value = null;
  if (photoUrl.value) {
    URL.revokeObjectURL(photoUrl.value);
    photoUrl.value = null;
  }
};

const submitAttendance = async () => {
  const loadingElement = await showLoading('Đang gửi dữ liệu...');
  try {
    const position = await Geolocation.getCurrentPosition({ enableHighAccuracy: true, timeout: 15000 });
    const userId = localStorage.getItem('user_id');
    const branchId = localStorage.getItem('branch_id');
    if (!userId || !branchId || !photoBlob.value) throw new Error('Thiếu thông tin!');
    const formData = new FormData();
    formData.append('userId', userId); 
    formData.append('branchId', branchId); 
    formData.append('lat', position.coords.latitude.toString()); 
    formData.append('lng', position.coords.longitude.toString()); 
    formData.append('photo', photoBlob.value, 'attendance.jpg');
    const endpoint = isCheckedIn.value ? '/check-out' : '/check-in';
    const response = await apiClient.post(`/mobile${endpoint}`, formData);

    if (response.data.success) {
      showToast(response.data.message || 'Thao tác thành công!', 'success');
      isCheckedIn.value = !isCheckedIn.value;
      closeCamera();
    } else {
      // Ép hiện đúng câu thông báo bạn yêu cầu
      showToast('Khoảng cách quá xa cửa hàng vui lòng lại gần cửa hàng', 'danger');
    }
  } catch (error: any) {
    // Nếu có lỗi hệ thống hoặc lỗi 400 từ server
    showToast('Khoảng cách quá xa cửa hàng vui lòng lại gần cửa hàng', 'danger');
  } finally {
    loadingElement.dismiss();
  }
};
</script>

<style scoped>
.welcome-text { font-weight: bold; margin-bottom: 2px; }
.role-debug { font-size: 11px; color: #3880ff; font-weight: bold; margin-bottom: 4px; text-transform: uppercase; }
.date-text { color: var(--ion-color-medium); margin-top: 0; }
.checkin-card { border-radius: 20px; box-shadow: 0 8px 24px rgba(0,0,0,0.08); margin-top: 20px; border: 1px solid rgba(var(--ion-color-primary-rgb), 0.2); }
.menu-item { border: 1px solid #f2f2f2; border-radius: 15px; margin-bottom: 12px; --background: #fff; }
.icon-in { font-size: 64px; color: var(--ion-color-primary); }
.icon-out { font-size: 64px; color: var(--ion-color-danger); }
.text-in { color: var(--ion-color-primary); font-weight: 800; }
.text-out { color: var(--ion-color-danger); font-weight: 800; }
.camera-container { width: 100%; aspect-ratio: 4/3; background-color: #1a1a1a; border-radius: 20px; overflow: hidden; margin-bottom: 15px; }
.video-feed, .photo-preview { width: 100%; height: 100%; object-fit: cover; }
.gps-hint { font-size: 13px; color: var(--ion-color-medium); font-style: italic; }
.action-btn { height: 52px; margin-top: 20px; font-weight: bold; font-size: 16px; }
.payroll-card {
  margin: 15px;
  border-radius: 18px;
  background: linear-gradient(135deg, #3880ff 0%, #1d4ed8 100%);
  color: white;
  box-shadow: 0 10px 20px rgba(56, 128, 255, 0.2);
}
.payroll-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.payroll-title {
  font-size: 13px;
  opacity: 0.9;
  font-weight: 500;
}
.salary-amount {
  font-size: 32px;
  font-weight: 800;
  margin: 10px 0;
  letter-spacing: -0.5px;
}
.payroll-footer {
  display: flex;
  gap: 15px;
  font-size: 13px;
  opacity: 0.8;
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 10px;
}
.announcement-section {
  margin: 20px 15px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}
.announcement-list {
  display: flex;
  overflow-x: auto;
  gap: 15px;
  padding-bottom: 10px;
}
.announcement-item {
  min-width: 200px;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 12px;
  border-left: 4px solid #3880ff;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}
.item-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 5px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-date {
  font-size: 12px;
  color: #888;
  margin: 0;
}
</style>