<template>
  <div class="p-6 max-w-lg mx-auto bg-white rounded-xl shadow-md">
    <h2 class="text-2xl font-bold text-center mb-4">Chấm công Ca Làm Việc</h2>

    <div class="relative bg-gray-200 rounded-lg overflow-hidden h-64 mb-4 flex justify-center items-center">
      <video ref="video" autoplay playsinline class="w-full h-full object-cover" v-show="!photoCaptured"></video>
      <canvas ref="canvas" class="hidden"></canvas>
      <img :src="photoCaptured" v-if="photoCaptured" class="w-full h-full object-cover" />
      
      <button v-if="photoCaptured" @click="retakePhoto" 
              class="absolute top-2 right-2 bg-red-500 text-white p-2 rounded-full text-xs">
        Chụp lại
      </button>
    </div>

    <div class="text-sm text-gray-600 mb-6 text-center">
      📍 Vị trí: <span v-if="latitude">{{ latitude.toFixed(5) }}, {{ longitude.toFixed(5) }}</span>
                 <span v-else class="text-red-500">Đang tìm vị trí GPS...</span>
    </div>

    <div class="flex gap-4">
      <el-button type="primary" class="w-full" size="large" @click="capturePhoto" v-if="!photoCaptured">
        📸 Chụp Ảnh Xác Nhận
      </el-button>

      <el-button type="success" class="w-full" size="large" @click="submitCheckIn" :loading="loading" v-if="photoCaptured">
        ✅ Xác Nhận Check-in
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import api from '@/api/axios.js'; 
import { Geolocation } from '@capacitor/geolocation'; // Đã thêm Capacitor Geolocation

const video = ref(null);
const canvas = ref(null);
const photoCaptured = ref(null);
const photoBlob = ref(null);

const latitude = ref(null);
const longitude = ref(null);
const loading = ref(false);

// Thông tin hardcode (Lưu ý: Bạn nên lấy từ store/localStorage sau này)
const currentUser = { id: 1, branchId: 1 }; 

let stream = null;

// 1. Khởi động Camera
const startCamera = async () => {
  try {
    stream = await navigator.mediaDevices.getUserMedia({ 
      video: { facingMode: "user" } // Ưu tiên camera trước
    });
    video.value.srcObject = stream;
  } catch (error) {
    console.error("Lỗi Camera:", error);
    ElMessage.error('Không thể mở Camera. Vui lòng cấp quyền!');
  }
};

// 2. Lấy tọa độ GPS (Dùng Capacitor để tránh Timeout trên Android)
const getLocation = async () => {
  try {
    const permissions = await Geolocation.checkPermissions();
    if (permissions.location !== 'granted') {
      await Geolocation.requestPermissions();
    }

    // Tuyệt chiêu: Lấy vị trí với timeout cực lớn và không dùng cache
    const position = await Geolocation.getCurrentPosition({
      enableHighAccuracy: true,  // ✅ Bật chính xác cao
      timeout: 30000,            // Cho nó hẳn 30 giây để tìm
      maximumAge: 0              // ✅ Lấy vị trí mới nhất
    });

    latitude.value = position.coords.latitude;
    longitude.value = position.coords.longitude;
    
  } catch (error) {
    // Nếu vẫn Timeout, thử "mồi" bằng cách dùng GPS của trình duyệt web (dự phòng)
    navigator.geolocation.getCurrentPosition((pos) => {
       latitude.value = pos.coords.latitude;
       longitude.value = pos.coords.longitude;
    });
    console.error('Lỗi GPS:', error);
  }
};

// 3. Xử lý Chụp ảnh & Nén ảnh tránh lỗi OOM (Out of Memory)
const capturePhoto = () => {
  const context = canvas.value.getContext('2d');
  
  // Giới hạn chiều rộng tối đa 1024px để tiết kiệm RAM
  const MAX_WIDTH = 1024;
  let width = video.value.videoWidth;
  let height = video.value.videoHeight;

  if (width > MAX_WIDTH) {
    height *= MAX_WIDTH / width;
    width = MAX_WIDTH;
  }

  canvas.value.width = width;
  canvas.value.height = height;
  
  // Vẽ ảnh thu nhỏ lên Canvas
  context.drawImage(video.value, 0, 0, width, height);
  
  // Giải phóng bộ nhớ của tấm ảnh cũ trước khi tạo mới
  if (photoCaptured.value) {
    URL.revokeObjectURL(photoCaptured.value);
  }

  // Chuyển thành Blob với chất lượng 0.6 (file nhẹ nhưng vẫn nét)
  canvas.value.toBlob((blob) => {
    photoBlob.value = blob;
    photoCaptured.value = URL.createObjectURL(blob); 
  }, 'image/jpeg', 0.6); 
};

const retakePhoto = () => {
  photoCaptured.value = null;
  photoBlob.value = null;
};

// 4. Gửi dữ liệu Check-in lên Server
const submitCheckIn = async () => {
  if (!latitude.value || !longitude.value) {
    return ElMessage.warning('Đang đợi tín hiệu GPS, vui lòng chờ...');
  }
  if (!photoBlob.value) {
    return ElMessage.warning('Vui lòng chụp ảnh khuôn mặt!');
  }

  loading.value = true;
  
  const formData = new FormData();
  formData.append('userId', currentUser.id);
  formData.append('branchId', currentUser.branchId);
  
  // Khớp chính xác tên biến với tham số trong Spring Boot
  formData.append('userLat', latitude.value);
  formData.append('userLon', longitude.value);
  
  // Đính kèm file ảnh
  formData.append('photo', photoBlob.value, 'selfie.jpg');

  try {
    // ✅ Sửa: Dùng API mobile
    const res = await api.post('/mobile/check-in', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    const successMsg = typeof res.data === 'string' ? res.data : (res.data.message || 'Check-in thành công!');
    ElMessage.success(successMsg);
    
    // Xử lý sau khi thành công (ví dụ: chuyển trang)
    // router.push('/history');

  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || "Lỗi kết nối Server";
    ElMessage.error('Lỗi Check-in: ' + errorMsg);
    console.error("Chi tiết lỗi Backend:", error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  startCamera();
  getLocation();
});

// Giải phóng tài nguyên camera khi rời trang
onUnmounted(() => {
  if (stream) {
    stream.getTracks().forEach(track => track.stop());
  }
  if (photoCaptured.value) {
    URL.revokeObjectURL(photoCaptured.value);
  }
});
</script>