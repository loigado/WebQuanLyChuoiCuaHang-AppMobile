package com.branchmanagement.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.branchmanagement.dto.CreateStockRequestDto;
import com.branchmanagement.dto.StockTransactionResponse;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.entity.Product;
import com.branchmanagement.entity.StockTransaction;
import com.branchmanagement.entity.StockTransactionDetail;
import com.branchmanagement.entity.User;
import com.branchmanagement.repository.BranchRepository;
import com.branchmanagement.repository.ProductRepository;
import com.branchmanagement.repository.StockTransactionRepository;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.service.StockRequestService;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of Stock Request Service.
 * Manages inventory import, export, and transfer requests.
 * 
 * @author Antigravity AI
 */
@Service
@Transactional(readOnly = true)
public class StockRequestServiceImpl implements StockRequestService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_REJECTED = "rejected";
    
    private static final String TYPE_EXPORT = "export";
    private static final String TYPE_TRANSFER = "transfer";

    private final StockTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final com.branchmanagement.repository.StockRepository stockRepository;

    public StockRequestServiceImpl(StockTransactionRepository transactionRepository,
                                  ProductRepository productRepository,
                                  BranchRepository branchRepository,
                                  UserRepository userRepository,
                                  com.branchmanagement.repository.StockRepository stockRepository) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    public Page<StockTransactionResponse> getAllRequests(Integer productId, Integer branchId, String status, 
                                                   LocalDateTime startDate, LocalDateTime endDate, 
                                                   Pageable pageable) {
        return transactionRepository.findAllWithFilters(productId, branchId, status, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    /**
     * Creates a new stock transaction request and handles inventory reservation.
     */
    @Override
    @Transactional
    public StockTransactionResponse createStockRequest(CreateStockRequestDto request, Integer creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Branch fromBranch = findBranchById(request.getFromBranchId(), "nguồn");
        Branch toBranch = findBranchById(request.getToBranchId(), "đích");

        StockTransaction transaction = initializeTransaction(request, creator, fromBranch, toBranch);
        processRequestDetails(request, transaction, fromBranch);

        StockTransaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void approveOrRejectRequest(Integer transactionId, String newStatus, String note, Integer approverId) {
        StockTransaction t = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu yêu cầu"));
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người duyệt"));

        String statusLower = newStatus.toLowerCase();
        t.setStatus(statusLower);

        if (STATUS_APPROVED.equals(statusLower)) {
            t.setApprovedBy(approver);
            t.setApprovedAt(LocalDateTime.now());
            t.setNote(note);
        } else if (STATUS_REJECTED.equals(statusLower)) {
            t.setRejectedBy(approver);
            t.setRejectedAt(LocalDateTime.now());
            t.setRejectionReason(note);
        } else {
            t.setNote(note);
        }

        t.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(t);
    }

    // --- Private Helper Methods ---

    private Branch findBranchById(Integer id, String label) {
        if (id == null) return null;
        return branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh " + label));
    }

    private StockTransaction initializeTransaction(CreateStockRequestDto request, User creator, Branch from, Branch to) {
        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionCode("REQ-" + System.currentTimeMillis());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setStatus(STATUS_PENDING);
        transaction.setReason(request.getReason());
        transaction.setCreatedBy(creator);
        transaction.setFromBranch(from);
        transaction.setToBranch(to);
        return transaction;
    }

    private void processRequestDetails(CreateStockRequestDto request, StockTransaction transaction, Branch fromBranch) {
        if (request.getDetails() == null) return;

        for (CreateStockRequestDto.DetailDto detailDto : request.getDetails()) {
            Product product = productRepository.findById(detailDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + detailDto.getProductId()));

            StockTransactionDetail detail = new StockTransactionDetail();
            detail.setStockTransaction(transaction);
            detail.setProduct(product);
            detail.setQuantity(detailDto.getQuantity());
            transaction.getDetails().add(detail);

            // Reserve stock for Export and Transfer
            if (TYPE_EXPORT.equals(request.getTransactionType()) || TYPE_TRANSFER.equals(request.getTransactionType())) {
                reserveStock(detailDto, fromBranch, product);
            }
        }
    }

    private void reserveStock(CreateStockRequestDto.DetailDto detailDto, Branch fromBranch, Product product) {
        if (fromBranch == null) {
            throw new RuntimeException("Phiếu xuất/điều chuyển bắt buộc phải có chi nhánh nguồn");
        }
        
        com.branchmanagement.entity.Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(
                detailDto.getProductId(), fromBranch.getBranchId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong kho chi nhánh này"));
        
        java.math.BigDecimal available = stock.getQuantity().subtract(stock.getReservedQuantity());
        if (detailDto.getQuantity().compareTo(available) > 0) {
            throw new RuntimeException("Tồn kho khả dụng không đủ cho sản phẩm " + product.getProductName() + ". Chỉ còn: " + available);
        }
        
        stock.setReservedQuantity(stock.getReservedQuantity().add(detailDto.getQuantity()));
        stockRepository.save(stock);
    }

    private StockTransactionResponse toResponse(StockTransaction t) {
        StockTransactionResponse dto = new StockTransactionResponse();
        dto.setTransactionId(t.getTransactionId());
        dto.setTransactionCode(t.getTransactionCode());
        dto.setTransactionType(t.getTransactionType());
        dto.setStatus(t.getStatus());

        if (t.getDetails() != null && !t.getDetails().isEmpty()) {
            dto.setDetails(t.getDetails().stream().map(d -> {
                StockTransactionResponse.DetailDto item = new StockTransactionResponse.DetailDto();
                item.productId = d.getProduct().getProductId();
                item.productCode = d.getProduct().getProductCode();
                item.productName = d.getProduct().getProductName();
                item.productUnit = d.getProduct().getUnit();
                item.quantity = d.getQuantity();
                item.unitPrice = d.getUnitPrice();
                return item;
            }).collect(Collectors.toList()));
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
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        
        return dto;
    }
}