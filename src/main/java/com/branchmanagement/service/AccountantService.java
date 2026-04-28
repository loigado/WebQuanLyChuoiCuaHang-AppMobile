package com.branchmanagement.service;

import com.branchmanagement.repository.AttendanceRepository;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.repository.StockTransactionRepository;
import com.branchmanagement.entity.StockTransaction;
import com.branchmanagement.entity.StockTransactionDetail;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class AccountantService {

    private final StockRepository stockRepository; 
    private final AttendanceRepository attendanceRepository;
    private final StockTransactionRepository transactionRepository;

    public AccountantService(StockRepository stockRepository, 
                             AttendanceRepository attendanceRepository,
                             StockTransactionRepository transactionRepository) {
        this.stockRepository = stockRepository;
        this.attendanceRepository = attendanceRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Map<String, Object>> getSystemInventory(Integer branchId) {
        List<Object[]> results = stockRepository.getAggregatedInventoryData(branchId);
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("branchName", row[0]);
            map.put("productName", row[1]);
            map.put("quantity", row[2]);
            map.put("unitPrice", row[3]);
            dataList.add(map);
        }
        return dataList;
    }

    public List<Map<String, Object>> getSystemAttendance(Integer branchId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        // Đảm bảo không bao giờ truyền null vào Repository (tránh lỗi BETWEEN)
        java.time.LocalDateTime actualStart = startDate;
        java.time.LocalDateTime actualEnd = endDate;
        
        if (actualStart == null || actualEnd == null) {
            actualStart = java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            actualEnd = java.time.LocalDateTime.now().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        }
        
        List<Object[]> results = attendanceRepository.getAggregatedAttendanceData(branchId, actualStart, actualEnd);
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("branchName", row[0]);
            map.put("employeeCode", row[1]);
            map.put("fullName", row[2]);
            map.put("totalShifts", row[3]);
            
            double hours = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            map.put("totalHours", hours);
            map.put("abnormalDays", row[5] != null ? row[5] : 0);
            
            java.math.BigDecimal rate = row[6] != null ? (java.math.BigDecimal) row[6] : java.math.BigDecimal.ZERO;
            map.put("hourlyRate", rate);
            
            // Tính lương: giờ * lương/giờ
            java.math.BigDecimal salary = rate.multiply(java.math.BigDecimal.valueOf(hours));
            map.put("totalSalary", salary);
            
            dataList.add(map);
        }
        return dataList;
    }
    
    public Map<String, Object> getFinancialSummary(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();
        
        // 1. Lấy tổng giá trị tồn kho thật
        summary.put("totalInventoryValue", stockRepository.getTotalSystemInventoryValue());
        
        // 2. Lấy danh sách giao dịch có lọc theo ngày (Dùng StockTransactionRepository cho chuẩn)
        List<StockTransaction> transactions = transactionRepository.findAllWithFilters(
                null, null, "APPROVED", startDate, endDate, 
                org.springframework.data.domain.PageRequest.of(0, 100)).getContent();
        
        List<Map<String, Object>> transactionList = new ArrayList<>();
        
        for (StockTransaction st : transactions) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", st.getCreatedAt()); 
            map.put("type", st.getTransactionType());
            
            // Tính tổng giá trị từ details (Dùng .multiply cho BigDecimal)
            java.math.BigDecimal totalAmount = st.getDetails().stream()
                    .map(d -> {
                        java.math.BigDecimal qty = d.getQuantity() != null ? d.getQuantity() : java.math.BigDecimal.ZERO;
                        java.math.BigDecimal price = d.getUnitPrice() != null ? d.getUnitPrice() : java.math.BigDecimal.ZERO;
                        return qty.multiply(price);
                    })
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            map.put("amount", totalAmount); 
            map.put("note", st.getNote()); 
            transactionList.add(map);
        }
        summary.put("recentTransactions", transactionList);
        
        return summary;
    }

    public List<Map<String, Object>> getAttendanceDetails(String username, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        List<com.branchmanagement.entity.Attendance> results = attendanceRepository.findDetailsByUserAndPeriod(username, startDate, endDate);
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (com.branchmanagement.entity.Attendance a : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("attendanceId", a.getAttendanceId());
            map.put("checkIn", a.getCheckIn());
            map.put("checkOut", a.getCheckOut());
            map.put("status", a.getStatus());
            
            double hours = 0.0;
            if (a.getTotalHours() != null) {
                hours = ((Number) a.getTotalHours()).doubleValue();
            }
            map.put("totalHours", hours);
            
            // Lấy lương từ User (đã được fetch qua query hoặc transactional)
            java.math.BigDecimal rate = java.math.BigDecimal.ZERO;
            if (a.getUser() != null) {
                rate = a.getUser().getHourlyRate() != null ? a.getUser().getHourlyRate() : java.math.BigDecimal.ZERO;
            }
            map.put("hourlyRate", rate);
            
            // Tính lương cho ca này
            java.math.BigDecimal salary = rate.multiply(java.math.BigDecimal.valueOf(hours));
            map.put("salary", salary);
            
            dataList.add(map);
        }
        return dataList;
    }
}