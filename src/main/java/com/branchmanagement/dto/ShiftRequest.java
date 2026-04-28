package com.branchmanagement.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ShiftRequest {
    private Integer branchId;
    private String shiftName;
    private String shiftType;
    private java.time.LocalTime startTime;
    private java.time.LocalTime endTime;
    private java.time.LocalDate date;
    private Integer createdBy;
    // Getters & Setters

	public Integer getBranchId() {
		return branchId;
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

	public Integer getCreatedBy() {
		return createdBy;
	}
}