package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.LeaveRequestResponse;
import com.branchmanagement.entity.LeaveRequest;
import com.branchmanagement.repository.LeaveRequestRepository;
import com.branchmanagement.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager/leave-requests")
public class ManagerLeaveController {

    private final LeaveRequestRepository leaveRequestRepository;

    public ManagerLeaveController(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    // 1. Lấy danh sách đơn xin nghỉ của Chi nhánh (Quản lý đang quản lý)
    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getRequestsByBranch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Page<LeaveRequest> requestPage = leaveRequestRepository
                    .findByBranch_BranchIdOrderByCreatedAtDesc(userDetails.getBranchId(), PageRequest.of(page, size));

            List<LeaveRequestResponse> responses = requestPage.stream().map(lr -> {
                LeaveRequestResponse dto = new LeaveRequestResponse();
                dto.setLeaveId(lr.getLeaveId());
                dto.setUserId(lr.getUser().getUserId());
                dto.setFullName(lr.getUser().getFullName());
                dto.setLeaveType(lr.getLeaveType());
                dto.setStartDate(lr.getStartDate());
                dto.setEndDate(lr.getEndDate());
                dto.setReason(lr.getReason());
                dto.setStatus(lr.getStatus());
                dto.setRejectReason(lr.getRejectReason());
                dto.setCreatedAt(lr.getCreatedAt());
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.ok("Thành công", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 2. Duyệt đơn (Approve)
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveLeave(@PathVariable Integer id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn xin nghỉ"));
        
        request.setStatus("approved");
        leaveRequestRepository.save(request);
        return ResponseEntity.ok(ApiResponse.ok("Đã duyệt đơn nghỉ phép!", null));
    }

    // 3. Từ chối đơn (Reject)
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectLeave(
            @PathVariable Integer id, 
            @RequestBody Map<String, String> payload) {
        
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn xin nghỉ"));
        
        String reason = payload.get("rejectReason");
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Phải nhập lý do từ chối");
        }

        request.setStatus("rejected");
        request.setRejectReason(reason);
        leaveRequestRepository.save(request);
        return ResponseEntity.ok(ApiResponse.ok("Đã từ chối đơn nghỉ phép!", null));
    }
}