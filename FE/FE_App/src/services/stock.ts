import apiClient from './api';

export interface PendingRequest {
  transactionId: number;
  transactionCode: string;
  transactionType: string;
  status: string;
  reason: string;
  createdAt: string;
  fromBranchName: string;
  toBranchName: string;
  productName: string;
  quantity: number;
}

export const stockService = {
  // Lấy tồn kho chi nhánh
  getBranchInventory(userId: number) {
    return apiClient.get(`/mobile/inventory?userId=${userId}`);
  },

  // Lấy danh sách phiếu chờ duyệt
  getPendingRequests(userId: number) {
    return apiClient.get(`/mobile/pending-requests?userId=${userId}`);
  },

  // Duyệt hoặc từ chối phiếu
  processRequest(userId: number, transactionId: number, decision: 'APPROVE' | 'REJECT', note: string = '') {
    return apiClient.post(`/mobile/approve-request`, null, {
      params: { userId, transactionId, decision, note }
    });
  }
};
