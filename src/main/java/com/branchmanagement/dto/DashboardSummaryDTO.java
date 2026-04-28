package com.branchmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardSummaryDTO {
    private long totalBranches;
    private long totalUsers;
    private long activeBranches;
    private long totalInventory; // Tạm thời để 0 nếu chưa làm phần kho
}