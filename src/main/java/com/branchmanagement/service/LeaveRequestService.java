package com.branchmanagement.service;

import com.branchmanagement.entity.LeaveRequest;
import com.branchmanagement.dto.LeaveRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveRequestService {
    void createRequest(LeaveRequest request);
    
    Page<LeaveRequestResponse> getBranchRequests(Integer branchId, Pageable pageable);
    
    // ✅ THÊM MỚI: Employee xem đơn của chính mình
    Page<LeaveRequestResponse> getMyRequests(Integer userId, Pageable pageable);
    
    void approveRequest(Integer id, Integer managerBranchId);
    
    void rejectRequest(Integer id, Integer managerBranchId, String reason);
}