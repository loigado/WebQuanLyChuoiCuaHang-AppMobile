package com.branchmanagement.service;

import java.util.List;

import com.branchmanagement.dto.BranchResponse;
import com.branchmanagement.entity.Branch;

/**
 * Interface định nghĩa các nghiệp vụ quản lý Branch
 */
public interface BranchService {

	/**
	 * Lấy danh sách tất cả chi nhánh Dùng cho dropdown chọn chi nhánh trong form
	 */
	List<BranchResponse> getAllBranches();

	/**
	 * Lấy danh sách chi nhánh đang hoạt động (status = 'active')
	 */
	List<BranchResponse> getActiveBranches();

	/**
	 * Lấy chi tiết một chi nhánh theo ID
	 */
	BranchResponse getBranchById(Integer id);

	/**
	 * Lấy chi nhánh theo mã (branch_code)
	 */
	BranchResponse getBranchByCode(String branchCode);

	Branch createBranch(Branch request);

	Branch updateBranch(Integer id, Branch request);

	void toggleStatus(Integer id);
}