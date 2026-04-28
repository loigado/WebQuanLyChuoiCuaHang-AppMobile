import api from './axios';

export const shiftApi = {
  // Lấy danh sách ca kèm nhân viên đã gán
  // Lưu ý: Backend dùng @PathVariable cho branchId và @RequestParam cho ngày
  getByBranch: (branchId, fromDate, toDate) => 
    api.get(`/manager/shifts/branch/${branchId}`, { 
      params: { fromDate, toDate } 
    }),

  // Tạo ca làm việc mới
  create: (payload) => api.post('/manager/shifts', payload),

  // Phân ca cho nhân viên: POST /api/manager/shifts/{shiftId}/assign/{userId}
  assignUser: (shiftId, userId) => 
    api.post(`/manager/shifts/${shiftId}/assign/${userId}`),

  // Hủy phân ca: DELETE /api/manager/shifts/{shiftId}/assign/{userShiftId}
  removeAssign: (shiftId, userShiftId) => 
    api.delete(`/manager/shifts/${shiftId}/assign/${userShiftId}`),

  // Hàm xóa ca làm việc
  deleteShift: (shiftId) => {
    return api.delete(`/manager/shifts/${shiftId}`);
  },

  // Hàm sửa nhân viên
  updateUserAssign: (userShiftId, newUserId) => {
    return api.put(`/manager/shifts/user-shift/${userShiftId}`, { userId: newUserId });
  }
};