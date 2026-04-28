package com.branchmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.branchmanagement.annotation.AuditAction;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.TransferRequestDto;
import com.branchmanagement.entity.TransferRequest;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.impl.TransferServiceImpl;
import com.branchmanagement.repository.TransferRequestRepository;
import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/manager/stock/transfer")
public class TransferController {

    private final TransferServiceImpl transferService;
    private final TransferRequestRepository transferRepo;

    public TransferController(TransferServiceImpl transferService, TransferRequestRepository transferRepo) {
        this.transferService = transferService;
        this.transferRepo = transferRepo;
    }

    // ✅ ĐÃ THÊM: Lấy danh sách phiếu điều chuyển theo chi nhánh hiện tại
    @GetMapping
    public ResponseEntity<?> getMyTransfers() {
        try {
            CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Integer branchId = user.getBranchId();
            
            List<TransferRequest> transfers;
            boolean isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
            if (isAdmin) {
                transfers = transferRepo.findAllByOrderByCreatedAtDesc();
            } else {
                if (branchId == null) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Tài khoản của bạn không được gán vào chi nhánh nào!"));
                }
                transfers = transferRepo.findByBranchId(branchId);
            }
            
            // ✅ MAP DỮ LIỆU SANG CẤU TRÚC AN TOÀN ĐỂ TRÁNH LỖI JACKSON LAZY LOADING
            List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
            for (TransferRequest t : transfers) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("requestId", t.getRequestId());
                map.put("requestCode", t.getRequestCode());
                map.put("quantity", t.getQuantity());
                map.put("reason", t.getReason());
                map.put("priority", t.getPriority());
                map.put("status", t.getStatus());
                if (t.getCreatedAt() != null) map.put("createdAt", t.getCreatedAt().toString());
                
                if (t.getProduct() != null) {
                    java.util.Map<String, Object> prod = new java.util.HashMap<>();
                    prod.put("productId", t.getProduct().getProductId());
                    prod.put("productName", t.getProduct().getProductName());
                    map.put("product", prod);
                }
                if (t.getFromBranch() != null) {
                    java.util.Map<String, Object> fBranch = new java.util.HashMap<>();
                    fBranch.put("branchId", t.getFromBranch().getBranchId());
                    fBranch.put("branchName", t.getFromBranch().getBranchName());
                    map.put("fromBranch", fBranch);
                }
                if (t.getToBranch() != null) {
                    java.util.Map<String, Object> tBranch = new java.util.HashMap<>();
                    tBranch.put("branchId", t.getToBranch().getBranchId());
                    tBranch.put("branchName", t.getToBranch().getBranchName());
                    map.put("toBranch", tBranch);
                }
                result.add(map);
            }
            
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách điều chuyển thành công", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi tải dữ liệu: " + e.getMessage()));
        }
    }

    // ✅ ĐÃ THÊM: Lấy chi tiết 1 phiếu
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransferRequest>> getById(@PathVariable Integer id) {
        TransferRequest tr = transferRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu điều chuyển"));
        return ResponseEntity.ok(ApiResponse.ok("Thành công", tr));
    }

    @PostMapping("/request")
    @AuditAction(action = "CREATE_TRANSFER_REQUEST")
    public ResponseEntity<?> create(@Valid @RequestBody TransferRequestDto dto) {
        return ResponseEntity.ok(transferService.createRequest(dto, getCurrentUserId())); 
    }

    @PostMapping("/{id}/approve")
    @AuditAction(action = "APPROVE_TRANSFER")
    public ResponseEntity<?> approve(@PathVariable Integer id) {
        return ResponseEntity.ok(transferService.approveRequest(id, getCurrentUserId())); 
    }

    @PostMapping("/{id}/reject")
    @AuditAction(action = "REJECT_TRANSFER")
    public ResponseEntity<?> reject(@PathVariable Integer id, @RequestParam String reason) {
        return ResponseEntity.ok(transferService.rejectRequest(id, getCurrentUserId(), reason));
    }

    @PostMapping("/{id}/ship")
    @AuditAction(action = "CONFIRM_SHIPPING")
    public ResponseEntity<?> ship(@PathVariable Integer id) {
        return ResponseEntity.ok(transferService.confirmShipping(id));
    }

    @PostMapping("/{id}/receive")
    @AuditAction(action = "RECEIVE_TRANSFER")
    public ResponseEntity<?> receive(@PathVariable Integer id) {
        // ✅ Đổi thành receiveTransfer
        return ResponseEntity.ok(transferService.receiveTransfer(id, getCurrentUserId())); 
    }

    private Integer getCurrentUserId() {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUserId();
    }
}