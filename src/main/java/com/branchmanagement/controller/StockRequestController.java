package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.CreateStockRequestDto;
import com.branchmanagement.dto.StockTransactionResponse;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.StockRequestService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/manager/stock-requests")
public class StockRequestController {

    private final StockRequestService stockRequestService;

    public StockRequestController(StockRequestService stockRequestService) {
        this.stockRequestService = stockRequestService;
    }

    /**
     * 1. TẠO PHIẾU YÊU CẦU MỚI
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StockTransactionResponse>> createStockRequest(
            @Valid @RequestBody CreateStockRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) { 
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Vui lòng đăng nhập để thực hiện tác vụ này"));
            }

            Integer currentManagerId = userDetails.getUserId();
            StockTransactionResponse created = stockRequestService.createStockRequest(request, currentManagerId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Tạo phiếu yêu cầu kho thành công, đang chờ duyệt", created));
                    
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 2. LẤY DANH SÁCH PHIẾU CÓ PHÂN TRANG VÀ BỘ LỌC
     * Fix lỗi: "No Data" bằng cách nhận productId và branchId để lọc trực tiếp từ SQL
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<StockTransactionResponse>>> getAllStockRequests(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            java.time.LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
            java.time.LocalDateTime end = toDate != null ? toDate.atTime(java.time.LocalTime.MAX) : null;

            Page<StockTransactionResponse> requests = stockRequestService.getAllRequests(productId, branchId, status, start, end, pageable);
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách phiếu thành công", requests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi tải danh sách: " + e.getMessage()));
        }
    }

    /**
     * 3. CẬP NHẬT TRẠNG THÁI (DUYỆT/TỪ CHỐI)
     */
@PutMapping("/{id}/status")
public ResponseEntity<ApiResponse<?>> updateRequestStatus(
        @PathVariable Integer id,
        @RequestBody Map<String, String> payload,
        @AuthenticationPrincipal CustomUserDetails userDetails) { // ✅ Thêm userDetails để lấy người duyệt
    try {
        String newStatus = payload.get("status");
        String note = payload.get("note");
        
        // Truyền thêm ID người duyệt xuống Service
        stockRequestService.approveOrRejectRequest(id, newStatus, note, userDetails.getUserId());
        
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công!", null));
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
    }
}
}