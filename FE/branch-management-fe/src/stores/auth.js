import { defineStore } from 'pinia';
import api from '@/api/axios';

export const useAuthStore = defineStore('auth', {
  state: () => {
    // ĐỔI SANG sessionStorage: Tự động xóa khi đóng trình duyệt
    const bId = sessionStorage.getItem('branchId');
    return {
      token: sessionStorage.getItem('token') || null,
      user: JSON.parse(sessionStorage.getItem('user')) || null,
      role: sessionStorage.getItem('role') || null,
      // Ép kiểu về Number nếu có dữ liệu, tránh chữ "null"
      branchId: (bId === 'null' || !bId) ? null : Number(bId),
    };
  },
  
  actions: {
    async login(username, password) {
      try {
        const response = await api.post('/auth/login', { username, password });
        const data = response.data.data;
        
        // Cập nhật Store - ✅ Lưu đầy đủ thông tin user
        this.token = data.token;
        this.role = data.role;
        this.user = { 
          id: data.id, 
          username: data.username, 
          fullName: data.fullName,
          branchId: data.branchId 
        };
        this.branchId = data.branchId; // Backend trả về

        // Lưu vào sessionStorage
        sessionStorage.setItem('token', data.token);
        sessionStorage.setItem('role', data.role);
        sessionStorage.setItem('user', JSON.stringify(this.user));
        
        // Lưu branchId dưới dạng chuỗi đơn giản
        if (data.branchId !== null && data.branchId !== undefined) {
          sessionStorage.setItem('branchId', String(data.branchId));
        } else {
          sessionStorage.removeItem('branchId');
        }
        
        return data;
      } catch (error) {
        throw error;
      }
    },

    logout() {
      this.token = null;
      this.user = null;
      this.role = null;
      this.branchId = null;
      // Xóa toàn bộ dữ liệu phiên làm việc
      sessionStorage.clear(); 
      location.href = '/login';
    }
  }
});