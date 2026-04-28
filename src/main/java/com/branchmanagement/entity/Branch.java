package com.branchmanagement.entity;

import java.math.BigDecimal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entity mapping bảng Branch trong SQL Server Đại diện cho các chi nhánh trong
 * hệ thống
 */
@Entity
@Table(name = "Branch")
public class Branch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "branch_id")
	private Integer branchId;

	/**
	 * Mã chi nhánh - unique, dùng để định danh ngắn gọn Ví dụ: HCM01, HN01, DN01
	 */
	@Column(name = "branch_code", unique = true, nullable = false, length = 20)
	private String branchCode;

	/**
	 * Tên đầy đủ của chi nhánh Ví dụ: Chi nhánh Quận 1, Chi nhánh Tân Bình
	 */
	@Column(name = "branch_name", nullable = false, length = 100)
	private String branchName;

	/**
	 * Địa chỉ chi tiết của chi nhánh
	 */
	@Column(name = "address", length = 300)
	private String address;

	/**
	 * Tọa độ vĩ độ (latitude) - dùng cho chấm công GPS Ví dụ: 10.7769 (TP.HCM)
	 */
	@Column(name = "latitude", precision = 10, scale = 8, nullable = false)
	
	
	private BigDecimal latitude;
	
	
	public Branch(Integer branchId) {
	    this.branchId = branchId;
	}
	/**
	 * Tọa độ kinh độ (longitude) - dùng cho chấm công GPS Ví dụ: 106.7009 (TP.HCM)
	 */
	@Column(name = "longitude", precision = 11, scale = 8, nullable = false)
	private BigDecimal longitude;

	/**
	 * Trạng thái: 'active' | 'inactive' Dùng soft delete: set status = 'inactive'
	 * thay vì xóa record
	 */
	@Column(name = "status", length = 20)
	private String status = "active";

	/**
	 * Ngày tạo chi nhánh
	 */
	@Column(name = "created_at", updatable = false)
	private java.time.LocalDateTime createdAt;

	// Tự động set thời gian tạo trước khi persist
	@PrePersist
	protected void onCreate() {
		this.createdAt = java.time.LocalDateTime.now();
	}

	// Quan hệ OneToMany với Camera
	@JsonIgnore
	@OneToMany(mappedBy = "branch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private java.util.List<Camera> cameras;

	// Explicit getters (to avoid Lombok dependency)
	public Integer getBranchId() {
		return branchId;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public String getBranchName() {
		return branchName;
	}

	public String getAddress() {
		return address;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public String getStatus() {
		return status;
	}

	public java.time.LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public java.util.List<Camera> getCameras() {
		return cameras;
	}

	// Optional setters if needed by JPA or tests
	public void setBranchId(Integer branchId) {
		this.branchId = branchId;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setCameras(java.util.List<Camera> cameras) {
		this.cameras = cameras;
	}
	public Branch() {
	}
}