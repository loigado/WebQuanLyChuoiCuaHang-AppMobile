package com.branchmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.CategoryRequest;
import com.branchmanagement.dto.CategoryResponse;
import com.branchmanagement.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/manager/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	/**
	 * Lấy cây danh mục sản phẩm (Đã được cache tại tầng Service)
	 */
	@GetMapping("/tree")
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
		try {
			List<CategoryResponse> tree = categoryService.getCategoryTree();
			return ResponseEntity.ok(ApiResponse.ok("Lấy cây danh mục thành công", tree));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
		}
	}

	/**
	 * Tạo danh mục mới (Tự động xóa cache tại tầng Service)
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
		try {
			CategoryResponse created = categoryService.createCategory(request);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.ok("Tạo danh mục thành công", created));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
		}
	}

	/**
	 * Cập nhật danh mục (Tự động xóa cache tại tầng Service)
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable Integer id,
			@Valid @RequestBody CategoryRequest request) {
		try {
			CategoryResponse updated = categoryService.updateCategory(id, request);
			return ResponseEntity.ok(ApiResponse.ok("Cập nhật danh mục thành công", updated));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
		}
	}

	/**
	 * Xóa danh mục - Soft Delete (Tự động xóa cache tại tầng Service)
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
		try {
			categoryService.deleteCategory(id);
			return ResponseEntity.ok(ApiResponse.ok("Đã vô hiệu hóa danh mục", null));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
		}
	}
	
	/**
	 * Lấy tất cả danh mục dạng danh sách phẳng (Đã được cache tại tầng Service)
	 */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCategories() {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách danh mục thành công", categoryService.getAllCategories()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }
}