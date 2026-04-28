package com.branchmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.branchmanagement.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
	boolean existsByCategoryCode(String categoryCode);
	
	// Lấy các danh mục gốc (không có cha)
	List<Category> findByParentCategoryIsNull();
	
	List<Category> findByParentCategoryIsNullAndStatus(String status);
}