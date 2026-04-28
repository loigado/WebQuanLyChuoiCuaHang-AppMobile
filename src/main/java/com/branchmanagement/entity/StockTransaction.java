package com.branchmanagement.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "[StockTransaction]")
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "transaction_code", unique = true, nullable = false, length = 50)
    private String transactionCode;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    // ✅ ĐÃ XÓA 3 TRƯỜNG CŨ (Product, Quantity, UnitPrice)
    // ✅ ĐÃ THÊM MỚI: Liên kết 1-Nhiều tới bảng Chi tiết (Line Items)
    @OneToMany(mappedBy = "stockTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTransactionDetail> details = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private TransferRequest transferRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_branch_id")
    private Branch fromBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_branch_id")
    private Branch toBranch;

    @Column(name = "reason", length = 200, columnDefinition = "NVARCHAR(200)")
    private String reason;

    @Column(name = "note", length = 500, columnDefinition = "NVARCHAR(500)")
    private String note;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "status", length = 20)
    private String status = "pending";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private User rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 200, columnDefinition = "NVARCHAR(200)") // ✅ Ép kiểu tiếng Việt
    private String rejectionReason;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(name = "actual_date")
    private LocalDateTime actualDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // --- HELPER METHOD CHO BẢNG CHI TIẾT ---
    public void addDetail(StockTransactionDetail detail) {
        details.add(detail);
        detail.setStockTransaction(this);
    }

    // --- GETTERS & SETTERS ---
    public Integer getTransactionId() { return transactionId; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    
    // Getter/Setter cho List Details
    public List<StockTransactionDetail> getDetails() { return details; }
    public void setDetails(List<StockTransactionDetail> details) { this.details = details; }

    public TransferRequest getTransferRequest() { return transferRequest; }
    public void setTransferRequest(TransferRequest transferRequest) { this.transferRequest = transferRequest; }
    public Branch getFromBranch() { return fromBranch; }
    public void setFromBranch(Branch fromBranch) { this.fromBranch = fromBranch; }
    public Branch getToBranch() { return toBranch; }
    public void setToBranch(Branch toBranch) { this.toBranch = toBranch; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public User getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(User rejectedBy) { this.rejectedBy = rejectedBy; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }
    public LocalDateTime getActualDate() { return actualDate; }
    public void setActualDate(LocalDateTime actualDate) { this.actualDate = actualDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}