package com.branchmanagement.service;

import java.time.LocalDate;
import java.util.List;

import com.branchmanagement.dto.NearbyStockResponse;
import com.branchmanagement.dto.StockDetailResponse;
import com.branchmanagement.dto.StockFinancialSummaryResponse;
import com.branchmanagement.dto.StockQuantityByBranchResponse;
import com.branchmanagement.dto.StockTransactionResponse;
import com.branchmanagement.security.CustomUserDetails;

public interface StockViewService {

	// ===== Báo cáo tài chính =====
	StockFinancialSummaryResponse getFinancialSummary(Integer branchId, LocalDate fromDate, LocalDate toDate);

	// ===== Tồn kho theo chi nhánh =====
	List<StockQuantityByBranchResponse> getStockQuantityByBranch();

	// ===== Ma trận điều chuyển =====
	List<Object[]> getTransferMatrixSimple();

	// ===== Module 3.1 cũ =====
	List<StockTransactionResponse> getAllTransactions(Integer branchId, Integer productId, String transactionType, String status,
			LocalDate fromDate, LocalDate toDate);

	List<StockTransactionResponse> getAllImportTransactions(Integer branchId, String status, LocalDate fromDate,
			LocalDate toDate);

	List<StockTransactionResponse> getAllExportTransactions(Integer branchId, String status, LocalDate fromDate,
			LocalDate toDate);

	List<StockTransactionResponse> getAllTransferTransactions(Integer fromBranchId, Integer toBranchId, String status,
			LocalDate fromDate, LocalDate toDate);

    // ==========================================
    // ✅ THÊM MỚI: Tính năng 2.4.3 Xem tồn kho
    // ==========================================
    List<StockDetailResponse> getAdvancedStockReport(Integer branchId, Integer categoryId);
    List<NearbyStockResponse> findNearbyStock(Integer productId, CustomUserDetails user);

    List<Object[]> getStocktakeDiscrepancyReport(Integer stocktakeId);
}