package com.branchmanagement.service;

import java.util.List;
import com.branchmanagement.dto.CategoryRequest;
import com.branchmanagement.dto.CategoryResponse;
import com.branchmanagement.entity.Category;

public interface CategoryService {
	List<CategoryResponse> getCategoryTree();
	CategoryResponse createCategory(CategoryRequest request);
	CategoryResponse updateCategory(Integer id, CategoryRequest request);
	void deleteCategory(Integer id); // Soft delete
	List<CategoryResponse> getAllCategories();
}