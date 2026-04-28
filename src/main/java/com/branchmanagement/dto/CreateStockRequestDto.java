package com.branchmanagement.dto;

import java.math.BigDecimal;
import java.util.List;

public class CreateStockRequestDto {
    private String transactionType;
    private Integer fromBranchId;
    private Integer toBranchId;
    private String reason;
    private String note;
    
    // Danh sách các món hàng
    private List<DetailDto> details;

    // --- LỚP CON (Chi tiết món hàng) ---
    public static class DetailDto {
        private Integer productId;
        private BigDecimal quantity;
        private BigDecimal unitPrice;

        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }

    // --- GETTERS & SETTERS LỚP CHA ---
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public Integer getFromBranchId() { return fromBranchId; }
    public void setFromBranchId(Integer fromBranchId) { this.fromBranchId = fromBranchId; }
    public Integer getToBranchId() { return toBranchId; }
    public void setToBranchId(Integer toBranchId) { this.toBranchId = toBranchId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<DetailDto> getDetails() { return details; }
    public void setDetails(List<DetailDto> details) { this.details = details; }
}