package com.branchmanagement.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryValueDto {
    private String categoryName;
    private BigDecimal totalQuantity;
    private BigDecimal totalValue; // Số lượng * Đơn giá
}