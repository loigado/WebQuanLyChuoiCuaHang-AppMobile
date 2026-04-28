package com.branchmanagement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO nhận data khi Admin từ chối phiếu
 */
public class RejectTransactionRequest {

	/**
	 * Lý do từ chối (bắt buộc)
	 */
	@NotBlank(message = "Lý do từ chối không được để trống")
	private String rejectionReason;

	// Constructor rỗng
	public RejectTransactionRequest() {
	}

	// Getter & Setter
	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
}