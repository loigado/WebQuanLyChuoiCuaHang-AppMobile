package com.branchmanagement.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.AttendanceDetailSummaryResponse;
import com.branchmanagement.dto.AttendanceSummaryResponse;
import com.branchmanagement.dto.StockDetailResponse;
import com.branchmanagement.dto.StockFinancialSummaryResponse;
import com.branchmanagement.dto.StockQuantityByBranchResponse;
import com.branchmanagement.entity.Attendance;
import com.branchmanagement.repository.AttendanceRepository;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.service.AccountantReportService;
import com.branchmanagement.service.StockViewService;
import com.branchmanagement.service.SystemConfigService;

@RestController
@RequestMapping("/api/accountant/reports")
public class AccountantStockViewController {

	private final StockViewService stockViewService;
	private final AccountantReportService accountantReportService;
	private final StockRepository stockRepository;
	private final AttendanceRepository attendanceRepository;
    private final SystemConfigService systemConfigService;

	public AccountantStockViewController(StockViewService stockViewService,
			AccountantReportService accountantReportService, StockRepository stockRepository,
			AttendanceRepository attendanceRepository, SystemConfigService systemConfigService) {
		this.stockViewService = stockViewService;
		this.accountantReportService = accountantReportService;
		this.stockRepository = stockRepository;
		this.attendanceRepository = attendanceRepository;
        this.systemConfigService = systemConfigService;
	}

	@GetMapping("/financial")
	public ResponseEntity<ApiResponse<StockFinancialSummaryResponse>> getFinancialSummary(
			@RequestParam(required = false) Integer branchId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		try {
			StockFinancialSummaryResponse summary = stockViewService.getFinancialSummary(branchId, fromDate, toDate);
			return ResponseEntity.ok(ApiResponse.ok("Lấy báo cáo tài chính kho thành công", summary));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
		}
	}

	@GetMapping("/quantity-by-branch")
	public ResponseEntity<ApiResponse<List<StockQuantityByBranchResponse>>> getQuantityByBranch() {
		try {
			List<StockQuantityByBranchResponse> data = stockViewService.getStockQuantityByBranch();
			return ResponseEntity.ok(ApiResponse.ok("Lấy số lượng tồn kho theo chi nhánh thành công", data));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
		}
	}

	@GetMapping("/transfer-matrix")
	public ResponseEntity<ApiResponse<List<Object[]>>> getTransferMatrix() {
		try {
			List<Object[]> data = stockViewService.getTransferMatrixSimple();
			return ResponseEntity.ok(ApiResponse.ok("Lấy ma trận điều chuyển thành công", data));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
		}
	}

	@GetMapping("/attendance-summary")
	public ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> getAttendanceSummary(
			@RequestParam(required = false) Integer branchId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		try {
			List<AttendanceSummaryResponse> data = accountantReportService.getAttendanceSummary(branchId, fromDate, toDate);
			return ResponseEntity.ok(ApiResponse.ok("Lấy báo cáo chấm công thành công", data));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
		}
	}

	@GetMapping("/attendance-detail")
	public ResponseEntity<ApiResponse<List<AttendanceDetailSummaryResponse>>> getAttendanceDetail(
			@RequestParam(required = false) Integer branchId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

		try {
			LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
			LocalDateTime to = toDate != null ? toDate.atTime(23, 59, 59) : null;

            // Lấy cấu hình chuẩn từ Database
			int lateHourLimit = systemConfigService.getCheckinStart().getHour();
			int earlyHourLimit = systemConfigService.getCheckoutStart().getHour();

			List<Object[]> rawData = attendanceRepository.getAttendanceDetailSummary(branchId, from, to, lateHourLimit, earlyHourLimit);

			List<AttendanceDetailSummaryResponse> response = rawData.stream()
					.map(r -> new AttendanceDetailSummaryResponse((Integer) r[0], (String) r[1], (String) r[2],
							(java.math.BigDecimal) r[3], ((Number) r[4]).longValue(), ((Number) r[5]).longValue(),
							((Number) r[6]).longValue(), ((Number) r[7]).longValue()))
					.collect(Collectors.toList());

			return ResponseEntity.ok(ApiResponse.ok("Lấy chi tiết chấm công thành công", response));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
		}
	}

	@GetMapping("/export-excel")
	public ResponseEntity<byte[]> exportExcel() {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			createStockSheet(workbook);
			createDetailSheet(workbook);

			workbook.write(out);

			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", "attachment; filename=Accountant_Report.xlsx");
			headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

			return ResponseEntity.ok().headers(headers).body(out.toByteArray());

		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	private void createStockSheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("STOCK SUMMARY");
		List<StockQuantityByBranchResponse> data = stockRepository.getTotalQuantityByBranch();

		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue("Chi nhánh");
		header.createCell(1).setCellValue("Tổng số lượng tồn");

		int rowIdx = 1;
		for (StockQuantityByBranchResponse d : data) {
			Row row = sheet.createRow(rowIdx++);
			row.createCell(0).setCellValue(d.getBranchName());
			row.createCell(1).setCellValue(d.getTotalQuantity() != null ? d.getTotalQuantity().doubleValue() : 0);
		}
		for (int i = 0; i < 2; i++) sheet.autoSizeColumn(i);
	}

	private void createDetailSheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("ATTENDANCE DETAIL");
        
        // Cải tiến: Nên lấy theo tháng hiện tại để tránh OOM
        LocalDateTime firstDayOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastDayOfMonth = LocalDate.now().atTime(23, 59, 59);

		List<Attendance> attendances = attendanceRepository.findAll(); // Khuyến khích đổi thành findByCheckInBetween() trong tương lai

        // Lấy config từ DB thay vì fix 09:00
        LocalTime lateTimeLimit = systemConfigService.getCheckinStart();

		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue("User ID");
		header.createCell(1).setCellValue("Họ tên");
		header.createCell(2).setCellValue("Chi nhánh");
		header.createCell(3).setCellValue("Check In");
		header.createCell(4).setCellValue("Check Out");
		header.createCell(5).setCellValue("Tổng giờ");
		header.createCell(6).setCellValue("Đi trễ?");
		header.createCell(7).setCellValue("Trạng thái");

		int rowIdx = 1;
		for (Attendance a : attendances) {
			boolean isLate = a.getCheckIn() != null && a.getCheckIn().toLocalTime().isAfter(lateTimeLimit);
            
			Row row = sheet.createRow(rowIdx++);
			row.createCell(0).setCellValue(a.getUser().getUserId());
			row.createCell(1).setCellValue(a.getUser().getFullName());
			row.createCell(2).setCellValue(a.getBranch().getBranchName());
			row.createCell(3).setCellValue(a.getCheckIn() != null ? a.getCheckIn().toString() : "");
			row.createCell(4).setCellValue(a.getCheckOut() != null ? a.getCheckOut().toString() : "");
			row.createCell(5).setCellValue(a.getTotalHours() != null ? a.getTotalHours().doubleValue() : 0);
			row.createCell(6).setCellValue(isLate ? "Có" : "Không");
			row.createCell(7).setCellValue(a.getStatus());
		}
		for (int i = 0; i < 8; i++) sheet.autoSizeColumn(i);
	}
}