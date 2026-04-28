package com.branchmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TransferRequestDto {
    private Integer productId;
    private Integer fromBranchId;
    private Integer toBranchId;
    private BigDecimal quantity;
    private String reason;
    private String priority;
    private LocalDate expectedDate;
}