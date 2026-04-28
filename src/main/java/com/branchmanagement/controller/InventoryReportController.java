package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.StockMovementDto;
import com.branchmanagement.dto.WorkflowStatsDto;
import com.branchmanagement.service.impl.InventoryReportServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventory-reports")
public class InventoryReportController {

    private final InventoryReportServiceImpl reportService;

    public InventoryReportController(InventoryReportServiceImpl reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/workflow-stats")
    public ResponseEntity<ApiResponse<WorkflowStatsDto>> getWorkflowStats() {
        return ResponseEntity.ok(ApiResponse.ok("Thành công", reportService.getWorkflowStats()));
    }

    @GetMapping("/stock-movement")
    public ResponseEntity<ApiResponse<List<StockMovementDto>>> getStockMovement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.ok("Thành công", reportService.getStockMovementReport(start, end)));
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() throws IOException {
        ByteArrayInputStream stream = reportService.exportCurrentInventoryExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=BaoCao_TonKho.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }
}