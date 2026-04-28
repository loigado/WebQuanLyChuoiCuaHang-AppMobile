import api from './axios';

export const branchApi = {
    // Lấy tất cả chi nhánh
    getAll() {
        return api.get('/admin/branches');
    },
    // Lấy chi tiết 1 chi nhánh
    getById(id) {
        return api.get(`/admin/branches/${id}`);
    },
    // Tạo mới chi nhánh
    create(data) {
        return api.post('/admin/branches', data);
    },
    // Cập nhật chi nhánh
    update(id, data) {
        return api.put(`/admin/branches/${id}`, data);
    },
    // Bật/Tắt trạng thái hoạt động
    toggleStatus(id) {
        return api.put(`/admin/branches/${id}/toggle`);
    }
};