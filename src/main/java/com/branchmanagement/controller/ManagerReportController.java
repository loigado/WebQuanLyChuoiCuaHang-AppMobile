package com.branchmanagement.controller;

import com.branchmanagement.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.service.ManagerReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/manager/reports")
public class ManagerReportController {

    private final ManagerReportService managerReportService;

    public ManagerReportController(ManagerReportService managerReportService) {
        this.managerReportService = managerReportService;
    }

    // ============================================================
    // 1. API TRẢ VỀ JSON CHO DASHBOARD (Sửa lỗi 404 & 500)
    // ============================================================

    /**
     * UC 2.6: Lấy dữ liệu tóm tắt (Thẻ Card)
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Lấy branchId từ user đang đăng nhập
        Integer branchId = userDetails.getBranchId();
        Map<String, Object> data = managerReportService.getBranchSummary(branchId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy tóm tắt thành công", data));
    }

    /**
     * UC 2.7.1: Báo cáo chấm công (JSON)
     */
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAttendanceReport(
            @RequestParam(required = false) String month,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer branchId = userDetails.getBranchId();
        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;
        
        if (month != null && month.contains("-")) {
            try {
                String[] parts = month.split("-");
                int year = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                start = java.time.LocalDateTime.of(year, m, 1, 0, 0, 0);
                java.time.LocalDate lastDay = java.time.LocalDate.of(year, m, 1).with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
                end = lastDay.atTime(23, 59, 59, 999999999);
            } catch (Exception e) { }
        }
        
        List<Map<String, Object>> data = managerReportService.getAttendanceReportData(branchId, start, end);
        return ResponseEntity.ok(ApiResponse.ok("Lấy báo cáo chấm công thành công", data));
    }

    /**
     * UC 2.4.8: Báo cáo tồn kho NXT (JSON)
     */
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInventoryReport(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Trả về dữ liệu Nhập - Xuất - Tồn cho chi nhánh cụ thể
        List<Map<String, Object>> data = managerReportService.getInventoryReportData(userDetails.getBranchId());
        return ResponseEntity.ok(ApiResponse.ok("Lấy báo cáo tồn kho thành công", data));
    }

    // ============================================================
    // 2. API XUẤT FILE EXCEL (Giữ nguyên logic của bạn)
    // ============================================================

    @GetMapping("/branch/{branchId}/inventory/excel")
    public ResponseEntity<Resource> exportInventoryExcel(@PathVariable Integer branchId) throws IOException {
        InputStreamResource file = new InputStreamResource(managerReportService.exportBranchInventoryExcel(branchId));
        String filename = "Ton-kho-chi-nhanh-" + branchId + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/branch/{branchId}/attendance/excel")
    public ResponseEntity<Resource> exportAttendanceExcel(
            @PathVariable Integer branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        InputStreamResource file = new InputStreamResource(
                managerReportService.exportBranchAttendanceExcel(branchId, startDate, endDate));
        String filename = "Cham-cong-chi-nhanh-" + branchId + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}