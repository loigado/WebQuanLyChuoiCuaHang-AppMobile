import api from './axios';

export const attendanceApi = {
  // Chú ý đoạn { params: { page, size, status } }
  getAttendances: (page, size, status, fromDate, toDate) => 
    api.get('/manager/attendances', { params: { page, size, status, fromDate, toDate } }),
    
  getDetail: (id) => api.get(`/manager/attendances/${id}`),
  approve: (id) => api.put(`/manager/attendances/${id}/approve`),
  reject: (id, reason) => api.put(`/manager/attendances/${id}/reject`, { reason })
};