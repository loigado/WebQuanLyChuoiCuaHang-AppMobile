package com.branchmanagement.entity;

import com.branchmanagement.enums.RoleType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "[User]")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "username", unique = true, nullable = false, length = 50)
	private String username;

	@JsonIgnore // ✅ ĐÃ THÊM: Ẩn password hash khỏi JSON response
	@Column(name = "password", nullable = false, length = 255)
	private String password;

	@Column(name = "full_name", nullable = false, length = 100)
	private String fullName;

	/**
	 * Chuyển sang dùng Enum để quản lý Role chặt chẽ hơn
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "role", length = 20)
	private RoleType role = RoleType.EMPLOYEE;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "branch_id")
	@JsonIgnoreProperties("users")
	private Branch branch;

	@Column(name = "status", length = 20)
	private String status = "active";

	@Column(name = "hourly_rate", precision = 12, scale = 2)
	private java.math.BigDecimal hourlyRate = java.math.BigDecimal.ZERO;

    public User() {}

	// ==========================================
	// GETTERS & SETTERS
	// ==========================================

	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }

	public RoleType getRole() { return role; }
	public void setRole(RoleType role) { this.role = role; }

	public Branch getBranch() { return branch; }
	public void setBranch(Branch branch) { this.branch = branch; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public java.math.BigDecimal getHourlyRate() { return hourlyRate; }
	public void setHourlyRate(java.math.BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
}