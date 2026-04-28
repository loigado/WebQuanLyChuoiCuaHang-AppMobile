package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.service.AccountantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accountant")
public class AccountantController {

    private final AccountantService accountantService;

    public AccountantController(AccountantService accountantService) {
        this.accountantService = accountantService;
    }

    // UC 3.1: Dữ liệu kho (Có thể truyền branchId để lọc)
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllInventory(
            @RequestParam(required = false) Integer branchId) {
        
        List<Map<String, Object>> data = accountantService.getSystemInventory(branchId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy dữ liệu kho thành công", data));
    }

    // UC 3.3: Dữ liệu chấm công (Có thể truyền branchId, month (YYYY-MM) để lọc)
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSystemAttendance(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) String month) {
        
        java.time.LocalDateTime startDate = null;
        java.time.LocalDateTime endDate = null;
        
        if (month != null && !month.isEmpty()) {
            try {
                // Xử lý cả định dạng YYYY-MM
                String[] parts = month.split("-");
                int year = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                
                startDate = java.time.LocalDateTime.of(year, m, 1, 0, 0, 0);
                // Lấy ngày cuối cùng của tháng
                java.time.LocalDate lastDay = java.time.LocalDate.of(year, m, 1).with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
                endDate = lastDay.atTime(23, 59, 59, 999999999);
            } catch (Exception e) {
                // Nếu lỗi định dạng, mặc định không lọc hoặc lọc tháng hiện tại
                System.err.println("Lỗi parse tháng: " + month);
            }
        }
        
        List<Map<String, Object>> data = accountantService.getSystemAttendance(branchId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Lấy dữ liệu chấm công thành công", data));
    }
    
    @GetMapping("/financial-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFinancialSummary(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        
        java.time.LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        java.time.LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        Map<String, Object> data = accountantService.getFinancialSummary(start, end);
        return ResponseEntity.ok(ApiResponse.ok("Lấy báo cáo tài chính thành công", data));
    }

    @GetMapping("/reports/attendance-details")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAttendanceDetails(
            @RequestParam String username,
            @RequestParam(required = false) String month) {
        
        java.time.LocalDateTime startDate = null;
        java.time.LocalDateTime endDate = null;
        
        if (month != null && !month.isEmpty()) {
            try {
                String[] parts = month.split("-");
                int year = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                startDate = java.time.LocalDateTime.of(year, m, 1, 0, 0, 0);
                java.time.LocalDate lastDay = java.time.LocalDate.of(year, m, 1).with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
                endDate = lastDay.atTime(23, 59, 59, 999999999);
            } catch (Exception e) {}
        }
        
        List<Map<String, Object>> data = accountantService.getAttendanceDetails(username, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Lấy chi tiết chấm công thành công", data));
    }
}