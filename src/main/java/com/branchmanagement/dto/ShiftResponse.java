package com.branchmanagement.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ShiftResponse {
	private Integer shiftId;
	private Integer branchId;
	private String branchName;
	private String shiftName;
	private String shiftType;
	private LocalTime startTime;
	private LocalTime endTime;
	private LocalDate date;
	private String status;
	private int assignedCount;
	private List<AssignedUserDTO> assignedUsers;

	public static class AssignedUserDTO {
		private Integer userId;
		private String fullName;
		private String role;
		private String shiftStatus; // assigned | completed | absent
		private Integer userShiftId;

		public AssignedUserDTO(Integer userId, String fullName, String role, String shiftStatus, Integer userShiftId) {
			this.userId = userId;
			this.fullName = fullName;
			this.role = role;
			this.shiftStatus = shiftStatus;
			this.userShiftId = userShiftId;
		}

		public Integer getUserId() {
			return userId;
		}

		public String getFullName() {
			return fullName;
		}

		public String getRole() {
			return role;
		}

		public String getShiftStatus() {
			return shiftStatus;
		}

		public Integer getUserShiftId() {
			return userShiftId;
		}
	}

	// Builder-style setters
	public ShiftResponse setShiftId(Integer v) {
		this.shiftId = v;
		return this;
	}

	public ShiftResponse setBranchId(Integer v) {
		this.branchId = v;
		return this;
	}

	public ShiftResponse setBranchName(String v) {
		this.branchName = v;
		return this;
	}

	public ShiftResponse setShiftName(String v) {
		this.shiftName = v;
		return this;
	}

	public ShiftResponse setShiftType(String v) {
		this.shiftType = v;
		return this;
	}

	public ShiftResponse setStartTime(LocalTime v) {
		this.startTime = v;
		return this;
	}

	public ShiftResponse setEndTime(LocalTime v) {
		this.endTime = v;
		return this;
	}

	public ShiftResponse setDate(LocalDate v) {
		this.date = v;
		return this;
	}

	public ShiftResponse setStatus(String v) {
		this.status = v;
		return this;
	}

	public ShiftResponse setAssignedCount(int v) {
		this.assignedCount = v;
		return this;
	}

	public ShiftResponse setAssignedUsers(List<AssignedUserDTO> v) {
		this.assignedUsers = v;
		return this;
	}

	public Integer getShiftId() {
		return shiftId;
	}

	public Integer getBranchId() {
		return branchId;
	}

	public String getBranchName() {
		return branchName;
	}

	public String getShiftName() {
		return shiftName;
	}

	public String getShiftType() {
		return shiftType;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getStatus() {
		return status;
	}

	public int getAssignedCount() {
		return assignedCount;
	}

	public List<AssignedUserDTO> getAssignedUsers() {
		return assignedUsers;
	}
}