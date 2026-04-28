package com.branchmanagement.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.StockTransactionResponse;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.StockViewService;

@RestController
@RequestMapping("/api/manager/inventory/history")
public class StockHistoryController {

    private final StockViewService stockViewService;

    public StockHistoryController(StockViewService stockViewService) {
        this.stockViewService = stockViewService;
    }

    /**
     * Lấy toàn bộ lịch sử biến động kho của chi nhánh
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getHistory(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer branchId = userDetails.getBranchId();
        List<StockTransactionResponse> data = stockViewService.getAllTransactions(branchId, productId, type, status, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.ok("Lấy lịch sử biến động kho thành công", data));
    }
}
