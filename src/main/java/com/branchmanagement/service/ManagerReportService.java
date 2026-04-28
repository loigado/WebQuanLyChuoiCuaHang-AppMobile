package com.branchmanagement.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.branchmanagement.dto.AttendanceReportDto;
import com.branchmanagement.dto.InventoryValueDto;
import com.branchmanagement.repository.AttendanceRepository;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
public class ManagerReportService {

    private final StockRepository stockRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ManagerReportService(StockRepository stockRepository,
                                AttendanceRepository attendanceRepository,
                                UserRepository userRepository,
                                ProductRepository productRepository) {
        this.stockRepository = stockRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * Lấy ID chi nhánh an toàn thông qua Reflection (Tránh lỗi Principal)
     */
    private Integer getCurrentBranchId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return (Integer) principal.getClass().getMethod("getBranchId").invoke(principal);
        } catch (Exception e) {
            return null; 
        }
    }

    // ============================================================
    // 1. DỮ LIỆU JSON CHO DASHBOARD (REAL DATA)
    // ============================================================

    public Map<String, Object> getBranchSummary(Integer branchId) {
        Map<String, Object> summary = new HashMap<>();
        
        if (branchId != null) {
            summary.put("totalStaff", userRepository.countByBranch_BranchId(branchId));
            summary.put("lowStockCount", stockRepository.countLowStockByBranch(branchId));
            summary.put("abnormalAttendance", attendanceRepository.countByBranchIdAndStatus(branchId, "abnormal"));
        }
        return summary;
    }

    public List<Map<String, Object>> getAttendanceReportData(Integer branchId, LocalDateTime start, LocalDateTime end) {
        if (branchId == null) return new ArrayList<>();
        
        // Đảm bảo không bao giờ truyền null vào Repository (tránh lỗi BETWEEN)
        LocalDateTime actualStart = start;
        LocalDateTime actualEnd = end;
        
        if (actualStart == null || actualEnd == null) {
            actualStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            actualEnd = LocalDateTime.now().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        }
        
        List<Object[]> results = attendanceRepository.getAggregatedAttendanceData(branchId, actualStart, actualEnd);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("username", row[1]); 
            map.put("fullName", row[2]);
            map.put("totalHours", row[4] != null ? row[4] : 0); 
            map.put("abnormalDays", row[5] != null ? row[5] : 0);
            dataList.add(map);
        }
        return dataList;
    }

    public List<Map<String, Object>> getInventoryReportData(Integer branchId) {
        if (branchId == null) return new ArrayList<>();

        // ✅ Lấy từ stockRepository để có thông tin chi nhánh
        List<Object[]> results = stockRepository.getAggregatedInventoryData(branchId);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("productName", row[1]);
            map.put("closingStock", row[2]);
            dataList.add(map);
        }
        return dataList;
    }

    // ============================================================
    // 2. XUẤT EXCEL (LOGIC CỦA BẠN)
    // ============================================================

    public ByteArrayInputStream exportBranchInventoryExcel(Integer branchId) throws IOException {
        List<InventoryValueDto> data = stockRepository.getInventoryValueByBranch(branchId);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Tồn kho chi nhánh");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Danh mục", "Tổng số lượng", "Tổng giá trị (VND)"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (InventoryValueDto item : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getCategoryName());
                row.createCell(1).setCellValue(item.getTotalQuantity().doubleValue());
                row.createCell(2).setCellValue(item.getTotalValue().doubleValue());
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportBranchAttendanceExcel(Integer branchId, LocalDate start, LocalDate end) throws IOException {
        LocalDateTime startDateTime = start.atStartOfDay(); 
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        List<AttendanceReportDto> data = attendanceRepository.getBranchAttendanceReport(branchId, startDateTime, endDateTime);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo cáo");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Mã NV", "Họ tên", "Giờ làm"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (AttendanceReportDto item : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getEmployeeCode());
                row.createCell(1).setCellValue(item.getFullName());
                row.createCell(2).setCellValue(item.getTotalWorkingHours() != null ? item.getTotalWorkingHours().doubleValue() : 0.0);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}