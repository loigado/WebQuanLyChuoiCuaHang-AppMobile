package com.branchmanagement.repository;

import com.branchmanagement.entity.SystemAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Long> {
    
    // Câu Query lọc theo ngày tháng linh hoạt (xử lý cả trường hợp không chọn ngày)
    @Query("SELECT s FROM SystemAuditLog s WHERE " +
           "(:startDate IS NULL OR s.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR s.createdAt <= :endDate)")
    Page<SystemAuditLog> findLogsWithDateFilter(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}