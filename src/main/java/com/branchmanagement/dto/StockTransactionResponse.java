package com.branchmanagement.dto;

import java.io.Serializable; // ✅ Thêm import
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat; // ✅ Thêm import

public class StockTransactionResponse implements Serializable { // ✅ Cần thiết cho Redis
    private static final long serialVersionUID = 1L;

    private Integer transactionId;
    private String transactionCode;
    private String transactionType;
    private Integer productId;
    private String productCode;
    private String productName;
    private String productUnit;
    private Integer fromBranchId;
    private String fromBranchName;
    private Integer toBranchId;
    private String toBranchName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String reason;
    private String note;
    private String attachmentUrl;
    private String status;
    private Integer createdById;
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer approvedById;
    private String approvedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    private Integer rejectedById;
    private String rejectedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rejectedAt;
    
    private String rejectionReason;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualDate;

    public static class DetailDto implements Serializable { // ✅ Cần Serializable cho DetailDto
        private static final long serialVersionUID = 1L;
        public Integer productId;
        public String productCode;
        public String productName;
        public String productUnit;
        public BigDecimal quantity;
        public BigDecimal unitPrice;
    }
    
    private List<DetailDto> details;

    public StockTransactionResponse() {}

    // Getters & Setters
    public Integer getTransactionId() { return transactionId; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductUnit() { return productUnit; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }
    public Integer getFromBranchId() { return fromBranchId; }
    public void setFromBranchId(Integer fromBranchId) { this.fromBranchId = fromBranchId; }
    public String getFromBranchName() { return fromBranchName; }
    public void setFromBranchName(String fromBranchName) { this.fromBranchName = fromBranchName; }
    public Integer getToBranchId() { return toBranchId; }
    public void setToBranchId(Integer toBranchId) { this.toBranchId = toBranchId; }
    public String getToBranchName() { return toBranchName; }
    public void setToBranchName(String toBranchName) { this.toBranchName = toBranchName; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getCreatedById() { return createdById; }
    public void setCreatedById(Integer createdById) { this.createdById = createdById; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getApprovedById() { return approvedById; }
    public void setApprovedById(Integer approvedById) { this.approvedById = approvedById; }
    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public Integer getRejectedById() { return rejectedById; }
    public void setRejectedById(Integer rejectedById) { this.rejectedById = rejectedById; }
    public String getRejectedByName() { return rejectedByName; }
    public void setRejectedByName(String rejectedByName) { this.rejectedByName = rejectedByName; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }
    public LocalDateTime getActualDate() { return actualDate; }
    public void setActualDate(LocalDateTime actualDate) { this.actualDate = actualDate; }
    public List<DetailDto> getDetails() { return details; }
    public void setDetails(List<DetailDto> details) { this.details = details; }
}