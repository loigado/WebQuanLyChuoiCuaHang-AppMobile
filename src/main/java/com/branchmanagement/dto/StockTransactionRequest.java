package com.branchmanagement.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockTransactionRequest {

	@NotBlank(message = "Loại giao dịch không được để trống (import, export, transfer)")
	private String transactionType;

	@NotNull(message = "Sản phẩm không được để trống")
	private Integer productId;

	private Integer fromBranchId; // Bắt buộc nếu là export hoặc transfer
	
	private Integer toBranchId;   // Bắt buộc nếu là import hoặc transfer

	@NotNull(message = "Số lượng không được để trống")
	@DecimalMin(value = "0.01", message = "Số lượng phải lớn hơn 0")
	private BigDecimal quantity;

	private BigDecimal unitPrice; // Tùy chọn, thường dùng khi nhập kho

	@NotBlank(message = "Lý do không được để trống")
	private String reason;

	private String note;
	
	private String attachmentUrl; // Link ảnh hóa đơn/chứng từ (tùy chọn)

	public StockTransactionRequest() {
	}

	// ==========================================
	// GETTERS & SETTERS
	// ==========================================

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getFromBranchId() {
		return fromBranchId;
	}

	public void setFromBranchId(Integer fromBranchId) {
		this.fromBranchId = fromBranchId;
	}

	public Integer getToBranchId() {
		return toBranchId;
	}

	public void setToBranchId(Integer toBranchId) {
		this.toBranchId = toBranchId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getAttachmentUrl() {
		return attachmentUrl;
	}

	public void setAttachmentUrl(String attachmentUrl) {
		this.attachmentUrl = attachmentUrl;
	}
}