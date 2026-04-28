package com.branchmanagement.dto;

import java.math.BigDecimal;

public class StockFinancialSummaryResponse {

	private BigDecimal totalImportValue;
	private BigDecimal totalExportValue;
	private BigDecimal difference;

	public StockFinancialSummaryResponse(BigDecimal totalImportValue, BigDecimal totalExportValue) {
		this.totalImportValue = totalImportValue != null ? totalImportValue : BigDecimal.ZERO;
		this.totalExportValue = totalExportValue != null ? totalExportValue : BigDecimal.ZERO;
		this.difference = this.totalImportValue.subtract(this.totalExportValue);
	}

	public BigDecimal getTotalImportValue() {
		return totalImportValue;
	}

	public BigDecimal getTotalExportValue() {
		return totalExportValue;
	}

	public BigDecimal getDifference() {
		return difference;
	}
}