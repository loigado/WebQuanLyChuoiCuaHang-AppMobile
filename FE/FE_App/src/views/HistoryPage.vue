<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="/home"></ion-back-button>
        </ion-buttons>
        <ion-title>Lịch Sử Chấm Công</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <ion-list v-if="attendanceHistory.length > 0">
        <ion-item v-for="(item, index) in attendanceHistory" :key="index" class="history-item">
          
          <div slot="start" class="photo-container-group">
            <div class="photo-box">
              <img :src="getImageUrl(item.checkInPhotoUrl)" @error="handleImageError" alt="Ảnh vào" />
              <span class="photo-label">VÀO</span>
            </div>
            <div class="photo-box">
              <img :src="getImageUrl(item.checkOutPhotoUrl)" @error="handleImageError" alt="Ảnh ra" />
              <span class="photo-label">RA</span>
            </div>
          </div>

          <ion-label class="ion-padding-start">
            <h2 class="history-date">
              Ngày: {{ formatDate(item.checkInTime) }}
            </h2>
            <p>📥 Vào: {{ formatTime(item.checkInTime) }}</p>
            <p>📤 Ra: {{ item.checkOutTime ? formatTime(item.checkOutTime) : 'Chưa Check-out' }}</p>
          </ion-label>
          
          <ion-badge slot="end" :color="item.totalHours > 0 ? 'success' : 'warning'">
            {{ item.totalHours || 0 }} giờ
          </ion-badge>
        </ion-item>
      </ion-list>

      <div v-else class="empty-state ion-text-center ion-padding">
        <ion-icon :icon="calendarOutline" class="empty-icon"></ion-icon>
        <p class="empty-text">Bạn chưa có dữ liệu chấm công nào.</p>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
/**
 * HistoryPage Component
 * Displays the attendance history for the logged-in employee, 
 * including check-in/check-out times and verification photos.
 */
import { ref } from 'vue';
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonList, 
  IonItem, IonLabel, IonBadge, IonButtons, IonBackButton, IonIcon,
  onIonViewWillEnter
} from '@ionic/vue';
import { calendarOutline } from 'ionicons/icons';
import apiClient, { getServerUrl } from '@/services/api';

// --- State Management ---
const attendanceHistory = ref<any[]>([]);
const DEFAULT_AVATAR = 'https://ionicframework.com/docs/img/demos/thumbnail.svg';

// --- Lifecycle Hooks ---
onIonViewWillEnter(async () => {
  await fetchHistory();
});

// --- Data Fetching ---
/**
 * Fetches attendance history from the backend for the current user.
 */
const fetchHistory = async () => {
  const userId = localStorage.getItem('user_id');
  if (!userId) return;

  try {
    const response = await apiClient.get('/mobile/attendance-history', { params: { userId } });
    attendanceHistory.value = response.data.data || [];
  } catch (error) {
    console.error("Failed to fetch attendance history:", error);
  }
};

// --- Helper Functions ---
/**
 * Resolves the full URL for the verification photo.
 */
const getImageUrl = (path: string) => {
  if (!path) return DEFAULT_AVATAR;
  const safePath = path.startsWith('/') ? path : `/${path}`;
  return `${getServerUrl()}${safePath}`;
};

/**
 * Handles image loading errors by showing a default placeholder.
 */
const handleImageError = (event: Event) => {
  (event.target as HTMLImageElement).src = DEFAULT_AVATAR;
};

/**
 * Formats a date string into a localized format.
 */
const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};

/**
 * Formats a date string into a localized time format (HH:mm).
 */
const formatTime = (dateStr: string) => {
  if (!dateStr) return '--:--';
  return new Date(dateStr).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
};
</script>

<style scoped>
.history-item {
  --padding-start: 10px;
  --padding-bottom: 12px;
  --padding-top: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.photo-container-group { display: flex; gap: 8px; }
.photo-box { display: flex; flex-direction: column; align-items: center; }
.photo-box img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.photo-label { font-size: 8px; font-weight: bold; color: #aaa; margin-top: 4px; }

.history-date { font-weight: bold; color: var(--ion-color-primary); margin-bottom: 4px; }

.empty-state { margin-top: 60px; }
.empty-icon { font-size: 80px; color: #ddd; }
.empty-text { color: #999; font-size: 16px; margin-top: 16px; }
</style>