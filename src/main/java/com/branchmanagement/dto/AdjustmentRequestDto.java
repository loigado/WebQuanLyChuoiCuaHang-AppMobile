package com.branchmanagement.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AdjustmentRequestDto {
    private Integer productId;
    private Integer branchId; // Kho cần kiểm kê
    private BigDecimal actualQuantity; // Số lượng đếm được thực tế
    private String reason; // Lý do: Hàng hỏng, mất mát, sai lệch...
}