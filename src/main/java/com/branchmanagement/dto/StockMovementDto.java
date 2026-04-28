package com.branchmanagement.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class StockMovementDto {
    private String productName;
    private BigDecimal openingStock; // Tồn đầu kỳ
    private BigDecimal totalImport;  // Tổng nhập
    private BigDecimal totalExport;  // Tổng xuất
    private BigDecimal totalTransfer; // Điều chuyển (+/-)
    private BigDecimal closingStock; // Tồn cuối kỳ
}