package com.branchmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "SystemAuditLog")
@Data
public class SystemAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username")
    private String username;

    @Column(name = "action", nullable = false)
    private String action; // Giá trị từ @AuditAction

    @Column(name = "method_signature")
    private String methodSignature; // Tên hàm được gọi

    @Column(name = "request_data", columnDefinition = "NVARCHAR(MAX)")
    private String requestData; // Dữ liệu gửi lên (nếu có)

    @Column(name = "status")
    private String status; // SUCCESS hoặc FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}