package com.branchmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.DashboardReportResponse;
import com.branchmanagement.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // API lấy dữ liệu JSON cho các biểu đồ ECharts
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardReportResponse>> getDashboardData() {
        DashboardReportResponse data = reportService.getDashboardStatistics();
        return ResponseEntity.ok(ApiResponse.ok("Lấy dữ liệu báo cáo thành công", data));
    }

    // API xuất file Excel báo cáo tổng hợp
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=HeThong_BaoCao_2026.xlsx");
        reportService.exportToExcel(response.getOutputStream());
    }
}