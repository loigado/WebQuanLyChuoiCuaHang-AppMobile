package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.InventoryRequestResponse;
import com.branchmanagement.service.InventoryRequestService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-requests")
public class InventoryRequestController {

    private final InventoryRequestService inventoryRequestService;

    public InventoryRequestController(InventoryRequestService inventoryRequestService) {
        this.inventoryRequestService = inventoryRequestService;
    }

    // 1. Lấy danh sách phiếu chờ duyệt
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<InventoryRequestResponse>>> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách thành công", 
                inventoryRequestService.getPendingRequests(pageable)));
    }

    // 2. Duyệt phiếu kho
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRequest(@PathVariable Integer id) {
        try {
            inventoryRequestService.approveRequest(id);
            return ResponseEntity.ok(ApiResponse.ok("Đã duyệt phiếu kho và cập nhật số lượng tồn kho", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 3. Từ chối phiếu kho
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable Integer id, 
            @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            inventoryRequestService.rejectRequest(id, reason);
            return ResponseEntity.ok(ApiResponse.ok("Đã từ chối phiếu kho", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 4. Lấy lịch sử duyệt phiếu (CÓ CHỨC NĂNG LỌC NGÀY VÀ CHI NHÁNH)
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<InventoryRequestResponse>>> getHistoryRequests(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        // Gọi xuống Service với đầy đủ tham số để lọc
        return ResponseEntity.ok(ApiResponse.ok("Lấy lịch sử thành công", 
                inventoryRequestService.getHistoryRequests(branchId, startDate, endDate, pageable)));
    }
}