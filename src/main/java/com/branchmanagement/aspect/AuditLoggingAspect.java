package com.branchmanagement.aspect;

import com.branchmanagement.annotation.AuditAction;
import com.branchmanagement.entity.SystemAuditLog;
import com.branchmanagement.repository.SystemAuditLogRepository;
import com.branchmanagement.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditLoggingAspect.class);
    private final SystemAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLoggingAspect(SystemAuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditAction)")
    public Object logAuditActivity(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
        SystemAuditLog auditLog = new SystemAuditLog();
        auditLog.setAction(auditAction.action());
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        auditLog.setMethodSignature(signature.getDeclaringTypeName() + "." + signature.getName());

        // 1. Lấy thông tin User từ JWT (SecurityContext)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            auditLog.setUserId(userDetails.getUserId());
            auditLog.setUsername(userDetails.getUsername());
        } else {
            auditLog.setUsername("SYSTEM_OR_ANONYMOUS");
        }

        // 2. Chụp lại request params (chỉ chụp object đầu tiên để tránh quá tải)
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            try {
                auditLog.setRequestData(objectMapper.writeValueAsString(args[0]));
            } catch (Exception e) {
                auditLog.setRequestData("Cannot serialize request body");
            }
        }

        // 3. Thực thi phương thức gốc
        Object result;
        try {
            result = joinPoint.proceed(); // Cho phép hàm chạy
            auditLog.setStatus("SUCCESS");
        } catch (Throwable ex) {
            auditLog.setStatus("FAILED: " + ex.getMessage());
            throw ex; // Vẫn ném lỗi ra ngoài để GlobalExceptionHandler bắt
        } finally {
            // 4. Lưu log vào DB bất kể thành công hay thất bại
            try {
                auditLogRepository.save(auditLog);
            } catch (Exception e) {
                logger.error("Lỗi khi ghi Audit Log: {}", e.getMessage());
            }
        }

        return result;
    }
}