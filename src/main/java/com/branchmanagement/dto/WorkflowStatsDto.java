package com.branchmanagement.dto;

import lombok.Data;

@Data
public class WorkflowStatsDto {
    private long totalDraft;
    private long totalPending;
    private long totalApproved;
    private long totalRejected;
    private long totalCompleted;
    private double approvalRate; // Tỷ lệ duyệt (%)
    private double avgProcessingHours; // Thời gian xử lý trung bình (giờ)
}