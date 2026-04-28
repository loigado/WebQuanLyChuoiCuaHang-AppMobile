package com.branchmanagement.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExportStockRequest {

    @NotNull(message = "ID Sản phẩm không được để trống")
    private Integer productId;

    @NotNull(message = "ID Chi nhánh không được để trống")
    private Integer branchId; // Trong thực tế có thể lấy ngầm từ JWT của Manager

    @NotNull(message = "Số lượng xuất không được để trống")
    @DecimalMin(value = "0.01", message = "Số lượng xuất phải lớn hơn 0")
    private BigDecimal quantity;

    @NotBlank(message = "Lý do xuất kho không được để trống")
    private String reason; // Bán hàng, hỏng, mất... 

    private String note;
    private String attachmentUrl;

    // Getters & Setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
}