package com.branchmanagement.service.impl;

import com.branchmanagement.dto.LeaveRequestResponse;
import com.branchmanagement.entity.LeaveRequest;
import com.branchmanagement.repository.LeaveRequestRepository;
import com.branchmanagement.service.LeaveRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Override
    @Transactional
    public void createRequest(LeaveRequest request) {
        request.setStatus("pending");
        request.setCreatedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> getBranchRequests(Integer branchId, Pageable pageable) {
        Page<LeaveRequest> requests = leaveRequestRepository.findByBranch_BranchId(branchId, pageable);
        
        return requests.map(req -> {
            LeaveRequestResponse dto = new LeaveRequestResponse();
            dto.setLeaveId(req.getLeaveId());
            dto.setFullName(req.getUser().getFullName());
            dto.setLeaveType(req.getLeaveType());
            dto.setStartDate(req.getStartDate());
            dto.setEndDate(req.getEndDate());
            dto.setReason(req.getReason());
            dto.setStatus(req.getStatus());
            return dto;
        });
    }

    // ✅ THÊM MỚI: Employee xem đơn nghỉ phép của chính mình
    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> getMyRequests(Integer userId, Pageable pageable) {
        Page<LeaveRequest> requests = leaveRequestRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
        
        return requests.map(req -> {
            LeaveRequestResponse dto = new LeaveRequestResponse();
            dto.setLeaveId(req.getLeaveId());
            dto.setFullName(req.getUser() != null ? req.getUser().getFullName() : "");
            dto.setLeaveType(req.getLeaveType());
            dto.setStartDate(req.getStartDate());
            dto.setEndDate(req.getEndDate());
            dto.setReason(req.getReason());
            dto.setStatus(req.getStatus());
            dto.setCreatedAt(req.getCreatedAt());
            return dto;
        });
    }

    @Override
    @Transactional
    public void approveRequest(Integer id, Integer managerBranchId) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn nghỉ phép"));
        request.setStatus("approved");
        leaveRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void rejectRequest(Integer id, Integer managerBranchId, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn nghỉ phép"));
        request.setStatus("rejected");
        leaveRequestRepository.save(request);
    }
}