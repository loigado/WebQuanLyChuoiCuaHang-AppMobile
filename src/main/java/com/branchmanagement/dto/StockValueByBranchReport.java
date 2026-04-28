package com.branchmanagement.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO báo cáo giá trị tồn kho theo chi nhánh
 */
public class StockValueByBranchReport {
	private Integer branchId;
	private String branchName;
	private BigDecimal totalValue; // Tổng giá trị tồn kho
	private Integer totalProducts; // Tổng số loại sản phẩm
	private BigDecimal totalQuantity; // Tổng số lượng

	// Getters & Setters
	public Integer getBranchId() {
		return branchId;
	}

	public void setBranchId(Integer branchId) {
		this.branchId = branchId;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public BigDecimal getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(BigDecimal totalValue) {
		this.totalValue = totalValue;
	}

	public Integer getTotalProducts() {
		return totalProducts;
	}

	public void setTotalProducts(Integer totalProducts) {
		this.totalProducts = totalProducts;
	}

	public BigDecimal getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(BigDecimal totalQuantity) {
		this.totalQuantity = totalQuantity;
	}
}

/**
 * DTO báo cáo giá trị tồn kho theo sản phẩm
 */
class StockValueByProductReport {
	private Integer productId;
	private String productCode;
	private String productName;
	private String unit;
	private BigDecimal totalQuantity;
	private BigDecimal averagePrice;
	private BigDecimal totalValue;

	// Getters & Setters
	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public BigDecimal getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(BigDecimal totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(BigDecimal averagePrice) {
		this.averagePrice = averagePrice;
	}

	public BigDecimal getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(BigDecimal totalValue) {
		this.totalValue = totalValue;
	}
}

/**
 * DTO báo cáo giao dịch kho (nhập/xuất/chênh lệch)
 */
class TransactionSummaryReport {
	private BigDecimal totalImportValue;
	private BigDecimal totalExportValue;
	private BigDecimal difference;
	private Integer totalImportCount;
	private Integer totalExportCount;

	// Getters & Setters
	public BigDecimal getTotalImportValue() {
		return totalImportValue;
	}

	public void setTotalImportValue(BigDecimal totalImportValue) {
		this.totalImportValue = totalImportValue;
	}

	public BigDecimal getTotalExportValue() {
		return totalExportValue;
	}

	public void setTotalExportValue(BigDecimal totalExportValue) {
		this.totalExportValue = totalExportValue;
	}

	public BigDecimal getDifference() {
		return difference;
	}

	public void setDifference(BigDecimal difference) {
		this.difference = difference;
	}

	public Integer getTotalImportCount() {
		return totalImportCount;
	}

	public void setTotalImportCount(Integer totalImportCount) {
		this.totalImportCount = totalImportCount;
	}

	public Integer getTotalExportCount() {
		return totalExportCount;
	}

	public void setTotalExportCount(Integer totalExportCount) {
		this.totalExportCount = totalExportCount;
	}
}

/**
 * DTO ma trận điều chuyển giữa chi nhánh
 */
class BranchTransferMatrixReport {
	private List<BranchTransferItem> items;

	public List<BranchTransferItem> getItems() {
		return items;
	}

	public void setItems(List<BranchTransferItem> items) {
		this.items = items;
	}

	public static class BranchTransferItem {
		private String fromBranchName;
		private String toBranchName;
		private Integer transferCount;
		private BigDecimal totalQuantity;

		public String getFromBranchName() {
			return fromBranchName;
		}

		public void setFromBranchName(String fromBranchName) {
			this.fromBranchName = fromBranchName;
		}

		public String getToBranchName() {
			return toBranchName;
		}

		public void setToBranchName(String toBranchName) {
			this.toBranchName = toBranchName;
		}

		public Integer getTransferCount() {
			return transferCount;
		}

		public void setTransferCount(Integer transferCount) {
			this.transferCount = transferCount;
		}

		public BigDecimal getTotalQuantity() {
			return totalQuantity;
		}

		public void setTotalQuantity(BigDecimal totalQuantity) {
			this.totalQuantity = totalQuantity;
		}
	}
}