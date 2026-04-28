package com.branchmanagement.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StocktakeRequestDto {
    @NotNull(message = "Vui lòng chọn chi nhánh")
    private Integer branchId;
    
    private List<Detail> details;

    @Data
    public static class Detail {
        private Integer productId;
        private BigDecimal systemQuantity;
        private BigDecimal actualQuantity;
        private String note;
    }
}
