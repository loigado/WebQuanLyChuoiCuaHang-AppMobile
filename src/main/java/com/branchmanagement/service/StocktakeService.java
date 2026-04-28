package com.branchmanagement.service;

import com.branchmanagement.entity.*;
import com.branchmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class StocktakeService {

    private final StocktakeRepository stocktakeRepository;
    private final StockRepository stockRepository; 
    private final StockTransactionRepository transactionRepository; 
    private final ProductRepository productRepository; 
    private final com.branchmanagement.repository.BranchRepository branchRepository;

    @lombok.Data
    public static class StocktakeRequestDto {
        private Integer branchId;
        private List<Detail> details;
        
        @lombok.Data
        public static class Detail {
            private Integer productId;
            private BigDecimal systemQuantity;
            private BigDecimal actualQuantity;
            private String note;
        }
    }

    public StocktakeService(StocktakeRepository stocktakeRepository, StockRepository stockRepository, 
                            StockTransactionRepository transactionRepository, ProductRepository productRepository,
                            com.branchmanagement.repository.BranchRepository branchRepository) {
        this.stocktakeRepository = stocktakeRepository;
        this.stockRepository = stockRepository;
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional
    public void createAndComplete(StocktakeRequestDto dto, Integer createdById) {
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));
        
        Stocktake stocktake = new Stocktake();
        stocktake.setBranch(branch);
        stocktake.setStatus("completed");
        stocktake.setCreatedAt(java.time.LocalDateTime.now());
        
        if (createdById != null) {
            User user = new User();
            user.setUserId(createdById);
            stocktake.setCreatedBy(user);
        }
        
        for (StocktakeRequestDto.Detail detailDto : dto.getDetails()) {
            BigDecimal diff = detailDto.getActualQuantity().subtract(detailDto.getSystemQuantity());

            StocktakeDetail detail = new StocktakeDetail();
            detail.setStocktake(stocktake);
            Product p = new Product();
            p.setProductId(detailDto.getProductId());
            detail.setProduct(p);
            detail.setSystemQuantity(detailDto.getSystemQuantity());
            detail.setActualQuantity(detailDto.getActualQuantity());
            // No note/discrepancyReason field in Entity, so we skip it.
            stocktake.getDetails().add(detail);

            if (diff.compareTo(BigDecimal.ZERO) != 0) {
                // Tự động tạo một phiếu StockTransaction để cân bằng
                StockTransaction balanceTx = new StockTransaction();
                balanceTx.setTransactionCode("CBK-" + System.currentTimeMillis() + "-" + detailDto.getProductId());
                balanceTx.setStatus("approved");
                
                String reason = "Cân bằng kho. Lý do: " + (detailDto.getNote() != null && !detailDto.getNote().isEmpty() ? detailDto.getNote() : "Không ghi chú");
                balanceTx.setReason(reason);
                
                if (createdById != null) {
                    User user = new User();
                    user.setUserId(createdById);
                    balanceTx.setCreatedBy(user);
                }
                
                StockTransactionDetail txDetail = new StockTransactionDetail();
                txDetail.setStockTransaction(balanceTx);
                txDetail.setProduct(p);
                txDetail.setQuantity(diff.abs());
                balanceTx.addDetail(txDetail);

                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    balanceTx.setTransactionType("import"); // Lệch thừa -> Nhập bù
                    balanceTx.setToBranch(branch);
                } else {
                    balanceTx.setTransactionType("export"); // Lệch thiếu -> Xuất hủy
                    balanceTx.setFromBranch(branch);
                }
                transactionRepository.save(balanceTx);
                
                // Cập nhật số lượng trực tiếp vào Kho (Stock)
                Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(detailDto.getProductId(), dto.getBranchId())
                        .orElse(null);
                if (stock != null) {
                    stock.setQuantity(detailDto.getActualQuantity()); // Set bằng đúng actualQuantity
                    stockRepository.save(stock);
                }
            }
        }
        stocktakeRepository.save(stocktake);
    }
}