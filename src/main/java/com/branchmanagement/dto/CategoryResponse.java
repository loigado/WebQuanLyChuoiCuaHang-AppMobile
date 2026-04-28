package com.branchmanagement.dto;

import java.io.Serializable; // ✅ Thêm import
import java.util.List;

public class CategoryResponse implements Serializable { // ✅ Cần thiết cho Redis
    private static final long serialVersionUID = 1L;

	private Integer categoryId;
	private String categoryCode;
	private String categoryName;
	private String description;
	private Integer parentId;
	private String status;
	private List<CategoryResponse> children;

    // Getters & Setters
    public Integer getCategoryId() { return categoryId; }
	public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
	public String getCategoryCode() { return categoryCode; }
	public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
	public String getCategoryName() { return categoryName; }
	public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Integer getParentId() { return parentId; }
	public void setParentId(Integer parentId) { this.parentId = parentId; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public List<CategoryResponse> getChildren() { return children; }
	public void setChildren(List<CategoryResponse> children) { this.children = children; }
}