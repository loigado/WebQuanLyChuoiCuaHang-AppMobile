package com.branchmanagement.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.branchmanagement.dto.InventoryValueDto;
import com.branchmanagement.dto.StockQuantityByBranchResponse;
import com.branchmanagement.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {

	Optional<Stock> findByProduct_ProductIdAndBranch_BranchId(Integer productId, Integer branchId);

	// Tổng qty theo chi nhánh (giữ nguyên)
	@Query("""
			    SELECT new com.branchmanagement.dto.StockQuantityByBranchResponse(
			        s.branch.branchId,
			        s.branch.branchName,
			        SUM(s.quantity)
			    )
			    FROM Stock s
			    GROUP BY s.branch.branchId, s.branch.branchName
			""")
	List<StockQuantityByBranchResponse> getTotalQuantityByBranch();

	// Chi tiết từng sản phẩm theo chi nhánh (Báo cáo cũ)
	@Query("""
			    SELECT
			        s.branch.branchId,
			        s.branch.branchName,
			        s.product.productId,
			        s.product.productCode,
			        s.product.productName,
			        s.product.unit,
			        s.quantity,
			        s.reservedQuantity
			    FROM Stock s
			    WHERE (:branchId IS NULL OR s.branch.branchId = :branchId)
			      AND s.quantity > 0
			    ORDER BY s.branch.branchName, s.product.productName
			""")
	List<Object[]> getStockDetail(@Param("branchId") Integer branchId);

	// Tổng giá trị tồn kho theo chi nhánh (join với StockTransaction để lấy unit_price)
	@Query("""
			    SELECT
			        s.branch.branchId,
			        s.branch.branchName,
			        SUM(s.quantity),
			        SUM(s.reservedQuantity)
			    FROM Stock s
			    WHERE (:branchId IS NULL OR s.branch.branchId = :branchId)
			    GROUP BY s.branch.branchId, s.branch.branchName
			    ORDER BY s.branch.branchName
			""")
	List<Object[]> getStockSummaryByBranch(@Param("branchId") Integer branchId);

    // =================================================================
    // ✅ THÊM MỚI CHO TÍNH NĂNG 2.4.3: Xem tồn kho nâng cấp
    // =================================================================
	@Query("SELECT s FROM Stock s " +
		       "JOIN FETCH s.product p " +
		       "LEFT JOIN FETCH p.category c " + 
		       "JOIN FETCH s.branch b " +
		       "WHERE (:branchId IS NULL OR b.branchId = :branchId) " +
		       "AND (:categoryId IS NULL OR c.categoryId = :categoryId)")
		List<Stock> findDetailedStock(@Param("branchId") Integer branchId, 
		                             @Param("categoryId") Integer categoryId);
    
    @Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.product.productId = :productId")
    BigDecimal getTotalQuantityByProduct(@Param("productId") Integer productId);
    
    @Query("SELECT new com.branchmanagement.dto.InventoryValueDto(" +
    	       "c.categoryName, SUM(s.quantity), SUM(s.quantity * p.price)) " +
    	       "FROM Stock s JOIN s.product p JOIN p.category c " +
    	       "GROUP BY c.categoryName")
    	List<InventoryValueDto> getInventoryValueByCategory();
    
 // Thêm vào StockRepository.java
    @Query("SELECT s FROM Stock s WHERE s.quantity <= :threshold")
    List<Stock> findStocksBelowThreshold(@Param("threshold") java.math.BigDecimal threshold);
    
 // Lấy giá trị tồn kho nhưng CHỈ CỦA 1 CHI NHÁNH
    @Query("SELECT new com.branchmanagement.dto.InventoryValueDto(" +
           "c.categoryName, SUM(s.quantity), SUM(s.quantity * p.price)) " +
           "FROM Stock s JOIN s.product p JOIN p.category c " +
           "WHERE s.branch.branchId = :branchId " +
           "GROUP BY c.categoryName")
    List<com.branchmanagement.dto.InventoryValueDto> getInventoryValueByBranch(@Param("branchId") Integer branchId);
    
    @Query("SELECT s FROM Stock s WHERE s.product.productId = :productId AND s.branch.branchId != :currentBranchId AND (s.quantity - COALESCE(s.reservedQuantity, 0)) > 0")
    List<Stock> findAvailableStockInOtherBranches(@Param("productId") Integer productId, @Param("currentBranchId") Integer currentBranchId);

    @Query("SELECT p.productId, p.productCode, p.productName, c.categoryName, " +
            "SUM(s.quantity), SUM(s.reservedQuantity), p.unit, p.minStock, p.maxStock " +
            "FROM Stock s JOIN s.product p LEFT JOIN p.category c " +
            "WHERE (:categoryId IS NULL OR c.categoryId = :categoryId) " +
            "AND (:branchId IS NULL OR s.branch.branchId = :branchId) " +
            "GROUP BY p.productId, p.productCode, p.productName, c.categoryName, p.unit, p.minStock, p.maxStock")
     List<Object[]> getAdvancedGroupedStock(@Param("categoryId") Integer categoryId, @Param("branchId") Integer branchId);

  // Thêm hàm này vào trong interface StockRepository
     @Query("SELECT s.branch.branchName, s.product.productName, s.quantity, s.product.price " +
            "FROM Stock s " +
            "WHERE (:branchId IS NULL OR s.branch.branchId = :branchId) " +
            "AND s.product.status = 'active'")
     List<Object[]> getAggregatedInventoryData(@Param("branchId") Integer branchId);

     @Query("SELECT COUNT(s) FROM Stock s WHERE s.branch.branchId = :branchId " +
             "AND s.product.status = 'active' AND s.quantity <= s.product.minStock")
      long countLowStockByBranch(@Param("branchId") Integer branchId);

     @Query("SELECT st.createdAt, st.transactionType, SUM(std.quantity * std.unitPrice), st.note " +
    	       "FROM StockTransaction st " +
    	       "JOIN st.details std " + 
    	       "WHERE (:startDate IS NULL OR st.createdAt >= :startDate) " +
    	       "AND (:endDate IS NULL OR st.createdAt <= :endDate) " +
    	       "GROUP BY st.transactionId, st.createdAt, st.transactionType, st.note " +
    	       "ORDER BY st.createdAt DESC")
    	List<Object[]> getRecentTransactions(@Param("startDate") java.time.LocalDateTime startDate, 
    	                                     @Param("endDate") java.time.LocalDateTime endDate);

    	// Hàm tính tổng giá trị kho hàng (vẫn giữ nguyên vì bảng Stock có product)
    	@Query("SELECT SUM(s.quantity * s.product.price) FROM Stock s")
    	java.math.BigDecimal getTotalSystemInventoryValue();

    	@Query("SELECT s FROM Stock s WHERE s.branch.branchId = :branchId")
        List<Stock> findByBranch_BranchId(@Param("branchId") Integer branchId);
}