import api from './axios';

export const configApi = {
  // Lấy toàn bộ danh sách cấu hình
  getAll: () => api.get('/admin/system-config'),
  
  // Cập nhật từng cái
  update: (key, value) => api.put(`/admin/system-config/${key}`, { configValue: value }),
  
  // Cập nhật hàng loạt (Sử dụng cho nút "Lưu tất cả")
  bulkUpdate: (configs) => api.put('/admin/system-config/bulk', { configs })
};