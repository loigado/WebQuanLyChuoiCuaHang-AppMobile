package com.branchmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.branchmanagement.annotation.AuditAction;
import com.branchmanagement.dto.AdjustmentRequestDto;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.ExportStockRequest;
import com.branchmanagement.dto.StockTransactionResponse;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.impl.ManagerStockServiceImpl;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/manager/stock") 
public class ManagerStockController {

    private final ManagerStockServiceImpl managerStockService;
    private final com.branchmanagement.service.StocktakeService stocktakeService;

    public ManagerStockController(ManagerStockServiceImpl managerStockService,
                                  com.branchmanagement.service.StocktakeService stocktakeService) {
        this.managerStockService = managerStockService;
        this.stocktakeService = stocktakeService;
    }

    @PostMapping("/export/draft")
    @AuditAction(action = "CREATE_EXPORT_DRAFT")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> createExportDraft(@Valid @RequestBody ExportStockRequest request) {
        StockTransactionResponse response = managerStockService.createExportDraft(request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok("Tạo phiếu xuất nháp thành công", response));
    }

    @PostMapping("/{id}/submit")
    @AuditAction(action = "SUBMIT_EXPORT_REQUEST")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> submitRequest(@PathVariable Integer id) {
        StockTransactionResponse response = managerStockService.submitRequest(id); 
        return ResponseEntity.ok(ApiResponse.ok("Gửi duyệt thành công, hàng đã được giữ (reserve)", response));
    }

    @PostMapping("/adjustment")
    @AuditAction(action = "CREATE_ADJUSTMENT_REQUEST")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> createAdjustment(@Valid @RequestBody AdjustmentRequestDto request) {
        StockTransactionResponse response = managerStockService.createAdjustmentRequest(request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok("Tạo phiếu điều chỉnh thành công, chờ Admin duyệt", response));
    }

    @PostMapping("/stocktake/complete")
    public ResponseEntity<ApiResponse<Void>> completeStocktake(
            @Valid @RequestBody com.branchmanagement.service.StocktakeService.StocktakeRequestDto request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            stocktakeService.createAndComplete(request, userDetails != null ? userDetails.getUserId() : null);
            return ResponseEntity.ok(ApiResponse.ok("Cân bằng kho thành công", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Integer getCurrentUserId() {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUserId();
    }
}