package com.branchmanagement.dto;

import java.math.BigDecimal;

public class AttendanceSummaryResponse {

	private Integer userId;
	private String fullName;
	private BigDecimal totalHours;

	public AttendanceSummaryResponse(Integer userId, String fullName, BigDecimal totalHours) {
		this.userId = userId;
		this.fullName = fullName;
		this.totalHours = totalHours;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public BigDecimal getTotalHours() {
		return totalHours;
	}

	public void setTotalHours(BigDecimal totalHours) {
		this.totalHours = totalHours;
	}

	// getters

}