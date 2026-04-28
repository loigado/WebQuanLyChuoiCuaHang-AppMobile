package com.branchmanagement.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.branchmanagement.annotation.AuditAction;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.ApproveTransactionRequest;
import com.branchmanagement.dto.RejectTransactionRequest;
import com.branchmanagement.dto.StockTransactionResponse;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.StockApprovalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/stock/approve")
public class StockApprovalController {

    private final StockApprovalService approvalService;

    public StockApprovalController(StockApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<StockTransactionResponse>>> getPendingTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách chờ duyệt thành công",
                approvalService.getPendingTransactions(page, size)));
    }

    // ✅ ĐÃ SỬA: Thêm tham số startDate và endDate để khớp với Service
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<StockTransactionResponse>>> getHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy lịch sử giao dịch thành công", 
                approvalService.getHistory(startDate, endDate, page, size)));
    }

    @PostMapping("/{transactionId}/accept")
    @AuditAction(action = "APPROVE_STOCK_TRANSACTION")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> approveTransaction(
            @PathVariable Integer transactionId,
            @RequestBody(required = false) ApproveTransactionRequest request) {
        
        Integer adminId = getCurrentUserId();
        StockTransactionResponse response = approvalService.approveTransaction(transactionId, request, adminId);
        return ResponseEntity.ok(ApiResponse.ok("Duyệt phiếu thành công", response));
    }

    @PostMapping("/{transactionId}/reject")
    @AuditAction(action = "REJECT_STOCK_TRANSACTION")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> rejectTransaction(
            @PathVariable Integer transactionId,
            @Valid @RequestBody RejectTransactionRequest request) {
            
        Integer adminId = getCurrentUserId();
        StockTransactionResponse response = approvalService.rejectTransaction(transactionId, request, adminId);
        return ResponseEntity.ok(ApiResponse.ok("Từ chối phiếu thành công", response));
    }

    // Helper method lấy ID từ JWT
    private Integer getCurrentUserId() {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUserId();
    }
}