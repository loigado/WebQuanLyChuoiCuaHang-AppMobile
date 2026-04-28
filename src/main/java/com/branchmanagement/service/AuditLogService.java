package com.branchmanagement.service;

import com.branchmanagement.entity.AuditLog;
import com.branchmanagement.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Dùng Propagation.REQUIRES_NEW để đảm bảo Log luôn được lưu lại 
    // kể cả khi thao tác chính bị lỗi (rollback)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String description, String performedBy) {
        AuditLog log = new AuditLog(action, description, performedBy);
        auditLogRepository.save(log);
    }
}