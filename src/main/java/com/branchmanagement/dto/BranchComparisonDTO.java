package com.branchmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchComparisonDTO {
    private String branchName;
    private long totalAttendance;
    private long onTimeCount;
    private double efficiencyRate; // Tính bằng công thức (onTime / total) * 100
}