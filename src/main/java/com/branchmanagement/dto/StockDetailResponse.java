package com.branchmanagement.dto;

import java.math.BigDecimal;

public class StockDetailResponse {
    private Integer branchId;
    private String branchName;
    private Integer productId;
    private String productCode;
    private String productName;
    private String categoryName;
    private String unit;
    
    private BigDecimal quantity;          // Tồn thực tế
    private BigDecimal reservedQuantity;  // Đang chờ xuất
    private BigDecimal availableQuantity; // Khả dụng = Tồn - Reserved
    
    private BigDecimal minThreshold;      // Ngưỡng min
    private BigDecimal maxThreshold;      // Ngưỡng max
    private String alertStatus;           // Trạng thái: normal, low, high, error

    public StockDetailResponse() {}

    // Hàm logic để Frontend (Vue.js) hiển thị màu sắc cảnh báo

    public void calculateAlertStatus() {
        // 1. Đảm bảo các giá trị không bị null để tránh lỗi so sánh
        if (this.availableQuantity == null) this.availableQuantity = BigDecimal.ZERO;
        if (this.minThreshold == null) this.minThreshold = BigDecimal.ZERO;
        if (this.maxThreshold == null) this.maxThreshold = BigDecimal.ZERO;

        // 2. Logic xét trạng thái
        if (this.availableQuantity.compareTo(BigDecimal.ZERO) < 0) {
            this.alertStatus = "NEGATIVE_STOCK"; 
        } else if (this.availableQuantity.compareTo(BigDecimal.ZERO) == 0) {
            this.alertStatus = "OUT_OF_STOCK";
        } else if (this.availableQuantity.compareTo(this.minThreshold) < 0) {
            this.alertStatus = "LOW_STOCK";
        } else if (this.maxThreshold.compareTo(BigDecimal.ZERO) > 0 && 
                   this.availableQuantity.compareTo(this.maxThreshold) > 0) {
            this.alertStatus = "OVER_STOCK"; // ✅ Chuẩn trạng thái Vượt mức
        } else {
            this.alertStatus = "NORMAL";
        }
    }

    // --- GETTERS & SETTERS ---
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }
    public BigDecimal getMaxThreshold() { return maxThreshold; }
    public void setMaxThreshold(BigDecimal maxThreshold) { this.maxThreshold = maxThreshold; }
    public String getAlertStatus() { return alertStatus; }
    public void setAlertStatus(String alertStatus) { this.alertStatus = alertStatus; }
}