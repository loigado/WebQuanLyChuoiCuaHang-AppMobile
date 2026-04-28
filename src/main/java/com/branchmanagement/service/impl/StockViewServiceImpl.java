package com.branchmanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.branchmanagement.dto.StockDetailResponse;
import com.branchmanagement.dto.StockFinancialSummaryResponse;
import com.branchmanagement.dto.StockQuantityByBranchResponse;
import com.branchmanagement.dto.StockTransactionResponse;
import com.branchmanagement.dto.NearbyStockResponse;
import com.branchmanagement.entity.StockTransaction;
import com.branchmanagement.entity.StockTransactionDetail;
import com.branchmanagement.entity.Stocktake;
import com.branchmanagement.entity.Stock;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.repository.StockTransactionRepository;
import com.branchmanagement.repository.BranchRepository; 
import com.branchmanagement.repository.StocktakeRepository; // ✅ ĐÃ THÊM IMPORT
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.StockViewService;
import com.branchmanagement.service.SystemConfigService;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class StockViewServiceImpl implements StockViewService {

    private final StockTransactionRepository transactionRepository;
    private final StockRepository stockRepository;
    private final SystemConfigService systemConfigService;
    private final BranchRepository branchRepository;
    private final StocktakeRepository stocktakeRepository; // ✅ ĐÃ THÊM BIẾN
    
    // ✅ CẬP NHẬT CONSTRUCTOR ĐỂ INJECT REPOSITORY MỚI
    public StockViewServiceImpl(StockTransactionRepository transactionRepository, 
            StockRepository stockRepository,
            SystemConfigService systemConfigService,
            BranchRepository branchRepository,
            StocktakeRepository stocktakeRepository) { 
        this.transactionRepository = transactionRepository;
        this.stockRepository = stockRepository;
        this.systemConfigService = systemConfigService;
        this.branchRepository = branchRepository;
        this.stocktakeRepository = stocktakeRepository;
    }

   @Override
    public List<StockDetailResponse> getAdvancedStockReport(Integer branchId, Integer categoryId) {
        return stockRepository.findDetailedStock(branchId, categoryId).stream()
            .map(stock -> {
                StockDetailResponse dto = new StockDetailResponse();
                dto.setBranchId(stock.getBranch().getBranchId());
                dto.setBranchName(stock.getBranch().getBranchName());
                
                if (stock.getProduct() != null) {
                    dto.setProductId(stock.getProduct().getProductId());
                    dto.setProductCode(stock.getProduct().getProductCode());
                    dto.setProductName(stock.getProduct().getProductName());
                    dto.setUnit(stock.getProduct().getUnit());
                    
                    dto.setMinThreshold(BigDecimal.valueOf(stock.getProduct().getMinStock()));
                    dto.setMaxThreshold(BigDecimal.valueOf(stock.getProduct().getMaxStock()));
                    
                    if (stock.getProduct().getCategory() != null) {
                        dto.setCategoryName(stock.getProduct().getCategory().getCategoryName());
                    } else {
                        dto.setCategoryName("Chưa phân loại");
                    }
                }
                
                dto.setQuantity(stock.getQuantity());
                dto.setReservedQuantity(stock.getReservedQuantity() != null ? stock.getReservedQuantity() : BigDecimal.ZERO);
                dto.setAvailableQuantity(dto.getQuantity().subtract(dto.getReservedQuantity()));
                
                dto.calculateAlertStatus(); 
                
                return dto;
            }).collect(Collectors.toList());
    }

    @Override
    public StockFinancialSummaryResponse getFinancialSummary(Integer branchId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.atTime(23, 59, 59) : null;

        List<Object[]> results = transactionRepository.getFinancialSummary(branchId, from, to);

        BigDecimal totalImport = BigDecimal.ZERO;
        BigDecimal totalExport = BigDecimal.ZERO;

        if (results != null && !results.isEmpty()) {
            Object[] row = results.get(0);
            if (row[0] != null) totalImport = (BigDecimal) row[0];
            if (row[1] != null) totalExport = (BigDecimal) row[1];
        }
        return new StockFinancialSummaryResponse(totalImport, totalExport);
    }

    @Override
    public List<StockQuantityByBranchResponse> getStockQuantityByBranch() {
        return stockRepository.getTotalQuantityByBranch();
    }

    @Override
    public List<Object[]> getTransferMatrixSimple() {
        return transactionRepository.getTransferMatrixSimple();
    }

    @Override
    public List<StockTransactionResponse> getAllTransactions(Integer branchId, Integer productId, String transactionType, String status,
            LocalDate fromDate, LocalDate toDate) {
        Specification<StockTransaction> spec = buildSpecification(branchId, null, null, productId, transactionType, status, fromDate, toDate);
        return transactionRepository.findAll(spec).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<StockTransactionResponse> getAllImportTransactions(Integer branchId, String status, LocalDate fromDate, LocalDate toDate) {
        Specification<StockTransaction> spec = buildSpecification(null, branchId, null, null, "import", status, fromDate, toDate);
        return transactionRepository.findAll(spec).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<StockTransactionResponse> getAllExportTransactions(Integer branchId, String status, LocalDate fromDate, LocalDate toDate) {
        Specification<StockTransaction> spec = buildSpecification(branchId, null, null, null, "export", status, fromDate, toDate);
        return transactionRepository.findAll(spec).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<StockTransactionResponse> getAllTransferTransactions(Integer fromBranchId, Integer toBranchId, String status, LocalDate fromDate, LocalDate toDate) {
        Specification<StockTransaction> spec = buildTransferSpecification(fromBranchId, toBranchId, status, fromDate, toDate);
        return transactionRepository.findAll(spec).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Specification<StockTransaction> buildSpecification(Integer fromBranchId, Integer toBranchId,
            Integer anyBranchId, Integer productId, String transactionType, String status, LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (fromBranchId != null) predicates.add(cb.equal(root.get("fromBranch").get("branchId"), fromBranchId));
            if (toBranchId != null) predicates.add(cb.equal(root.get("toBranch").get("branchId"), toBranchId));
            if (anyBranchId != null) {
                predicates.add(cb.or(cb.equal(root.get("fromBranch").get("branchId"), anyBranchId),
                        cb.equal(root.get("toBranch").get("branchId"), anyBranchId)));
            }
            if (productId != null) {
                // Join with details to filter by productId
                predicates.add(cb.equal(root.join("details").get("product").get("productId"), productId));
            }
            if (transactionType != null && !transactionType.isBlank()) predicates.add(cb.equal(root.get("transactionType"), transactionType));
            if (status != null && !status.isBlank()) predicates.add(cb.equal(root.get("status"), status));
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate.atStartOfDay()));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate.atTime(23, 59, 59)));
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<StockTransaction> buildTransferSpecification(Integer fromBranchId, Integer toBranchId, String status, LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.or(cb.equal(root.get("transactionType"), "transfer_out"), cb.equal(root.get("transactionType"), "transfer_in")));
            if (fromBranchId != null) predicates.add(cb.equal(root.get("fromBranch").get("branchId"), fromBranchId));
            if (toBranchId != null) predicates.add(cb.equal(root.get("toBranch").get("branchId"), toBranchId));
            if (status != null && !status.isBlank()) predicates.add(cb.equal(root.get("status"), status));
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate.atStartOfDay()));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate.atTime(23, 59, 59)));
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private StockTransactionResponse toResponse(StockTransaction t) {
        StockTransactionResponse dto = new StockTransactionResponse();
        dto.setTransactionId(t.getTransactionId());
        dto.setTransactionCode(t.getTransactionCode());
        dto.setTransactionType(t.getTransactionType());

        if (t.getDetails() != null && !t.getDetails().isEmpty()) {
            List<StockTransactionResponse.DetailDto> details = t.getDetails().stream().map(d -> {
                StockTransactionResponse.DetailDto detailDto = new StockTransactionResponse.DetailDto();
                detailDto.productId = d.getProduct().getProductId();
                detailDto.productCode = d.getProduct().getProductCode();
                detailDto.productName = d.getProduct().getProductName();
                detailDto.productUnit = d.getProduct().getUnit();
                detailDto.quantity = d.getQuantity();
                detailDto.unitPrice = d.getUnitPrice();
                return detailDto;
            }).collect(Collectors.toList());
            dto.setDetails(details);

            // Giữ lại các trường legacy cho tương thích (lấy từ item đầu tiên)
            StockTransactionDetail firstItem = t.getDetails().get(0);
            dto.setProductId(firstItem.getProduct().getProductId());
            dto.setProductCode(firstItem.getProduct().getProductCode());
            dto.setProductName(firstItem.getProduct().getProductName());
            dto.setProductUnit(firstItem.getProduct().getUnit());
            dto.setQuantity(firstItem.getQuantity());
            dto.setUnitPrice(firstItem.getUnitPrice());
            if (firstItem.getQuantity() != null && firstItem.getUnitPrice() != null) {
                dto.setTotalAmount(firstItem.getQuantity().multiply(firstItem.getUnitPrice()));
            }
        }

        if (t.getFromBranch() != null) {
            dto.setFromBranchId(t.getFromBranch().getBranchId());
            dto.setFromBranchName(t.getFromBranch().getBranchName());
        }
        if (t.getToBranch() != null) {
            dto.setToBranchId(t.getToBranch().getBranchId());
            dto.setToBranchName(t.getToBranch().getBranchName());
        }

        dto.setReason(t.getReason());
        dto.setNote(t.getNote());
        dto.setStatus(t.getStatus());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }
    
    @Override
    public List<NearbyStockResponse> findNearbyStock(Integer productId, CustomUserDetails user) {
        Branch myBranch = branchRepository.findById(user.getBranchId()).orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));
        List<Stock> stocks = stockRepository.findAvailableStockInOtherBranches(productId, user.getBranchId());
        return stocks.stream().map(s -> {
            NearbyStockResponse dto = new NearbyStockResponse();
            dto.setBranchId(s.getBranch().getBranchId());
            dto.setBranchName(s.getBranch().getBranchName());
            BigDecimal qty = s.getQuantity() != null ? s.getQuantity() : BigDecimal.ZERO;
            BigDecimal res = s.getReservedQuantity() != null ? s.getReservedQuantity() : BigDecimal.ZERO;
            dto.setAvailableQuantity(qty.subtract(res));
            double dist = calculateDistance(
                myBranch.getLatitude() != null ? myBranch.getLatitude().doubleValue() : 0,
                myBranch.getLongitude() != null ? myBranch.getLongitude().doubleValue() : 0,
                s.getBranch().getLatitude() != null ? s.getBranch().getLatitude().doubleValue() : 0,
                s.getBranch().getLongitude() != null ? s.getBranch().getLongitude().doubleValue() : 0
            );
            dto.setDistance(Math.round(dist * 10.0) / 10.0);
            return dto;
        }).sorted(Comparator.comparingDouble(NearbyStockResponse::getDistance)).collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0) return 999999.0;
        final int R = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
    
    @Override
    public List<Object[]> getStocktakeDiscrepancyReport(Integer stocktakeId) {
        // ✅ ĐÃ KHẮC PHỤC: Sử dụng stocktakeRepository đã được inject
        Stocktake st = stocktakeRepository.findById(stocktakeId).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu kiểm kê"));
        return st.getDetails().stream().map(d -> new Object[]{
            d.getProduct().getProductName(),
            d.getSystemQuantity(),
            d.getActualQuantity(),
            d.getActualQuantity().subtract(d.getSystemQuantity())
        }).collect(Collectors.toList());
    }
}