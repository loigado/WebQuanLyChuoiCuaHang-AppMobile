package com.branchmanagement.dto;

import java.math.BigDecimal;

public class StockQuantityByBranchResponse {

	private Integer branchId;
	private String branchName;
	private BigDecimal totalQuantity;

	public StockQuantityByBranchResponse(Integer branchId, String branchName, BigDecimal totalQuantity) {
		this.branchId = branchId;
		this.branchName = branchName;
		this.totalQuantity = totalQuantity;
	}

	public Integer getBranchId() {
		return branchId;
	}

	public String getBranchName() {
		return branchName;
	}

	public BigDecimal getTotalQuantity() {
		return totalQuantity;
	}
}