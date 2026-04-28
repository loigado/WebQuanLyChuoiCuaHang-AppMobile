package com.branchmanagement.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict; // ✅ Thêm import
import org.springframework.cache.annotation.Cacheable; // ✅ Thêm import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.branchmanagement.dto.CategoryRequest;
import com.branchmanagement.dto.CategoryResponse;
import com.branchmanagement.entity.Category;
import com.branchmanagement.repository.CategoryRepository;
import com.branchmanagement.service.CategoryService;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryServiceImpl(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Override
    @Cacheable(value = "categories", key = "'tree'") // ✅ Đưa từ Controller xuống
	public List<CategoryResponse> getCategoryTree() {
	    List<Category> rootCategories = categoryRepository.findByParentCategoryIsNullAndStatus("active");
	    return rootCategories.stream().map(this::mapToResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional
    @CacheEvict(value = "categories", allEntries = true) // ✅ Xóa cache khi thêm mới
	public CategoryResponse createCategory(CategoryRequest request) {
		if (categoryRepository.existsByCategoryCode(request.getCategoryCode())) {
			throw new RuntimeException("Mã danh mục đã tồn tại");
		}
		Category category = new Category();
		category.setCategoryCode(request.getCategoryCode());
		category.setCategoryName(request.getCategoryName());
		category.setDescription(request.getDescription());
		if (request.getParentId() != null) {
			Category parent = categoryRepository.findById(request.getParentId())
					.orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
			category.setParentCategory(parent);
		}
		Category saved = categoryRepository.save(category);
		return mapToResponse(saved);
	}

	@Override
	@Transactional
    @CacheEvict(value = "categories", allEntries = true) // ✅ Xóa cache khi sửa
	public CategoryResponse updateCategory(Integer id, CategoryRequest request) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
		category.setCategoryName(request.getCategoryName());
		category.setDescription(request.getDescription());
		if (request.getParentId() != null) {
			if (request.getParentId().equals(id)) {
				throw new RuntimeException("Danh mục không thể tự làm cha của chính nó");
			}
			Category parent = categoryRepository.findById(request.getParentId())
					.orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
			category.setParentCategory(parent);
		} else {
			category.setParentCategory(null);
		}
		return mapToResponse(categoryRepository.save(category));
	}

	@Override
	@Transactional
    @CacheEvict(value = "categories", allEntries = true) // ✅ Xóa cache khi xóa
	public void deleteCategory(Integer id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
		category.setStatus("inactive");
		categoryRepository.save(category);
	}

	private CategoryResponse mapToResponse(Category category) {
		CategoryResponse response = new CategoryResponse();
		response.setCategoryId(category.getCategoryId());
		response.setCategoryCode(category.getCategoryCode());
		response.setCategoryName(category.getCategoryName());
		response.setDescription(category.getDescription());
		response.setStatus(category.getStatus());
		if (category.getParentCategory() != null) {
			response.setParentId(category.getParentCategory().getCategoryId());
		}
		if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
			List<CategoryResponse> children = category.getSubCategories().stream()
					.filter(c -> "active".equals(c.getStatus())) 
					.map(this::mapToResponse)
					.collect(Collectors.toList());
			response.setChildren(children);
		}
		return response;
	}
	
	@Override
	public List<CategoryResponse> getAllCategories() {
	    // Sử dụng hàm mapToResponse bạn đã viết sẵn để chuyển Entity sang DTO
	    return categoryRepository.findAll().stream()
	            .map(this::mapToResponse)
	            .collect(Collectors.toList());
	}
}