package com.branchmanagement.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.*;

@Entity
@Table(name = "[Category]")
@SQLDelete(sql = "UPDATE [Category] SET is_deleted = 1 WHERE category_id = ?")
@SQLRestriction("is_deleted = 0")
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "category_code", unique = true, nullable = false, length = 50)
	private String categoryCode;

	@Column(name = "category_name", nullable = false, length = 100)
	private String categoryName;

	@Column(name = "description", length = 255)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Category parentCategory;

	@OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Category> subCategories = new ArrayList<>();

	@Column(name = "status", length = 20)
	private String status = "active";

	@Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // ✅ Sửa lỗi LocalDateTime
	private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean isDeleted = false;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// Getters & Setters
	public Integer getCategoryId() { return categoryId; }
	public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
	public String getCategoryCode() { return categoryCode; }
	public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
	public String getCategoryName() { return categoryName; }
	public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Category getParentCategory() { return parentCategory; }
	public void setParentCategory(Category parentCategory) { this.parentCategory = parentCategory; }
	public List<Category> getSubCategories() { return subCategories; }
	public void setSubCategories(List<Category> subCategories) { this.subCategories = subCategories; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}