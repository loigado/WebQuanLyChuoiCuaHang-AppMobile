package com.branchmanagement.repository;

import com.branchmanagement.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    // Lấy đơn của nhân viên thuộc chi nhánh cụ thể
    Page<LeaveRequest> findByBranch_BranchIdOrderByCreatedAtDesc(Integer branchId, Pageable pageable);
    
    // Lấy đơn của cá nhân nhân viên
    Page<LeaveRequest> findByUser_UserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);
    Page<LeaveRequest> findByBranch_BranchId(Integer branchId, Pageable pageable);
}