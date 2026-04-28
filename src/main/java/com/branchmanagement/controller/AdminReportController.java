package com.branchmanagement.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.branchmanagement.dto.InventoryValueDto;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.repository.StockTransactionRepository;
import com.branchmanagement.service.AdminReportService;
import com.branchmanagement.service.impl.InventoryReportServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

	private final AdminReportService reportService;
	private final StockTransactionRepository transactionRepository;
	private final StockRepository stockRepository; 
	private final InventoryReportServiceImpl inventoryReportService; 

	// 1. Summary (Cũ của bạn)
	@GetMapping("/summary")
	public Map<String, Object> getSummary() {
		return reportService.getSystemSummary();
	}

	// 2. Thống kê workflow phiếu kho (Cũ của bạn)
	@GetMapping("/workflow-stats")
	public Map<String, Object> getWorkflowStats() {
		Map<String, Object> result = new HashMap<>();

		List<Object[]> byStatus = transactionRepository.countByStatusGrouped();
		Map<String, Long> statusMap = new HashMap<>();
		for (Object[] row : byStatus) {
			statusMap.put((String) row[0], ((Number) row[1]).longValue());
		}
		result.put("byStatus", statusMap);

		List<Object[]> byType = transactionRepository.countByTypeAndStatus();
		Map<String, Map<String, Long>> typeMap = new HashMap<>();
		for (Object[] row : byType) {
			String type = (String) row[0];
			String status = (String) row[1];
			long count = ((Number) row[2]).longValue();
			typeMap.computeIfAbsent(type, k -> new HashMap<>()).put(status, count);
		}
		result.put("byTypeAndStatus", typeMap);

		long approved = statusMap.getOrDefault("approved", 0L);
		long rejected = statusMap.getOrDefault("rejected", 0L);
		long total = approved + rejected;
		result.put("approvalRate", total > 0 ? Math.round((double) approved / total * 100) : 0);
		result.put("rejectionRate", total > 0 ? Math.round((double) rejected / total * 100) : 0);

		return result;
	}

	// 3. Báo cáo giá trị tồn kho theo danh mục (JSON cho Dashboard)
	@GetMapping("/inventory-value")
	public ResponseEntity<List<InventoryValueDto>> getInventoryValue() {
		return ResponseEntity.ok(stockRepository.getInventoryValueByCategory());
	}

	// 4. API Xuất Báo Cáo Excel Tổng Hợp (Tồn kho + Biến động)
	@GetMapping("/inventory/excel")
	public ResponseEntity<Resource> exportInventoryExcel(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) throws IOException {
		
		// Mặc định 30 ngày gần nhất nếu không truyền
		if (start == null) start = LocalDateTime.now().minusDays(30);
		if (end == null) end = LocalDateTime.now();

		String filename = "Bao-cao-tong-hop-kho.xlsx";
		InputStreamResource file = new InputStreamResource(inventoryReportService.exportComprehensiveReport(start, end));

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	// ✅ PHẦN BỊ THIẾU: Báo cáo biến động kho (Tồn đầu - Nhập - Xuất - Tồn cuối)
	@GetMapping("/movement")
	public ResponseEntity<?> getMovementReport(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
		return ResponseEntity.ok(inventoryReportService.getStockMovementReport(start, end));
	}
}