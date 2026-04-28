package com.branchmanagement.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MobileStockResponse {
    private Integer productId;
    private String productName;
    private String productCode;
    private String unit;
    private BigDecimal quantity;
    private boolean isLowStock;
}
