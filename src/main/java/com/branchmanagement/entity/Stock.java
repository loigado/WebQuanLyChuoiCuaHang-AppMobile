package com.branchmanagement.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity mapping bảng Stock trong SQL Server Lưu số lượng tồn kho của từng sản
 * phẩm tại từng chi nhánh
 */
@Entity
@Table(name = "[Stock]")
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "stock_id")
	private Integer stockId;

	/**
	 * Sản phẩm
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	/**
	 * Chi nhánh
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	/**
	 * Số lượng tồn kho thực tế
	 */
	@Column(name = "quantity", precision = 18, scale = 2)
	private BigDecimal quantity = BigDecimal.ZERO;

	/**
	 * Số lượng đã đặt trước/reserve (đang chờ duyệt xuất kho) Khi tạo phiếu xuất →
	 * tăng reserved_quantity Khi duyệt phiếu xuất → giảm quantity và giảm
	 * reserved_quantity
	 */
	@Column(name = "reserved_quantity", precision = 18, scale = 2)
	private BigDecimal reservedQuantity = BigDecimal.ZERO;

	// Getters & Setters
	public Integer getStockId() {
		return stockId;
	}

	public void setStockId(Integer stockId) {
		this.stockId = stockId;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getReservedQuantity() {
		return reservedQuantity;
	}

	public void setReservedQuantity(BigDecimal reservedQuantity) {
		this.reservedQuantity = reservedQuantity;
	}
}