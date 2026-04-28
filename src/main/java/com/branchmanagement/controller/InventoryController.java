package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.NearbyStockResponse;
import com.branchmanagement.dto.StockDetailResponse;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.StockViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/manager/inventory")
public class InventoryController {

    private final StockViewService stockViewService;

    public InventoryController(StockViewService stockViewService) {
        this.stockViewService = stockViewService;
    }

    // 1. API Báo cáo tồn kho (Đã khóa quyền Manager)
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<StockDetailResponse>>> getStockReport(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) Integer categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

            Integer targetBranchId = branchId;
            
            // Ép buộc: Nếu không phải Admin thì lấy đúng branchId của user đang đăng nhập
            if (!isAdmin) {
                targetBranchId = userDetails.getBranchId();
            }

            List<StockDetailResponse> report = stockViewService.getAdvancedStockReport(targetBranchId, categoryId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy báo cáo tồn kho thành công", report));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 2. API Tìm kho lân cận (Smart Routing)
    @GetMapping("/find-nearby")
    public ResponseEntity<ApiResponse<List<NearbyStockResponse>>> findNearbyStock(
            @RequestParam Integer productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            List<NearbyStockResponse> data = stockViewService.findNearbyStock(productId, userDetails);
            return ResponseEntity.ok(ApiResponse.ok("Tìm kho lân cận thành công", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/advanced-report")
    public ResponseEntity<ApiResponse<?>> getAdvancedStockReport(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) Integer categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // Nếu không phải ADMIN và không truyền branchId, tự động lấy kho của Manager đó
            Integer targetBranchId = branchId;
            if (targetBranchId == null) {
                targetBranchId = userDetails.getBranchId();
            }

            List<StockDetailResponse> report = stockViewService.getAdvancedStockReport(targetBranchId, categoryId);
            return ResponseEntity.ok(ApiResponse.ok("Tải báo cáo tồn kho thành công", report));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}