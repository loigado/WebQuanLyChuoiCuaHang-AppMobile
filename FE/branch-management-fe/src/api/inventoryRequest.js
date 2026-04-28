import api from './axios';

export const inventoryApi = {
  // Lấy danh sách phiếu đang chờ duyệt
  getPending: (params) => api.get('/inventory-requests/pending', { params }),
  
  // Duyệt phiếu
  approve: (id) => api.put(`/inventory-requests/${id}/approve`),
  
  // Từ chối phiếu (kèm lý do)
  reject: (id, data) => api.put(`/inventory-requests/${id}/reject`, data),
  getHistory: (params) => api.get('/inventory-requests/history', { params }),
  findNearby: (productId) => api.get('/manager/inventory/find-nearby', { params: { productId } })
};