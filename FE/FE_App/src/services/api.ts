import axios from 'axios';
import { Capacitor } from '@capacitor/core';
import { ElMessage } from 'element-plus';

// Tự động chuyển đổi URL: App thì dùng IP, Web thì dùng localhost
export const getServerUrl = () => {
  if (Capacitor.isNativePlatform()) {
    // Chạy trên máy ảo hoặc điện thoại thật
    return 'http://192.168.1.8:8081'; 
  } else {
    // Chạy trên trình duyệt Web máy tính
    return 'http://localhost:8081'; 
  }
};

export const API_BASE_URL = `${getServerUrl()}/api`;

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000 // Tăng timeout lên 15s cho chắc
});

// Interceptor cho Request: Tự động đính kèm Token vào Header
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor cho Response: Bắt lỗi tập trung để không sập App/Web
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const { response } = error;

    if (response) {
      if (response.status === 401) {
        ElMessage.error("Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!");
        localStorage.clear();
        window.location.href = '/login';
      } else if (response.status === 403) {
        ElMessage.warning("Bạn không có quyền thực hiện hành động này!");
      } else {
        ElMessage.error(response.data?.message || "Đã xảy ra lỗi hệ thống");
      }
    } else {
      // Khi server tắt, sai IP, hoặc khác mạng Wi-Fi
      ElMessage.error("Không thể kết nối tới Server. Vui lòng kiểm tra lại mạng!");
    }
    return Promise.reject(error);
  }
);

export default apiClient;