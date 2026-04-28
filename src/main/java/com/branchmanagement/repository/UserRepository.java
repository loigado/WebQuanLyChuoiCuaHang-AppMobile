package com.branchmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.branchmanagement.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByUsername(String username);
	long countByBranch_BranchId(Integer branchId);

	Optional<User> findByUsername(String username);

	Page<User> findAll(Pageable pageable);

	// ✅ THÊM MỚI
	List<User> findByBranch_BranchIdOrderByFullNameAsc(Integer branchId);

	// ✅ ĐÃ SỬA: Tham số phải là RoleType (Enum), không phải String
	List<User> findByRoleOrderByFullNameAsc(com.branchmanagement.enums.RoleType role);

	// ✅ ĐÃ SỬA: Dùng Enum cho JPQL query
	@Query("SELECT u FROM User u WHERE u.branch.branchId = :branchId AND u.role = com.branchmanagement.enums.RoleType.MANAGER")
    List<User> findManagersByBranch(@Param("branchId") Integer branchId);

    // 2. Tìm tất cả Admin
    @Query("SELECT u FROM User u WHERE u.role = com.branchmanagement.enums.RoleType.ADMIN")
    List<User> findAllAdmins();
    
}