package com.branchmanagement.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.branchmanagement.repository.AttendanceRepository;
import com.branchmanagement.repository.StockTransactionRepository;
import com.branchmanagement.service.AdminReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

	private final AttendanceRepository attendanceRepository;
	private final StockTransactionRepository transactionRepository;

	@Override
	public Map<String, Object> getSystemSummary() {

		Map<String, Object> summary = new HashMap<>();

		summary.put("totalAttendances", attendanceRepository.count());
		summary.put("totalTransactions", transactionRepository.count());
		summary.put("pendingTransactions", transactionRepository.countByStatus("PENDING"));

        // ✅ Trở lại logic đơn giản: Đếm theo loại phiếu gốc
        summary.put("importCount", transactionRepository.countByTransactionType("import"));
        summary.put("exportCount", transactionRepository.countByTransactionType("export"));
        summary.put("transferCount", transactionRepository.countByTransactionType("transfer"));

		return summary;
	}
}