/**
 * Axios Configuration
 * Handles base URL, automatic JWT token attachment, and global error handling.
 */
import axios from 'axios';
import { ElMessage } from 'element-plus';

const api = axios.create({
  baseURL: 'http://localhost:8081/api', // Backend API Gateway
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * Request Interceptor
 * Automatically attaches the Bearer Token from sessionStorage to outgoing requests.
 */
api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('token'); 
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Response Interceptor
 * Centralized error handling for HTTP status codes (401, 403, etc.).
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const { response } = error;

    if (response) {
      handleHttpError(response);
    } else {
      ElMessage.error("Không thể kết nối tới Server. Vui lòng kiểm tra kết nối mạng!");
    }
    return Promise.reject(error);
  }
);

/**
 * Helper to handle specific HTTP error codes
 */
function handleHttpError(response) {
  switch (response.status) {
    case 401:
      ElMessage.error("Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!");
      sessionStorage.clear();
      window.location.href = '/login';
      break;
    case 403:
      ElMessage.warning("Bạn không có quyền thực hiện hành động này!");
      break;
    default:
      ElMessage.error(response.data?.message || "Đã xảy ra lỗi hệ thống");
  }
}

export default api;