<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="/home"></ion-back-button>
        </ion-buttons>
        <ion-title>Đơn Xin Nghỉ</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-list v-if="leaveRequests.length > 0">
        <ion-item v-for="item in leaveRequests" :key="item.leaveId" class="leave-card" lines="none">
          <ion-label class="ion-text-wrap">
            <h2 style="color: #3880ff; font-weight: bold;">{{ getLeaveTypeName(item.leaveType) }}</h2>
            <p>Từ: <strong>{{ formatDate(item.startDate) }}</strong> - Đến: <strong>{{ formatDate(item.endDate) }}</strong></p>
            <p>Lý do: {{ item.reason }}</p>
            <p v-if="item.rejectReason" style="color: red; font-size: 13px;">Lý do từ chối: {{ item.rejectReason }}</p>
            <p style="font-size: 12px; color: gray;">Ngày gửi: {{ formatDateTime(item.createdAt) }}</p>
          </ion-label>
          <ion-badge slot="end" :color="getStatusColor(item.status)">
            {{ getStatusText(item.status) }}
          </ion-badge>
        </ion-item>
      </ion-list>

      <div v-else class="ion-text-center" style="margin-top: 50px; color: gray;">
        <ion-icon :icon="documentTextOutline" style="font-size: 64px;"></ion-icon>
        <p>Bạn chưa có đơn xin nghỉ nào.</p>
      </div>

      <ion-fab vertical="bottom" horizontal="end" slot="fixed">
        <ion-fab-button @click="isOpenModal = true">
          <ion-icon :icon="addOutline"></ion-icon>
        </ion-fab-button>
      </ion-fab>

      <ion-modal :is-open="isOpenModal" @didDismiss="isOpenModal = false">
        <ion-header>
          <ion-toolbar>
            <ion-title>Tạo Đơn Xin Nghỉ</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="isOpenModal = false">Đóng</ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          
          <ion-item class="input-item">
            <ion-label position="stacked">Loại nghỉ phép</ion-label>
            <ion-select v-model="form.leaveType" placeholder="Chọn loại nghỉ">
              <ion-select-option value="Annual">Nghỉ phép năm</ion-select-option>
              <ion-select-option value="Sick">Nghỉ ốm</ion-select-option>
              <ion-select-option value="Private">Nghỉ việc riêng</ion-select-option>
            </ion-select>
          </ion-item>

          <ion-item class="input-item ion-margin-top">
            <ion-label position="stacked">Từ ngày</ion-label>
            <ion-input type="date" v-model="form.startDate"></ion-input>
          </ion-item>

          <ion-item class="input-item ion-margin-top">
            <ion-label position="stacked">Đến ngày</ion-label>
            <ion-input type="date" v-model="form.endDate"></ion-input>
          </ion-item>
          
          <ion-item class="input-item ion-margin-top">
            <ion-label position="stacked">Lý do nghỉ</ion-label>
            <ion-textarea v-model="form.reason" :rows="3" placeholder="Nhập lý do chi tiết..."></ion-textarea>
          </ion-item>

          <ion-button expand="block" class="ion-margin-top" @click="submitLeave">
            GỬI ĐƠN
          </ion-button>
        </ion-content>
      </ion-modal>

    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonBackButton,
  IonList, IonItem, IonLabel, IonBadge, IonFab, IonFabButton, IonIcon,
  IonModal, IonButton, IonInput, IonTextarea, IonSelect, IonSelectOption,
  toastController, loadingController
} from '@ionic/vue';
import { addOutline, documentTextOutline } from 'ionicons/icons';
import apiClient from '@/services/api';

const leaveRequests = ref<any[]>([]);
const isOpenModal = ref(false);
const form = reactive({ leaveType: 'Annual', startDate: '', endDate: '', reason: '' });

onMounted(() => {
  loadLeaveRequests();
});

const loadLeaveRequests = async () => {
  try {
    const userId = localStorage.getItem('user_id');
    const response = await apiClient.get(`/mobile/leave-requests?userId=${userId}`);
    leaveRequests.value = response.data.data;
  } catch (error) {
    console.error("Lỗi tải đơn:", error);
  }
};

const submitLeave = async () => {
  if (!form.startDate || !form.endDate || !form.reason.trim()) {
    showToast('Vui lòng điền đầy đủ thông tin', 'warning');
    return;
  }
  if (new Date(form.startDate) > new Date(form.endDate)) {
    showToast('Ngày bắt đầu không được lớn hơn ngày kết thúc', 'warning');
    return;
  }
  
  const loading = await loadingController.create({ message: 'Đang gửi đơn...' });
  await loading.present();

  try {
    const userId = localStorage.getItem('user_id');
    const response = await apiClient.post(`/mobile/leave-requests?userId=${userId}`, form);
    
    showToast(response.data.message, 'success');
    isOpenModal.value = false;
    form.startDate = ''; form.endDate = ''; form.reason = '';
    loadLeaveRequests(); 
  } catch (error) {
    showToast('Lỗi gửi đơn', 'danger');
  } finally {
    await loading.dismiss();
  }
};

// --- Helper Functions ---
const getLeaveTypeName = (type: string) => {
  const map: any = { 'Annual': 'Nghỉ phép năm', 'Sick': 'Nghỉ ốm', 'Private': 'Việc riêng' };
  return map[type] || type;
};

const getStatusText = (status: string) => {
  if (status === 'pending') return 'Chờ duyệt';
  if (status === 'approved') return 'Đã duyệt';
  if (status === 'rejected') return 'Từ chối';
  return status;
};

const getStatusColor = (status: string) => {
  if (status === 'pending') return 'warning';
  if (status === 'approved') return 'success';
  if (status === 'rejected') return 'danger';
  return 'medium';
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString('vi-VN', { hour: '2-digit', minute:'2-digit', day:'2-digit', month:'2-digit', year:'numeric' });
};

const showToast = async (msg: string, color: string) => {
  const toast = await toastController.create({ message: msg, duration: 2500, color: color });
  await toast.present();
};
</script>

<style scoped>
.leave-card { --background: #fff; margin-bottom: 12px; border-radius: 10px; border: 1px solid #eee; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
.input-item { --background: #f4f5f8; border-radius: 10px; }
</style>