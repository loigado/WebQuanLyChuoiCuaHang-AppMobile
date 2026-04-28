package com.branchmanagement.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportDto {
    private String employeeCode;
    private String fullName;
    private long totalPresent;     // Tổng số ngày đi làm đúng giờ
    private long totalLate;        // Tổng số ngày đi muộn
    private long totalEarlyLeave;  // Tổng số ngày về sớm
    private BigDecimal totalWorkingHours; // Tổng số giờ làm việc
}