import api from './axios';

export const cameraApi = {
  // ✅ BỎ CHỮ /api Ở ĐẦU (vì axios đã có sẵn baseURL rồi)
  adminGetAll: () => api.get('/admin/cameras'),
  managerGetByBranch: (branchId) => api.get(`/manager/cameras/my-branch/${branchId}`),
  
  create: (data) => api.post('/admin/cameras', data),
  delete: (id) => api.delete(`/admin/cameras/${id}`),
  
  getLiveUrl: (id) => api.get(`/admin/cameras/${id}/live`)
};