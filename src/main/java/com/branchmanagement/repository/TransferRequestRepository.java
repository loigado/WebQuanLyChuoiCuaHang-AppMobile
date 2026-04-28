package com.branchmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.branchmanagement.entity.TransferRequest;
import java.util.List;

import org.springframework.data.repository.query.Param;

public interface TransferRequestRepository extends JpaRepository<TransferRequest, Integer> {
    
    // Phiếu điều chuyển liên quan đến chi nhánh (là nguồn HOẶC đích)
    @Query("SELECT t FROM TransferRequest t WHERE t.fromBranch.branchId = :branchId OR t.toBranch.branchId = :branchId ORDER BY t.createdAt DESC")
    List<TransferRequest> findByBranchId(@Param("branchId") Integer branchId);
    
    List<TransferRequest> findAllByOrderByCreatedAtDesc();

    long countByFromBranch_BranchIdAndStatusIgnoreCase(Integer branchId, String status);

    long countByToBranch_BranchIdAndStatusIgnoreCase(Integer branchId, String status);

    @Query("SELECT t FROM TransferRequest t WHERE LOWER(t.status) IN ('pending', 'shipping') ORDER BY t.createdAt DESC")
    List<TransferRequest> findPendingAndShipping();
}