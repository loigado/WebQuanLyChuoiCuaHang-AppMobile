package com.branchmanagement.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class TransferStockRequest {
    private Integer productId;
    private Integer fromBranchId;
    private Integer toBranchId;
    private BigDecimal quantity;
    private String reason;
}