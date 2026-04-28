package com.branchmanagement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LeaveRequestResponse {
    private Integer leaveId;
    private Integer userId;
    private String fullName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
}