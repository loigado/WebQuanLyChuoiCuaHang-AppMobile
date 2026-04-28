package com.branchmanagement.dto;

import java.math.BigDecimal;

public class AttendanceDetailSummaryResponse {

	private Integer userId;
	private String fullName;
	private String branchName;
	private BigDecimal totalHours;
	private Long totalDays;
	private Long abnormalCount;
	private Long lateCount;
	private Long earlyLeaveCount;

	public AttendanceDetailSummaryResponse(Integer userId, String fullName, String branchName, BigDecimal totalHours,
			Long totalDays, Long abnormalCount, Long lateCount, Long earlyLeaveCount) {
		this.userId = userId;
		this.fullName = fullName;
		this.branchName = branchName;
		this.totalHours = totalHours != null ? totalHours : BigDecimal.ZERO;
		this.totalDays = totalDays != null ? totalDays : 0L;
		this.abnormalCount = abnormalCount != null ? abnormalCount : 0L;
		this.lateCount = lateCount != null ? lateCount : 0L;
		this.earlyLeaveCount = earlyLeaveCount != null ? earlyLeaveCount : 0L;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getBranchName() {
		return branchName;
	}

	public BigDecimal getTotalHours() {
		return totalHours;
	}

	public Long getTotalDays() {
		return totalDays;
	}

	public Long getAbnormalCount() {
		return abnormalCount;
	}

	public Long getLateCount() {
		return lateCount;
	}

	public Long getEarlyLeaveCount() {
		return earlyLeaveCount;
	}
}