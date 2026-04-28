package com.branchmanagement.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.branchmanagement.entity.StockTransaction;

@Repository
public interface StockTransactionRepository
        extends JpaRepository<StockTransaction, Integer>, JpaSpecificationExecutor<StockTransaction> {

    // ✅ HÀM QUAN TRỌNG NHẤT: Lọc phiếu theo Sản phẩm và Chi nhánh có phân trang
    // Giải quyết lỗi "No Data" khi người dùng bấm xem lịch sử của 1 món cụ thể
    @Query("SELECT DISTINCT t FROM StockTransaction t LEFT JOIN t.details d " +
           "WHERE (:productId IS NULL OR d.product.productId = :productId) " +
           "AND (:branchId IS NULL OR t.fromBranch.branchId = :branchId OR t.toBranch.branchId = :branchId) " +
           "AND (:status IS NULL OR UPPER(t.status) = UPPER(:status)) " +
           "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR t.createdAt <= :endDate)")
    Page<StockTransaction> findAllWithFilters(
            @Param("productId") Integer productId, 
            @Param("branchId") Integer branchId, 
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    Page<StockTransaction> findByStatus(String status, Pageable pageable);

    Page<StockTransaction> findByStatusIn(List<String> statuses, Pageable pageable);

    List<StockTransaction> findByStatusOrderByCreatedAtDesc(String status);

    List<StockTransaction> findByStatusAndTransactionTypeOrderByCreatedAtDesc(String status, String transactionType);

    List<StockTransaction> findByCreatedBy_UserIdOrderByCreatedAtDesc(Integer userId);
    
    Optional<StockTransaction> findByTransferRequest_RequestIdAndTransactionType(Integer requestId, String type);
    
    long countByToBranch_BranchIdAndStatusIgnoreCase(Integer branchId, String status);

    // ==========================================
    // BÁO CÁO & THỐNG KÊ (ĐÃ SỬA CHUẨN MASTER-DETAIL)
    // ==========================================

    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN t.transactionType = 'import' AND d.unitPrice IS NOT NULL THEN d.quantity * d.unitPrice ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN t.transactionType = 'export' AND d.unitPrice IS NOT NULL THEN d.quantity * d.unitPrice ELSE 0 END), 0) " +
           "FROM StockTransaction t JOIN t.details d " + 
           "WHERE t.status = 'approved' " +
           "AND (:branchId IS NULL OR t.fromBranch.branchId = :branchId OR t.toBranch.branchId = :branchId) " +
           "AND (:fromDate IS NULL OR t.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR t.createdAt <= :toDate)")
    List<Object[]> getFinancialSummary(@Param("branchId") Integer branchId, 
                                       @Param("fromDate") java.time.LocalDateTime fromDate, 
                                       @Param("toDate") java.time.LocalDateTime toDate);

    @Query("SELECT t.fromBranch.branchName, t.toBranch.branchName, SUM(d.quantity) " +
           "FROM StockTransaction t JOIN t.details d " + 
           "WHERE t.transactionType = 'transfer_out' AND t.status = 'approved' " +
           "GROUP BY t.fromBranch.branchName, t.toBranch.branchName")
    List<Object[]> getTransferMatrixSimple();

    long countByStatus(String status);
    
    long countByTransactionType(String transactionType);

    // Đếm phiếu xuất kho thông thường (Loại trừ mọi trường hợp hao hụt)
    @Query("SELECT COUNT(t) FROM StockTransaction t WHERE t.transactionType = 'export' " +
           "AND (t.reason IS NULL OR (LOWER(t.reason) NOT LIKE '%hư%' AND LOWER(t.reason) NOT LIKE '%hỏng%' AND LOWER(t.reason) NOT LIKE '%mất%' AND LOWER(t.reason) NOT LIKE '%vỡ%' AND LOWER(t.reason) NOT LIKE '%thiếu%' " +
           "AND LOWER(t.reason) NOT LIKE '%hu%' AND LOWER(t.reason) NOT LIKE '%hong%' AND LOWER(t.reason) NOT LIKE '%mat%' AND LOWER(t.reason) NOT LIKE '%vo%' AND LOWER(t.reason) NOT LIKE '%thieu%')) " +
           "AND (t.note IS NULL OR (LOWER(t.note) NOT LIKE '%hư%' AND LOWER(t.note) NOT LIKE '%hỏng%' AND LOWER(t.note) NOT LIKE '%mất%' AND LOWER(t.note) NOT LIKE '%vỡ%' AND LOWER(t.note) NOT LIKE '%thiếu%' " +
           "AND LOWER(t.note) NOT LIKE '%hu%' AND LOWER(t.note) NOT LIKE '%hong%' AND LOWER(t.note) NOT LIKE '%mat%' AND LOWER(t.note) NOT LIKE '%vo%' AND LOWER(t.note) NOT LIKE '%thieu%'))")
    long countNormalExports();

    // Đếm phiếu hao hụt (Gồm Adjustment + Export hư/hỏng/mất/vỡ/thiếu - Cả có dấu và không dấu)
    @Query("SELECT COUNT(t) FROM StockTransaction t WHERE t.transactionType = 'adjustment' " +
           "OR (t.transactionType = 'export' AND (" +
           "LOWER(t.reason) LIKE '%hư%' OR LOWER(t.reason) LIKE '%hỏng%' OR LOWER(t.reason) LIKE '%mất%' OR LOWER(t.reason) LIKE '%vỡ%' OR LOWER(t.reason) LIKE '%thiếu%' OR " +
           "LOWER(t.reason) LIKE '%hu%' OR LOWER(t.reason) LIKE '%hong%' OR LOWER(t.reason) LIKE '%mat%' OR LOWER(t.reason) LIKE '%vo%' OR LOWER(t.reason) LIKE '%thieu%' OR " +
           "LOWER(t.note) LIKE '%hư%' OR LOWER(t.note) LIKE '%hỏng%' OR LOWER(t.note) LIKE '%mất%' OR LOWER(t.note) LIKE '%vỡ%' OR LOWER(t.note) LIKE '%thiếu%' OR " +
           "LOWER(t.note) LIKE '%hu%' OR LOWER(t.note) LIKE '%hong%' OR LOWER(t.note) LIKE '%mat%' OR LOWER(t.note) LIKE '%vo%' OR LOWER(t.note) LIKE '%thieu%'))")
    long countAdjustmentsAndLosses();

    @Query("SELECT t.status, COUNT(t) FROM StockTransaction t GROUP BY t.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT t.transactionType, t.status, COUNT(t) FROM StockTransaction t GROUP BY t.transactionType, t.status")
    List<Object[]> countByTypeAndStatus();
    
    // Tính thời gian xử lý trung bình
    @Query(value = "SELECT AVG(CAST(DATEDIFF(MINUTE, created_at, approved_at) AS FLOAT)) / 60.0 " +
                   "FROM stock_transaction WHERE status = 'approved'", nativeQuery = true)
    Double getAverageProcessingTimeInHours();
    
    @Query("SELECT SUM(d.quantity) FROM StockTransaction t JOIN t.details d " +
           "WHERE d.product.productId = :productId " +
           "AND t.transactionType = :type " +
           "AND t.status = 'approved' " +
           "AND t.createdAt >= :start AND t.createdAt <= :end")
    BigDecimal sumQuantityByTypeAndDate(@Param("productId") Integer productId, 
                                        @Param("type") String type, 
                                        @Param("start") LocalDateTime start, 
                                        @Param("end") LocalDateTime end);

    @Query("SELECT SUM(d.quantity) FROM StockTransaction t JOIN t.details d " +
           "WHERE d.product.productId = :productId " +
           "AND t.status = 'approved' " +
           "AND t.createdAt > :end")
    BigDecimal sumNetChangeAfter(@Param("productId") Integer productId, 
                                 @Param("end") LocalDateTime end);

    @Query("SELECT st FROM StockTransaction st WHERE st.status = 'pending' " +
            "AND (st.fromBranch.branchId = :branchId OR st.toBranch.branchId = :branchId) " +
            "ORDER BY st.createdAt DESC")
    List<StockTransaction> findPendingByBranch(@Param("branchId") Integer branchId);

    @Query("SELECT st FROM StockTransaction st WHERE LOWER(st.status) = 'pending' ORDER BY st.createdAt DESC")
    List<StockTransaction> findPendingTransactions();
    List<StockTransaction> findByStatusIgnoreCase(String status);
    
    @Query("SELECT st FROM StockTransaction st WHERE st.status IN ('approved', 'rejected') " +
            "AND (:startDate IS NULL OR st.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR st.createdAt <= :endDate) " +
            "ORDER BY st.createdAt DESC")
     Page<StockTransaction> findApprovalHistoryWithDate(
             @Param("startDate") LocalDateTime startDate,
             @Param("endDate") LocalDateTime endDate,
             Pageable pageable);
}