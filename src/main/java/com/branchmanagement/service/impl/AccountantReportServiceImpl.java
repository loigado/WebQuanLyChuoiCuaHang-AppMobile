package com.branchmanagement.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.branchmanagement.dto.AttendanceSummaryResponse;
import com.branchmanagement.repository.AttendanceRepository;
import com.branchmanagement.service.AccountantReportService;

@Service
@Transactional(readOnly = true)
public class AccountantReportServiceImpl implements AccountantReportService {

	private final AttendanceRepository attendanceRepository;

	public AccountantReportServiceImpl(AttendanceRepository attendanceRepository) {
		this.attendanceRepository = attendanceRepository;
	}

	@Override
	public List<AttendanceSummaryResponse> getAttendanceSummary(Integer branchId, LocalDate fromDate,
			LocalDate toDate) {

		LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
		LocalDateTime to = toDate != null ? toDate.atTime(23, 59, 59) : null;

		return attendanceRepository.getAttendanceSummary(branchId, from, to).stream()
				.map(r -> new AttendanceSummaryResponse((Integer) r[0], (String) r[1],
						r[2] != null ? (java.math.BigDecimal) r[2] : java.math.BigDecimal.ZERO))
				.collect(Collectors.toList());
	}
}