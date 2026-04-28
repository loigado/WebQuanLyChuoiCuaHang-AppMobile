package com.branchmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Inventory_Request")
@Data
public class InventoryRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Người tạo phiếu
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "request_type", nullable = false)
    private String requestType; // Các giá trị: IMPORT, EXPORT, ADJUST

    @Column(nullable = false)
    private Integer quantity; // Số lượng cần nhập/xuất

    @Column(length = 500)
    private String reason;

    @Column(length = 20)
    private String status = "pending"; // pending, approved, rejected

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}