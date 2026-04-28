import api from './axios';

export const userApi = {
  // Cho UserList.vue
  getAllPaged: (page, size) => api.get('/admin/users/paged', { params: { page, size } }),

  // ✅ ĐÃ SỬA: Gọi đúng endpoint users thay vì branches
  getAll: () => api.get('/admin/users/paged', { params: { page: 0, size: 1000 } }),

  // Khớp với @GetMapping("/branch/{branchId}")
  getByBranch: (branchId) => api.get(`/admin/users/branch/${branchId}`),
  
  // Khớp với @PatchMapping("/{id}/lock") và unlock
  lock: (id) => api.patch(`/admin/users/${id}/lock`),
  unlock: (id) => api.patch(`/admin/users/${id}/unlock`),

  // ✅ ĐÃ THÊM: Reset mật khẩu (UC 1.1)
  resetPassword: (id, newPassword) => api.patch(`/admin/users/${id}/reset-password`, { newPassword }),

  // Cho UserForm.vue
  getById: (id) => api.get(`/admin/users/${id}`),
  create: (data) => api.post('/admin/users', data),
  update: (id, data) => api.put(`/admin/users/${id}`, data)
};