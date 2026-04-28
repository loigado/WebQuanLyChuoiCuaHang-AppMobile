package com.branchmanagement.service;

import com.branchmanagement.dto.InventoryRequestResponse;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryRequestService {
    Page<InventoryRequestResponse> getPendingRequests(Pageable pageable);
    void approveRequest(Integer requestId);
    void rejectRequest(Integer requestId, String reason);
    Page<InventoryRequestResponse> getHistoryRequests(Pageable pageable);
    Page<InventoryRequestResponse> getHistoryRequests(Integer branchId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}