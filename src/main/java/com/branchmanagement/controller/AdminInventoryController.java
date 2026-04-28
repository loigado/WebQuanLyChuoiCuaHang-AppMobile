package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.StockQuantityByBranchResponse;
import com.branchmanagement.repository.StockRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/inventory")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminInventoryController {

    private final StockRepository stockRepository;

    public AdminInventoryController(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @GetMapping("/summary-by-branch")
    public ResponseEntity<ApiResponse<List<StockQuantityByBranchResponse>>> getSummaryByBranch() {
        try {
            List<StockQuantityByBranchResponse> summary = stockRepository.getTotalQuantityByBranch();
            return ResponseEntity.ok(ApiResponse.ok("Thành công", summary));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInventoryDetails(
            @RequestParam(required = false) Integer branchId) {
        try {
            List<Object[]> results = stockRepository.getStockDetail(branchId);
            List<Map<String, Object>> data = results.stream().map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("branchId", row[0]);
                map.put("branchName", row[1]);
                map.put("productId", row[2]);
                map.put("productCode", row[3]);
                map.put("productName", row[4]);
                map.put("unit", row[5]);
                map.put("quantity", row[6]);
                map.put("reservedQuantity", row[7]);
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.ok("Thành công", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
}
