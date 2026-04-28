package com.branchmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryRequest {
	@NotBlank(message = "Mã danh mục không được để trống")
	private String categoryCode;

	@NotBlank(message = "Tên danh mục không được để trống")
	private String categoryName;

	private String description;
	private Integer parentId; // Null nếu là danh mục gốc

	// Getters & Setters
	public String getCategoryCode() { return categoryCode; }
	public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
	public String getCategoryName() { return categoryName; }
	public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Integer getParentId() { return parentId; }
	public void setParentId(Integer parentId) { this.parentId = parentId; }
}