package com.branchmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public class RejectRequest {
    
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;

    public RejectRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}