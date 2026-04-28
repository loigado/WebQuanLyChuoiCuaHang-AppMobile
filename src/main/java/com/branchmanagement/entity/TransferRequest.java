package com.branchmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TransferRequest")
@Data
public class TransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id") // ✅ BẮT BUỘC: Khớp với cột 'request_id' trong SQL Server
    private Integer requestId;

    @Column(name = "request_code", unique = true)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_branch_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Branch fromBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_branch_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Branch toBranch;

    @Column(name = "quantity", precision = 18, scale = 2)
    private BigDecimal quantity;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "priority", length = 20)
    private String priority; // Low, Normal, High, Urgent
    
    @Column(name = "expected_date")
    private LocalDate expectedDate;
    
    @Column(name = "status", length = 50)
    private String status; // Pending, Approved, Rejected, Shipping, Completed, Cancelled

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ Tự động gán ngày tạo khi lưu lần đầu
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ✅ Tự động cập nhật ngày sửa khi update
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}