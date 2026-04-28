package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.LeaveRequestResponse;
import com.branchmanagement.dto.RejectRequest;
import com.branchmanagement.entity.LeaveRequest;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.LeaveRequestService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveService;

    public LeaveRequestController(LeaveRequestService leaveService) {
        this.leaveService = leaveService;
    }

    // Nhân viên tạo đơn mới
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@RequestBody LeaveRequest request, 
                                                   @AuthenticationPrincipal CustomUserDetails user) {
        // Thay vì dùng new Branch(id), hãy set thủ công để đảm bảo luôn biên dịch được
        com.branchmanagement.entity.Branch branch = new com.branchmanagement.entity.Branch();
        branch.setBranchId(user.getBranchId());
        request.setBranch(branch);
        
        leaveService.createRequest(request);
        return ResponseEntity.ok(ApiResponse.ok("Gửi đơn nghỉ phép thành công", null));
    }

    @GetMapping("/manager")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getBranchRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        
        if (user == null || user.getBranchId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Phiên đăng nhập hết hạn hoặc thiếu thông tin chi nhánh"));
        }

        Page<LeaveRequestResponse> data = leaveService.getBranchRequests(user.getBranchId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok("Thành công", data));
    }

    // ✅ THÊM MỚI: Employee xem đơn của chính mình (filter theo userId từ JWT)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập"));
        }

        Page<LeaveRequestResponse> data = leaveService.getMyRequests(user.getUserId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok("Thành công", data));
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveLeave(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // managerBranchId lấy từ session để đảm bảo không duyệt "chéo" chi nhánh
        leaveService.approveRequest(id, userDetails.getBranchId());
        return ResponseEntity.ok(ApiResponse.ok("Đã duyệt đơn nghỉ phép thành công", null));
    }

    /**
     * 2. API Từ chối đơn nghỉ phép kèm lý do (Dành cho Manager)
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectLeave(
            @PathVariable Integer id,
            @Valid @RequestBody RejectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // Truyền lý do từ RequestBody vào Service
        leaveService.rejectRequest(id, userDetails.getBranchId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.ok("Đã từ chối đơn nghỉ phép", null));
    }
}