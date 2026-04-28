<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="/home"></ion-back-button>
        </ion-buttons>
        <ion-title>Lịch Làm Việc</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding-top">
      
      <div class="filter-section ion-padding-horizontal">
        <ion-segment v-model="selectedView" @ionChange="loadSchedule">
          <ion-segment-button value="week">
            <ion-label>Tuần này</ion-label>
          </ion-segment-button>
          <ion-segment-button value="month">
            <ion-label>Tháng này</ion-label>
          </ion-segment-button>
        </ion-segment>
      </div>

      <ion-list v-if="schedules.length > 0" class="ion-margin-top">
        <ion-item v-for="(item, index) in schedules" :key="index" class="schedule-card" lines="none">
          
          <div slot="start" class="date-box" :class="{ 'is-today': isToday(item.date) }">
            <span class="day">{{ getDayName(item.date) }}</span>
            <span class="date">{{ getDateOnly(item.date) }}</span>
          </div>

          <ion-label>
            <h2 class="shift-name">{{ item.shiftName }}</h2>
            <p class="time-range">
              <ion-icon :icon="timeOutline"></ion-icon> 
              {{ item.startTime }} - {{ item.endTime }}
            </p>
            <p class="branch-name">
              <ion-icon :icon="locationOutline"></ion-icon> 
              {{ item.branchName }}
            </p>
          </ion-label>

        </ion-item>
      </ion-list>

      <div v-else class="empty-state ion-text-center">
        <ion-icon :icon="calendarClearOutline"></ion-icon>
        <h3>Trống lịch</h3>
        <p>Bạn chưa được xếp ca làm việc nào trong thời gian này.</p>
      </div>

    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
/**
 * SchedulePage Component
 * Displays the work schedule for the logged-in employee.
 * Handles different date formats and maps backend data to a unified UI structure.
 */
import { ref, onMounted } from 'vue';
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, 
  IonContent, IonList, IonItem, IonLabel, IonSegment, IonSegmentButton, 
  IonIcon, loadingController, toastController 
} from '@ionic/vue';
import { timeOutline, locationOutline, calendarClearOutline } from 'ionicons/icons';
import apiClient from '@/services/api';

// --- State Management ---
const selectedView = ref('week');
const schedules = ref<any[]>([]);

// --- Lifecycle Hooks ---
onMounted(() => {
  loadSchedule();
});

// --- Actions ---
/**
 * Loads the employee's work schedule from the server based on the selected view (week/month).
 */
const loadSchedule = async () => {
  const loading = await loadingController.create({ 
    message: 'Đang tải lịch làm việc...',
    duration: 5000 
  });
  await loading.present();

  try {
    const userId = localStorage.getItem('user_id');
    const { start, end } = getDateRange(selectedView.value);
    
    const response = await apiClient.get(`/mobile/my-shifts`, {
      params: { 
        userId,
        startDate: start,
        endDate: end
      }
    });
    
    const allShifts = (response.data.data || []).map(mapShiftItem);
    const todayStr = new Date().toISOString().split('T')[0];
    
    // Chỉ lấy những ca làm việc có ngày >= ngày hôm nay
    schedules.value = allShifts.filter((item: any) => item.date >= todayStr);

  } catch (error) {
    console.error("Failed to load schedule:", error);
    showErrorToast('Lỗi khi tải lịch làm việc. Vui lòng thử lại sau.');
  } finally {
    await loading.dismiss();
  }
};

/**
 * Calculates the start and end dates for the selected view.
 */
const getDateRange = (view: string) => {
  const now = new Date();
  let start = new Date(now);
  let end = new Date(now);

  if (view === 'week') {
    // Current week (Monday to Sunday)
    const day = now.getDay();
    const diff = now.getDate() - day + (day === 0 ? -6 : 1); 
    start = new Date(now.setDate(diff));
    end = new Date(start);
    end.setDate(start.getDate() + 6);
  } else {
    // Current month
    start = new Date(now.getFullYear(), now.getMonth(), 1);
    end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  }

  return {
    start: start.toISOString().split('T')[0],
    end: end.toISOString().split('T')[0]
  };
};

/**
 * Maps a single shift item from the backend to the UI model.
 */
const mapShiftItem = (item: any) => {
  let rawDate = item.date || item.shift?.date || item.shiftDate;
  if (Array.isArray(rawDate)) {
    rawDate = `${rawDate[0]}-${String(rawDate[1]).padStart(2, '0')}-${String(rawDate[2]).padStart(2, '0')}`;
  }

  let start = item.startTime || item.shift?.startTime;
  if (typeof start === 'string' && start.length >= 5) {
    start = start.substring(0, 5);
  } else if (Array.isArray(start)) {
    start = `${String(start[0]).padStart(2, '0')}:${String(start[1]).padStart(2, '0')}`;
  }

  let end = item.endTime || item.shift?.endTime;
  if (typeof end === 'string' && end.length >= 5) {
    end = end.substring(0, 5);
  } else if (Array.isArray(end)) {
    end = `${String(end[0]).padStart(2, '0')}:${String(end[1]).padStart(2, '0')}`;
  }

  const branchName = item.branchName 
                || item.shift?.branch?.branchName 
                || item.shift?.branchName 
                || 'Liên hệ quản lý';

  return {
    date: rawDate,
    shiftName: item.shiftName || item.shift?.shiftName || 'Ca làm việc',
    startTime: start,
    endTime: end,
    branchName
  };
};

const showErrorToast = async (message: string) => {
  const toast = await toastController.create({
    message,
    duration: 3000,
    color: 'danger',
    position: 'bottom'
  });
  await toast.present();
};

// --- Helper Functions for Date Rendering ---
const isToday = (dateString: string) => {
  const today = new Date().toISOString().split('T')[0];
  return dateString === today;
};

const getDayName = (dateString: string) => {
  const days = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
  return days[new Date(dateString).getDay()];
};

const getDateOnly = (dateString: string) => {
  const date = new Date(dateString);
  return `${date.getDate()}/${date.getMonth() + 1}`;
};
</script>

<style scoped>
.filter-section {
  margin-bottom: 15px;
}

.schedule-card {
  --background: #ffffff;
  --padding-start: 10px;
  --inner-padding-end: 10px;
  margin: 10px 15px;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

.date-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f4f5f8;
  color: #666;
  width: 60px;
  height: 60px;
  border-radius: 10px;
  margin-right: 15px;
  margin-top: 10px;
  margin-bottom: 10px;
}

.date-box.is-today {
  background-color: #3880ff;
  color: white;
}

.date-box .day {
  font-size: 14px;
  font-weight: bold;
}

.date-box .date {
  font-size: 16px;
  font-weight: bold;
}

.shift-name {
  font-size: 18px;
  font-weight: bold;
  color: #222;
  margin-bottom: 5px;
}

.time-range, .branch-name {
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 3px;
}

.empty-state {
  margin-top: 80px;
  color: #999;
}
.empty-state ion-icon {
  font-size: 80px;
  color: #d7d8da;
}
</style>