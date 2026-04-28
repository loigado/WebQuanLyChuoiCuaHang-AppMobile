package com.branchmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.branchmanagement.entity.Camera;

/**
 * Repository cho entity Camera
 */
@Repository
public interface CameraRepository extends JpaRepository<Camera, Integer> {

	/**
	 * Lấy tất cả camera, sắp xếp mới nhất lên đầu
	 */
	List<Camera> findAllByOrderByCreatedAtDesc();

	/**
	 * Lấy camera theo chi nhánh (dùng cho filter theo branch)
	 */
	List<Camera> findByBranch_BranchIdOrderByCreatedAtDesc(Integer branchId);

	/**
	 * Lấy camera theo trạng thái (active/inactive)
	 */
	List<Camera> findByStatusOrderByCreatedAtDesc(String status);

	/**
	 * Lấy camera active theo chi nhánh
	 */
	List<Camera> findByBranch_BranchIdAndStatus(Integer branchId, String status);
}