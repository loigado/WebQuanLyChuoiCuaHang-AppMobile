package com.branchmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobilePayrollResponse {
    private String month;           // Ví dụ: "04/2026"
    private double totalHours;      // Tổng số giờ làm trong tháng
    private BigDecimal hourlyRate;  // Mức lương theo giờ
    private BigDecimal estimatedSalary; // Tổng lương dự tính = hours * rate
    private int totalShifts;        // Tổng số ca đã hoàn thành
}
