package com.branchmanagement.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InventoryRequestResponse {
    private Integer requestId;
    private String branchName;
    private String fullName;
    private String productName;
    private String requestType;
    private Integer quantity;
    private String reason;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
}