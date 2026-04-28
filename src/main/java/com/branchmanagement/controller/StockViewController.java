package com.branchmanagement.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.StockDetailResponse;
import com.branchmanagement.service.StockViewService;

@RestController
@RequestMapping("/api/manager") // ✅ Đảm bảo tiền tố này khớp với đường dẫn gọi từ FE
public class StockViewController {

    private final StockViewService stockViewService;

    public StockViewController(StockViewService stockViewService) {
        this.stockViewService = stockViewService;
    }

    /**
     * API Lấy tồn kho chi tiết (Dùng cho cả trang Báo cáo và Kiểm kê)
     * Đường dẫn: GET /api/manager/inventory-detail
     */
    @GetMapping("/inventory-detail")
    public ResponseEntity<ApiResponse<List<StockDetailResponse>>> getInventoryDetail(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) Integer categoryId) {
        try {
            // Sử dụng hàm đã có trong StockViewService
            List<StockDetailResponse> report = stockViewService.getAdvancedStockReport(branchId, categoryId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy dữ liệu tồn kho thành công", report));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi khi lấy dữ liệu: " + e.getMessage()));
        }
    }
}