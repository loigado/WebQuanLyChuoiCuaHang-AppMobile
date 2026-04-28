package com.branchmanagement.service;

import com.branchmanagement.dto.CreateStockRequestDto;
import com.branchmanagement.dto.StockTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockRequestService {
    Page<StockTransactionResponse> getAllRequests(Integer productId, Integer branchId, String status, 
                                                  java.time.LocalDateTime startDate, 
                                                  java.time.LocalDateTime endDate, 
                                                  Pageable pageable);

    StockTransactionResponse createStockRequest(CreateStockRequestDto request, Integer creatorId);
    
    void approveOrRejectRequest(Integer transactionId, String newStatus, String note, Integer approverId);
}