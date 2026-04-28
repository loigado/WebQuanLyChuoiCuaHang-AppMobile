package com.branchmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "[AuditLog]")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // VD: DUYET_PHIEU, SUA_CAU_HINH, XOA_SAN_PHAM

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description; // Mô tả chi tiết bằng tiếng Việt

    @Column(name = "performed_by", nullable = false)
    private String performedBy; // Tên tài khoản (username) thực hiện

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { 
        this.createdAt = LocalDateTime.now(); 
    }

    public AuditLog() {}

    public AuditLog(String action, String description, String performedBy) {
        this.action = action;
        this.description = description;
        this.performedBy = performedBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}