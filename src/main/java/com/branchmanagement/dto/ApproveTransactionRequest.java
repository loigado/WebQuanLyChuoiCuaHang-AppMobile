package com.branchmanagement.dto;

/**
 * DTO nhận data khi Admin duyệt phiếu
 */
public class ApproveTransactionRequest {

	/**
	 * Ghi chú khi duyệt (optional)
	 */
	private String note;

	// Constructor rỗng
	public ApproveTransactionRequest() {
	}

	// Getter & Setter
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}