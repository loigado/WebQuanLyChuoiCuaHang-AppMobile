package com.branchmanagement.service;

import java.time.LocalDate;
import java.util.List;

import com.branchmanagement.dto.AttendanceSummaryResponse;

public interface AccountantReportService {

	List<AttendanceSummaryResponse> getAttendanceSummary(Integer branchId, LocalDate fromDate, LocalDate toDate);
}