package com.branchmanagement.service.impl;

import com.branchmanagement.dto.InventoryRequestResponse;
import com.branchmanagement.entity.InventoryRequest;
import com.branchmanagement.entity.Product;
import com.branchmanagement.entity.Stock;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.repository.InventoryRequestRepository;
import com.branchmanagement.repository.ProductRepository;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.service.InventoryRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime; // ✅ ĐÃ THÊM IMPORT NÀY ĐỂ FIX LỖI

@Service
public class InventoryRequestServiceImpl implements InventoryRequestService {

    private final InventoryRequestRepository requestRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public InventoryRequestServiceImpl(InventoryRequestRepository requestRepository, 
                                     ProductRepository productRepository,
                                     StockRepository stockRepository) {
        this.requestRepository = requestRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryRequestResponse> getPendingRequests(Pageable pageable) {
        return requestRepository.findByStatusOrderByCreatedAtDesc("pending", pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void approveRequest(Integer requestId) {
        InventoryRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu yêu cầu"));

        if (!"pending".equalsIgnoreCase(request.getStatus().trim())) {
            throw new RuntimeException("Chỉ có thể duyệt phiếu đang ở trạng thái chờ");
        }

        Product product = request.getProduct();
        Branch branch = request.getBranch();

        Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(
                product.getProductId(), branch.getBranchId()
        ).orElseGet(() -> {
            Stock newStock = new Stock();
            newStock.setProduct(product);
            newStock.setBranch(branch);
            newStock.setQuantity(BigDecimal.ZERO);
            return newStock;
        });

        BigDecimal requestQty = BigDecimal.valueOf(request.getQuantity());

        if ("import".equalsIgnoreCase(request.getRequestType().trim())) {
            stock.setQuantity(stock.getQuantity().add(requestQty));
        } else if ("export".equalsIgnoreCase(request.getRequestType().trim())) {
            if (stock.getQuantity().compareTo(requestQty) < 0) {
                throw new RuntimeException("Số lượng tồn kho không đủ để xuất");
            }
            stock.setQuantity(stock.getQuantity().subtract(requestQty));
        }

        stockRepository.save(stock);
        
        request.setStatus("approved");
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.saveAndFlush(request);
    }

    @Override
    @Transactional
    public void rejectRequest(Integer requestId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Chỉ có thể từ chối khi có lý do");
        }
        
        InventoryRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu yêu cầu"));

        if (!"pending".equalsIgnoreCase(request.getStatus().trim())) {
            throw new RuntimeException("Chỉ có thể từ chối phiếu đang ở trạng thái chờ");
        }
        
        request.setStatus("rejected");
        request.setRejectReason(reason);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.saveAndFlush(request);
    }

    private InventoryRequestResponse toResponse(InventoryRequest entity) {
        InventoryRequestResponse res = new InventoryRequestResponse();
        res.setRequestId(entity.getRequestId());
        if (entity.getBranch() != null) res.setBranchName(entity.getBranch().getBranchName());
        if (entity.getUser() != null) res.setFullName(entity.getUser().getFullName());
        if (entity.getProduct() != null) res.setProductName(entity.getProduct().getProductName());
        res.setRequestType(entity.getRequestType());
        res.setQuantity(entity.getQuantity());
        res.setReason(entity.getReason());
        res.setStatus(entity.getStatus());
        res.setRejectReason(entity.getRejectReason());
        res.setCreatedAt(entity.getCreatedAt());
        return res;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryRequestResponse> getHistoryRequests(Pageable pageable) {
        return requestRepository.findByStatusInOrderByCreatedAtDesc(
                java.util.Arrays.asList("approved", "rejected"), pageable)
                .map(this::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryRequestResponse> getHistoryRequests(Integer branchId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        
        // Nếu có ngày kết thúc, set nó đến cuối ngày (23:59:59) để lấy đủ dữ liệu trong ngày đó
        LocalDateTime endOfDay = (endDate != null) ? endDate.withHour(23).withMinute(59).withSecond(59) : null;
        
        // Gọi xuống Repository với các tham số lọc
        return requestRepository.findHistoryWithFilters(branchId, startDate, endOfDay, pageable)
                .map(this::toResponse);
    }
}