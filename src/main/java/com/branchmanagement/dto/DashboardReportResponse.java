package com.branchmanagement.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardReportResponse {
    private long totalAttendance;
    private long abnormalCount; // Đi muộn/Về sớm
    private long lowStockCount; // Tồn kho dưới ngưỡng
    private Map<String, Long> inventoryWorkflowStatus; // Trạng thái phiếu kho
    private List<BranchComparisonDTO> branchComparison; // Danh sách so sánh chi nhánh
}