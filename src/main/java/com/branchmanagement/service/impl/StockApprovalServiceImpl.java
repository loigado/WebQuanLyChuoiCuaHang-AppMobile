package com.branchmanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.branchmanagement.dto.ApproveTransactionRequest;
import com.branchmanagement.dto.RejectTransactionRequest;
import com.branchmanagement.dto.StockTransactionResponse;
import com.branchmanagement.entity.Stock;
import com.branchmanagement.entity.StockTransaction;
import com.branchmanagement.entity.StockTransactionDetail;
import com.branchmanagement.entity.User;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.repository.StockTransactionRepository;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.repository.TransferRequestRepository;
import com.branchmanagement.service.NotificationService;
import com.branchmanagement.service.StockApprovalService;

@Service
@Transactional(readOnly = true)
public class StockApprovalServiceImpl implements StockApprovalService {

	private static final Logger logger = LoggerFactory.getLogger(StockApprovalServiceImpl.class);

	private final StockTransactionRepository transactionRepository;
	private final StockRepository stockRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;
    private final TransferRequestRepository transferReqRepo;

	public StockApprovalServiceImpl(StockTransactionRepository transactionRepository, StockRepository stockRepository,
			UserRepository userRepository, NotificationService notificationService, TransferRequestRepository transferReqRepo) {
		this.transactionRepository = transactionRepository;
		this.stockRepository = stockRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
        this.transferReqRepo = transferReqRepo;
	}

	@Override
	public Page<StockTransactionResponse> getPendingTransactions(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return transactionRepository.findByStatus("pending", pageable).map(this::toResponse);
	}

	@Override
    public Page<StockTransactionResponse> getHistory(LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Chuyển LocalDate thành LocalDateTime (có giờ phút giây)
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        // Gọi xuống Repository
        return transactionRepository.findApprovalHistoryWithDate(start, end, pageable)
                .map(this::toResponse); // toResponse là hàm map DTO có sẵn của bạn
    }

	@Override
	public List<StockTransactionResponse> getPendingTransactions() {
		return transactionRepository.findByStatusOrderByCreatedAtDesc("pending").stream().map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public List<StockTransactionResponse> getPendingByType(String transactionType) {
		return transactionRepository.findByStatusAndTransactionTypeOrderByCreatedAtDesc("pending", transactionType)
				.stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public StockTransactionResponse approveTransaction(Integer transactionId, ApproveTransactionRequest request, Integer adminId) {
		StockTransaction transaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu với ID: " + transactionId));

		if (!"pending".equalsIgnoreCase(transaction.getStatus())) {
		    throw new RuntimeException("Chỉ có thể duyệt phiếu đang ở trạng thái 'pending'");
		}

		User admin = userRepository.findById(adminId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy admin với ID: " + adminId));

		transaction.setStatus("approved");
		transaction.setApprovedBy(admin);
		transaction.setApprovedAt(LocalDateTime.now());

		if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
			String existingNote = transaction.getNote() != null ? transaction.getNote() : "";
			transaction.setNote(existingNote + "\n[Admin ghi chú]: " + request.getNote());
		}

		switch (transaction.getTransactionType()) {
		case "import":
			handleImportApproval(transaction);
			break;
		case "export":
			handleExportApproval(transaction);
			break;
		case "adjustment":
			handleAdjustmentApproval(transaction);
			break;
		case "transfer_out":
			handleTransferOutApproval(transaction);
			break;
		case "transfer_in":
			handleTransferInApproval(transaction);
			break;
		case "transfer":
		    handleTransferApproval(transaction);
		    break;
		default:
			throw new RuntimeException("Loại phiếu không hợp lệ");
		}

		StockTransaction saved = transactionRepository.save(transaction);
		logger.info("Admin {} đã duyệt phiếu {}", admin.getFullName(), transaction.getTransactionCode());

        if (saved.getCreatedBy() != null) {
            notificationService.sendAlert(saved.getCreatedBy(), "transaction_approved", "Phiếu kho ĐÃ ĐƯỢC DUYỆT",
                "Phiếu " + saved.getTransactionCode() + " đã được duyệt.", "Normal", "/manager/stock/history");
        }
		return toResponse(saved);
	}

	@Override
	@Transactional
	public StockTransactionResponse rejectTransaction(Integer transactionId, RejectTransactionRequest request, Integer adminId) {
		StockTransaction transaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu với ID: " + transactionId));

		if (!"pending".equalsIgnoreCase(transaction.getStatus())) {
		    throw new RuntimeException("Chỉ có thể duyệt phiếu đang ở trạng thái 'pending'");
		}

		User admin = userRepository.findById(adminId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy admin với ID: " + adminId));

		transaction.setStatus("rejected");
		transaction.setRejectedBy(admin);
		transaction.setRejectedAt(LocalDateTime.now());

		if (request != null) {
			transaction.setRejectionReason(request.getRejectionReason());
		}

		if ("export".equals(transaction.getTransactionType()) || "transfer_out".equals(transaction.getTransactionType()) || "transfer".equals(transaction.getTransactionType())) {
		    returnReservedQuantity(transaction);
		}

        if ("transfer_out".equals(transaction.getTransactionType()) && transaction.getTransferRequest() != null) {
            com.branchmanagement.entity.TransferRequest req = transaction.getTransferRequest();
            req.setStatus("Rejected_By_Admin");
            transferReqRepo.save(req);
        }

		StockTransaction saved = transactionRepository.save(transaction);

        if (saved.getCreatedBy() != null) {
            notificationService.sendAlert(saved.getCreatedBy(), "transaction_rejected", "Phiếu kho BỊ TỪ CHỐI",
                "Phiếu " + saved.getTransactionCode() + " đã bị từ chối.", "Urgent", "/manager/stock/history");
        }
		return toResponse(saved);
	}

	// ✅ ĐÃ SỬA: SỬ DỤNG VÒNG LẶP ĐỂ DUYỆT TỪNG MÓN
	private void handleImportApproval(StockTransaction transaction) {
		Integer branchId = transaction.getToBranch().getBranchId();
        for (StockTransactionDetail detail : transaction.getDetails()) {
            Integer productId = detail.getProduct().getProductId();
            Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(productId, branchId).orElseGet(() -> {
                Stock s = new Stock();
                s.setProduct(detail.getProduct());
                s.setBranch(transaction.getToBranch());
                s.setQuantity(BigDecimal.ZERO);
                s.setReservedQuantity(BigDecimal.ZERO);
                return stockRepository.save(s);
            });
            stock.setQuantity(stock.getQuantity().add(detail.getQuantity()));
            stockRepository.save(stock);
        }
	}

	private void handleExportApproval(StockTransaction transaction) {
		Integer branchId = transaction.getFromBranch().getBranchId();
        for (StockTransactionDetail detail : transaction.getDetails()) {
            Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(detail.getProduct().getProductId(), branchId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho SP: " + detail.getProduct().getProductName()));
            
            // ✅ Đã khôi phục logic trừ trực tiếp sản phẩm
            stock.setQuantity(stock.getQuantity().subtract(detail.getQuantity()));
            stock.setReservedQuantity(stock.getReservedQuantity().subtract(detail.getQuantity()));
            stockRepository.save(stock);
        }
	}

	private void handleAdjustmentApproval(StockTransaction transaction) {
        for (StockTransactionDetail detail : transaction.getDetails()) {
            Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(
                    detail.getProduct().getProductId(), transaction.getFromBranch().getBranchId()
            ).orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho"));
            stock.setQuantity(stock.getQuantity().add(detail.getQuantity()));
            stockRepository.save(stock);
        }
	}

	private void handleTransferOutApproval(StockTransaction transaction) {
		handleExportApproval(transaction);
        if (transaction.getTransferRequest() != null) {
            com.branchmanagement.entity.TransferRequest req = transaction.getTransferRequest();
            req.setStatus("Shipping");
            transferReqRepo.save(req);
        }
	}

	private void handleTransferInApproval(StockTransaction transaction) {
		handleImportApproval(transaction);
	}

	private void returnReservedQuantity(StockTransaction transaction) {
		Integer branchId = transaction.getFromBranch().getBranchId();
        for (StockTransactionDetail detail : transaction.getDetails()) {
            Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(detail.getProduct().getProductId(), branchId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho"));
            java.math.BigDecimal newReserved = stock.getReservedQuantity().subtract(detail.getQuantity());
            if (newReserved.compareTo(java.math.BigDecimal.ZERO) < 0) {
                newReserved = java.math.BigDecimal.ZERO;
            }
            stock.setReservedQuantity(newReserved);
            stockRepository.save(stock);
        }
	}
	
	private void handleTransferApproval(StockTransaction transaction) {
	    Integer fromBranchId = transaction.getFromBranch().getBranchId();
	    Integer toBranchId = transaction.getToBranch().getBranchId();

        for (StockTransactionDetail detail : transaction.getDetails()) {
            Integer productId = detail.getProduct().getProductId();
            BigDecimal qty = detail.getQuantity();

            Stock fromStock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(productId, fromBranchId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho tại chi nhánh xuất"));

            // ✅ AUTO-FIX: Tự động vá lỗi thiếu reserve cho các phiếu cũ bị kẹt
            if (fromStock.getReservedQuantity().compareTo(qty) < 0) {
                java.math.BigDecimal missingReserve = qty.subtract(fromStock.getReservedQuantity());
                java.math.BigDecimal available = fromStock.getQuantity().subtract(fromStock.getReservedQuantity());
                if (available.compareTo(missingReserve) < 0) {
                    throw new RuntimeException("Số lượng tồn kho thực tế không đủ để duyệt phiếu điều chuyển này!");
                }
                fromStock.setReservedQuantity(fromStock.getReservedQuantity().add(missingReserve));
            }
            
            fromStock.setQuantity(fromStock.getQuantity().subtract(qty));
            fromStock.setReservedQuantity(fromStock.getReservedQuantity().subtract(qty));
            stockRepository.save(fromStock);

            Stock toStock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(productId, toBranchId)
                    .orElseGet(() -> {
                        Stock s = new Stock();
                        s.setProduct(detail.getProduct());
                        s.setBranch(transaction.getToBranch());
                        s.setQuantity(BigDecimal.ZERO);
                        s.setReservedQuantity(BigDecimal.ZERO);
                        return s;
                    });
            toStock.setQuantity(toStock.getQuantity().add(qty));
            stockRepository.save(toStock);
        }
	}

	private StockTransactionResponse toResponse(StockTransaction t) {
		StockTransactionResponse dto = new StockTransactionResponse();

		dto.setTransactionId(t.getTransactionId());
		dto.setTransactionCode(t.getTransactionCode());
		dto.setTransactionType(t.getTransactionType());

        if (t.getDetails() != null && !t.getDetails().isEmpty()) {
            List<StockTransactionResponse.DetailDto> detailList = t.getDetails().stream().map(d -> {
                StockTransactionResponse.DetailDto item = new StockTransactionResponse.DetailDto();
                item.productId = d.getProduct().getProductId();
                item.productCode = d.getProduct().getProductCode();
                item.productName = d.getProduct().getProductName();
                item.productUnit = d.getProduct().getUnit();
                item.quantity = d.getQuantity();
                item.unitPrice = d.getUnitPrice();
                return item;
            }).collect(Collectors.toList());
            dto.setDetails(detailList);

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
		dto.setAttachmentUrl(t.getAttachmentUrl());
		dto.setStatus(t.getStatus());

		if (t.getCreatedBy() != null) {
			dto.setCreatedById(t.getCreatedBy().getUserId());
			dto.setCreatedByName(t.getCreatedBy().getFullName());
		}

		if (t.getApprovedBy() != null) {
			dto.setApprovedById(t.getApprovedBy().getUserId());
			dto.setApprovedByName(t.getApprovedBy().getFullName());
		}

		dto.setApprovedAt(t.getApprovedAt());
		dto.setRejectedAt(t.getRejectedAt());
		dto.setRejectionReason(t.getRejectionReason());
		dto.setExpectedDate(t.getExpectedDate());
		dto.setActualDate(t.getActualDate());
		dto.setCreatedAt(t.getCreatedAt());
		dto.setUpdatedAt(t.getUpdatedAt());

		return dto;
	}
	
}