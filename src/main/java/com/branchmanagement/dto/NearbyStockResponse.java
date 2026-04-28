package com.branchmanagement.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class NearbyStockResponse {
    private Integer branchId;
    private String branchName;
    private double distance; // Khoảng cách (km hoặc mét)
    private BigDecimal availableQuantity; // Số lượng có thể bán/điều chuyển
}