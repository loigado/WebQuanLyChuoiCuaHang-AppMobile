package com.branchmanagement.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.branchmanagement.dto.ApproveTransactionRequest;
import com.branchmanagement.dto.RejectTransactionRequest;
import com.branchmanagement.dto.StockTransactionResponse;
import java.time.LocalDate;
/**
 * Interface định nghĩa nghiệp vụ duyệt phiếu kho
 */
public interface StockApprovalService {
	Page<StockTransactionResponse> getPendingTransactions(int page, int size);

	Page<StockTransactionResponse> getHistory(LocalDate startDate, LocalDate endDate, int page, int size);

	/**
	 * Lấy danh sách phiếu đang chờ duyệt (status = 'pending')
	 */
	List<StockTransactionResponse> getPendingTransactions();

	/**
	 * Lấy phiếu chờ duyệt theo loại (import/export/adjustment)
	 */
	List<StockTransactionResponse> getPendingByType(String transactionType);

	/**
	 * Duyệt phiếu (Approve) Logic: - Phiếu nhập (import): Cộng Stock.quantity -
	 * Phiếu xuất (export): Trừ Stock.quantity và trừ reserved_quantity - Phiếu điều
	 * chỉnh (adjustment): Set Stock.quantity = số lượng thực tế - Phiếu chuyển kho
	 * (transfer): Xử lý cả nguồn và đích
	 * 
	 * @param transactionId ID phiếu
	 * @param request       Ghi chú (optional)
	 * @param adminId       ID admin đang duyệt
	 */
	StockTransactionResponse approveTransaction(Integer transactionId, ApproveTransactionRequest request,
			Integer adminId);

	/**
	 * Từ chối phiếu (Reject) Logic: - Set status = 'rejected' - Nếu là phiếu xuất:
	 * Trả lại reserved_quantity - Tạo thông báo cho người tạo phiếu
	 * 
	 * @param transactionId ID phiếu
	 * @param request       Lý do từ chối (bắt buộc)
	 * @param adminId       ID admin đang từ chối
	 */
	StockTransactionResponse rejectTransaction(Integer transactionId, RejectTransactionRequest request,
			Integer adminId);
}