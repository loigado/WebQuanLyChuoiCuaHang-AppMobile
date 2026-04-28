package com.branchmanagement.repository;

import com.branchmanagement.entity.InventoryRequest;
import org.springframework.data.repository.query.Param; // ✅ ĐÃ SỬA IMPORT CHUẨN CỦA SPRING DATA
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRequestRepository extends JpaRepository<InventoryRequest, Integer> {
    
    // Admin lấy danh sách các phiếu đang chờ duyệt
    Page<InventoryRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    // LẤY LỊCH SỬ PHIẾU ĐÃ XỬ LÝ
    Page<InventoryRequest> findByStatusInOrderByCreatedAtDesc(List<String> statuses, Pageable pageable);
    
    long countByStatusIgnoreCase(String status);
    
    long countByBranch_BranchIdAndStatusIgnoreCase(Integer branchId, String status);
    
    // ✅ Câu Query lọc theo Chi nhánh và Ngày tháng
    @Query("SELECT r FROM InventoryRequest r WHERE " +
           "(:branchId IS NULL OR r.branch.branchId = :branchId) AND " +
           "(:startDate IS NULL OR r.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR r.createdAt <= :endDate) AND " +
           "r.status IN ('approved', 'rejected') " +
           "ORDER BY r.createdAt DESC")
    Page<InventoryRequest> findHistoryWithFilters(
            @Param("branchId") Integer branchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}