<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="/home"></ion-back-button>
        </ion-buttons>
        <ion-title>Duyệt phiếu kho</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding bg-light">
      <ion-refresher slot="fixed" @ionRefresh="doRefresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>

      <div v-if="loading" class="ion-text-center ion-padding">
        <ion-spinner name="crescent"></ion-spinner>
        <p>Đang tải danh sách...</p>
      </div>

      <div v-else-if="requests.length === 0" class="empty-state">
        <ion-icon :icon="listOutline" class="empty-icon"></ion-icon>
        <h4>Không có phiếu nào chờ duyệt</h4>
        <p>Mọi thứ đã được xử lý xong!</p>
      </div>

      <div v-else>
        <ion-card v-for="req in requests" :key="req.transactionId" class="request-card">
          <ion-card-header>
            <div class="header-row">
              <ion-badge :color="getTypeColor(req.transactionType)">{{ formatType(req.transactionType) }}</ion-badge>
              <span class="code">{{ req.transactionCode }}</span>
            </div>
            <ion-card-title class="product-name">{{ req.productName }}</ion-card-title>
            <ion-card-subtitle>Số lượng: <strong>{{ req.quantity }}</strong></ion-card-subtitle>
          </ion-card-header>

          <ion-card-content>
            <div class="info-row">
              <ion-icon :icon="businessOutline"></ion-icon>
              <span>Từ: {{ req.fromBranchName || 'Hệ thống' }}</span>
            </div>
            <div class="info-row">
              <ion-icon :icon="arrowForwardOutline"></ion-icon>
              <span>Đến: {{ req.toBranchName || 'Hệ thống' }}</span>
            </div>
            <div class="info-row" v-if="req.reason">
              <ion-icon :icon="documentTextOutline"></ion-icon>
              <span>Lý do: {{ req.reason }}</span>
            </div>
            
            <div class="action-buttons ion-margin-top">
              <ion-button expand="block" color="success" @click="handleAction(req, 'APPROVE')">
                <ion-icon :icon="checkmarkCircleOutline" slot="start"></ion-icon>
                Duyệt ngay
              </ion-button>
              <ion-button expand="block" color="danger" fill="outline" @click="handleAction(req, 'REJECT')">
                <ion-icon :icon="closeCircleOutline" slot="start"></ion-icon>
                Từ chối
              </ion-button>
            </div>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonBackButton,
  IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle, IonCardContent, IonBadge,
  IonIcon, IonButton, IonSpinner, IonRefresher, IonRefresherContent, alertController, toastController
} from '@ionic/vue';
import { 
  listOutline, businessOutline, arrowForwardOutline, documentTextOutline,
  checkmarkCircleOutline, closeCircleOutline 
} from 'ionicons/icons';
import { stockService, PendingRequest } from '@/services/stock';

const loading = ref(true);
const requests = ref<PendingRequest[]>([]);
const userId = Number(localStorage.getItem('user_id'));

const fetchData = async () => {
  try {
    loading.value = true;
    const res = await stockService.getPendingRequests(userId);
    requests.value = res.data.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const doRefresh = async (event: any) => {
  await fetchData();
  event.target.complete();
};

const handleAction = async (req: PendingRequest, decision: 'APPROVE' | 'REJECT') => {
  const alert = await alertController.create({
    header: decision === 'APPROVE' ? 'Xác nhận duyệt' : 'Từ chối phiếu',
    message: decision === 'APPROVE' 
      ? `Bạn có chắc chắn muốn duyệt phiếu ${req.transactionCode}?`
      : 'Vui lòng nhập lý do từ chối:',
    inputs: decision === 'REJECT' ? [
      {
        name: 'note',
        type: 'textarea',
        placeholder: 'Lý do...'
      }
    ] : [
      {
        name: 'note',
        type: 'text',
        placeholder: 'Ghi chú (không bắt buộc)'
      }
    ],
    buttons: [
      { text: 'Hủy', role: 'cancel' },
      {
        text: decision === 'APPROVE' ? 'Đồng ý duyệt' : 'Xác nhận từ chối',
        handler: async (data) => {
          try {
            await stockService.processRequest(userId, req.transactionId, decision, data.note);
            const toast = await toastController.create({
              message: decision === 'APPROVE' ? 'Đã duyệt phiếu thành công!' : 'Đã từ chối phiếu.',
              duration: 2000,
              color: decision === 'APPROVE' ? 'success' : 'warning'
            });
            await toast.present();
            fetchData(); // Tải lại danh sách
          } catch (err) {
            console.error(err);
          }
        }
      }
    ]
  });
  await alert.present();
};

const formatType = (type: string) => {
  const maps: any = {
    'import': 'Nhập kho',
    'export': 'Xuất kho',
    'transfer': 'Điều chuyển',
    'adjustment': 'Cân bằng'
  };
  return maps[type] || type;
};

const getTypeColor = (type: string) => {
  if (type === 'import') return 'success';
  if (type === 'export') return 'danger';
  if (type === 'transfer') return 'primary';
  return 'warning';
};

onMounted(fetchData);
</script>

<style scoped>
.bg-light {
  --background: #f4f5f8;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 70%;
  color: #92949c;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.request-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.code {
  font-size: 12px;
  font-family: monospace;
  color: #666;
}

.product-name {
  font-weight: 800;
  color: #1a1a1a;
  font-size: 18px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
  color: #4a4a4a;
  font-size: 14px;
}

.info-row ion-icon {
  font-size: 16px;
  color: #3880ff;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
