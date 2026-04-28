import api from './axios';

export const leaveApi = {
  // Manager xem danh sách đơn của chi nhánh → ManagerLeaveController
  getForManager: (params) => api.get('/manager/leave-requests', { params }),

  // Duyệt đơn
  approve: (id) => api.put(`/manager/leave-requests/${id}/approve`),

  // Từ chối kèm lý do (field: rejectReason)
  reject: (id, reason) => api.put(`/manager/leave-requests/${id}/reject`, { rejectReason: reason })
};