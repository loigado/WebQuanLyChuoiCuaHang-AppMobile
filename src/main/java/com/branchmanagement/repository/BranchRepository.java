package com.branchmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.branchmanagement.entity.Branch;

/**
 * Repository cho entity Branch Cung cấp các phương thức truy vấn chi nhánh
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {

	/**
	 * Tìm chi nhánh theo mã chi nhánh (branch_code) Dùng khi cần check trùng lặp
	 * hoặc tìm nhanh bằng code
	 */
	Optional<Branch> findByBranchCode(String branchCode);

	/**
	 * Tìm chi nhánh theo tên (tìm chính xác)
	 */
	Optional<Branch> findByBranchName(String branchName);

	/**
	 * Tìm chi nhánh theo tên (tìm gần đúng - LIKE) Hữu ích cho chức năng search
	 */
	List<Branch> findByBranchNameContaining(String keyword);

	/**
	 * Lấy tất cả chi nhánh đang hoạt động (status = 'active')
	 */
	List<Branch> findByStatus(String status);

	/**
	 * Lấy tất cả chi nhánh, sắp xếp theo tên A-Z
	 */
	List<Branch> findAllByOrderByBranchNameAsc();

	/**
	 * Kiểm tra chi nhánh có tồn tại theo branch_code không Return true/false thay
	 * vì Optional
	 */
	boolean existsByBranchCode(String branchCode);

	/**
	 * Đếm số lượng chi nhánh đang hoạt động
	 */
	@Query("SELECT COUNT(b) FROM Branch b WHERE b.status = 'active'")
	long countActiveBranches();

	/**
	 * Tìm chi nhánh trong bán kính (km) từ tọa độ cho trước Dùng công thức
	 * Haversine để tính khoảng cách GPS
	 * 
	 * @param latitude  Vĩ độ trung tâm
	 * @param longitude Kinh độ trung tâm
	 * @param radiusKm  Bán kính tìm kiếm (km)
	 */
	@Query(value = "SELECT * FROM Branch b WHERE "
			+ "(6371 * acos(cos(radians(:latitude)) * cos(radians(b.latitude)) * "
			+ "cos(radians(b.longitude) - radians(:longitude)) + "
			+ "sin(radians(:latitude)) * sin(radians(b.latitude)))) <= :radiusKm "
			+ "AND b.status = 'active'", nativeQuery = true)
	List<Branch> findBranchesWithinRadius(Double latitude, Double longitude, Double radiusKm);
}