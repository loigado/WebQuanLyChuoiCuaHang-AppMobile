import api from './axios';

export const reportApi = {
  // Xuất file Excel báo cáo cho Admin
  exportAdminExcel: () => api.get('/admin/reports/inventory/excel', { responseType: 'blob' }),

  // Xuất file Excel báo cáo cho Kế toán
  exportFinancialExcel: () => api.get('/accountant/reports/export-excel', { responseType: 'blob' })
};