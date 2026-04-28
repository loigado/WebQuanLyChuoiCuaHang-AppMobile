package com.branchmanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.branchmanagement.dto.AdjustmentRequestDto;
import com.branchmanagement.dto.ExportStockRequest;
import com.branchmanagement.dto.TransferStockRequest; 
import com.branchmanagement.dto.StockTransactionResponse;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.entity.Product;
import com.branchmanagement.entity.Stock;
import com.branchmanagement.entity.StockTransaction;
import com.branchmanagement.entity.StockTransactionDetail; // ✅ Thêm bảng Detail
import com.branchmanagement.entity.User;
import com.branchmanagement.repository.BranchRepository;
import com.branchmanagement.repository.ProductRepository;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.repository.StockTransactionRepository;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.service.NotificationService;

@Service
@Transactional(readOnly = true)
public class ManagerStockServiceImpl {

    private final StockTransactionRepository transactionRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ManagerStockServiceImpl(StockTransactionRepository transactionRepository, StockRepository stockRepository,
            ProductRepository productRepository, BranchRepository branchRepository, UserRepository userRepository,
            NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ================= 2.4.5 XUẤT KHO (EXPORT) =================
    @Transactional
    public StockTransactionResponse createExportDraft(ExportStockRequest request, Integer managerId) {
        Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(request.getProductId(), request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong kho chi nhánh này"));

        BigDecimal availableQuantity = stock.getQuantity().subtract(stock.getReservedQuantity());
        
        if (request.getQuantity().compareTo(availableQuantity) > 0) {
            throw new RuntimeException("Tồn kho khả dụng không đủ. Tồn hiện tại: " + availableQuantity);
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Sản phẩm ID: " + request.getProductId()));
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Chi nhánh ID: " + request.getBranchId()));
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Manager ID: " + managerId));

        // Tạo Phiếu Mẹ
        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionCode("XK" + System.currentTimeMillis());
        transaction.setTransactionType("export");
        transaction.setFromBranch(branch);
        transaction.setReason(request.getReason());
        transaction.setNote(request.getNote());
        transaction.setStatus("draft");
        transaction.setCreatedBy(manager);
        transaction.setCreatedAt(LocalDateTime.now());

        // Thêm Chi tiết sản phẩm
        StockTransactionDetail detail = new StockTransactionDetail();
        detail.setProduct(product);
        detail.setQuantity(request.getQuantity());
        detail.setUnitPrice(BigDecimal.ZERO);
        transaction.addDetail(detail);

        return toResponse(transactionRepository.save(transaction));
    }

    // ================= 2.4.6 ĐIỀU CHUYỂN KHO (TRANSFER) =================
    @Transactional
    public StockTransactionResponse createTransferDraft(TransferStockRequest request, Integer managerId) {
        Stock fromStock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(request.getProductId(), request.getFromBranchId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong kho xuất"));

        BigDecimal available = fromStock.getQuantity().subtract(fromStock.getReservedQuantity());
        if (request.getQuantity().compareTo(available) > 0) {
            throw new RuntimeException("Kho gửi không đủ hàng. Khả dụng: " + available);
        }

        Product product = productRepository.findById(request.getProductId()).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        Branch fromBranch = branchRepository.findById(request.getFromBranchId()).orElseThrow(() -> new RuntimeException("Không tìm thấy kho gửi"));
        Branch toBranch = branchRepository.findById(request.getToBranchId()).orElseThrow(() -> new RuntimeException("Không tìm thấy kho nhận"));
        User manager = userRepository.findById(managerId).orElseThrow(() -> new RuntimeException("Không tìm thấy Manager"));

        // Tạo Phiếu Mẹ
        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionCode("DC" + System.currentTimeMillis());
        transaction.setTransactionType("transfer");
        transaction.setFromBranch(fromBranch);
        transaction.setToBranch(toBranch);
        transaction.setReason(request.getReason());
        transaction.setStatus("draft");
        transaction.setCreatedBy(manager);
        transaction.setCreatedAt(LocalDateTime.now());

        // Thêm Chi tiết sản phẩm
        StockTransactionDetail detail = new StockTransactionDetail();
        detail.setProduct(product);
        detail.setQuantity(request.getQuantity());
        detail.setUnitPrice(BigDecimal.ZERO);
        transaction.addDetail(detail);

        return toResponse(transactionRepository.save(transaction));
    }

    /**
     * Gửi duyệt chung (Status -> Pending & Reserve hàng)
     */
    @Transactional
    public StockTransactionResponse submitRequest(Integer transactionId) {
        StockTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu ID: " + transactionId));

        if (!"draft".equals(transaction.getStatus())) {
            throw new RuntimeException("Chỉ có thể gửi duyệt phiếu ở trạng thái Nháp");
        }

        // Lặp qua các món trong phiếu để Reserve (giữ chỗ)
        for (StockTransactionDetail detail : transaction.getDetails()) {
            Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(
                    detail.getProduct().getProductId(), transaction.getFromBranch().getBranchId())
                    .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy tồn kho để giữ hàng"));

            BigDecimal available = stock.getQuantity().subtract(stock.getReservedQuantity());
            if (detail.getQuantity().compareTo(available) > 0) {
                throw new RuntimeException("Hàng trong kho không còn đủ để gửi duyệt (sản phẩm: " + detail.getProduct().getProductName() + ")");
            }

            stock.setReservedQuantity(stock.getReservedQuantity().add(detail.getQuantity()));
            stockRepository.save(stock);
        }

        transaction.setStatus("pending");
        transaction.setUpdatedAt(LocalDateTime.now());
        StockTransaction savedTx = transactionRepository.save(transaction);

        // ✅ GỬI CẢNH BÁO CHO TẤT CẢ ADMIN (Chờ duyệt)
        List<User> admins = userRepository.findAllAdmins();
        for (User admin : admins) {
            notificationService.sendAlert(
                admin,
                "transaction_pending",
                "Có phiếu kho mới chờ duyệt",
                "Mã phiếu: " + savedTx.getTransactionCode() + " vừa được gửi lên để duyệt.",
                "High",
                "/admin/stock/approve/pending"
            );
        }

        return toResponse(savedTx);
    }

    // ================= 2.4.7 ĐIỀU CHỈNH KHO =================
    @Transactional
    public StockTransactionResponse createAdjustmentRequest(AdjustmentRequestDto request, Integer managerId) {
        Stock stock = stockRepository.findByProduct_ProductIdAndBranch_BranchId(request.getProductId(), request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong kho chi nhánh này"));

        BigDecimal systemQty = stock.getQuantity();
        BigDecimal actualQty = request.getActualQuantity();
        BigDecimal variance = actualQty.subtract(systemQty);

        if (variance.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Số lượng thực tế khớp với hệ thống, không cần điều chỉnh!");
        }

        if (actualQty.compareTo(stock.getReservedQuantity()) < 0) {
            throw new RuntimeException("Lỗi nghiêm trọng: Số lượng thực tế (" + actualQty + 
                ") không thể nhỏ hơn số hàng đang bị giữ chỗ (" + stock.getReservedQuantity() + ")");
        }

        Product product = productRepository.findById(request.getProductId()).orElseThrow();
        Branch branch = branchRepository.findById(request.getBranchId()).orElseThrow();
        User manager = userRepository.findById(managerId).orElseThrow();

        // Tạo Phiếu Mẹ
        StockTransaction tx = new StockTransaction();
        tx.setTransactionCode("ADJ" + System.currentTimeMillis());
        tx.setTransactionType("adjustment");
        tx.setFromBranch(branch); 
        tx.setReason(request.getReason());
        tx.setNote("Hệ thống: " + systemQty + " | Thực tế: " + actualQty); 
        tx.setStatus("pending"); 
        tx.setCreatedBy(manager);
        tx.setCreatedAt(LocalDateTime.now());

        // Thêm Chi tiết sản phẩm (Lưu lượng chênh lệch)
        StockTransactionDetail detail = new StockTransactionDetail();
        detail.setProduct(product);
        detail.setQuantity(variance); // variance có thể âm hoặc dương
        detail.setUnitPrice(BigDecimal.ZERO);
        tx.addDetail(detail);

        StockTransaction savedTx = transactionRepository.save(tx);

        // ✅ GỬI CẢNH BÁO ĐIỀU CHỈNH CHO ADMIN
        List<User> admins = userRepository.findAllAdmins();
        for (User admin : admins) {
            notificationService.sendAlert(
                admin,
                "transaction_pending",
                "Có phiếu ĐIỀU CHỈNH KHO chờ duyệt",
                "Mã phiếu: " + savedTx.getTransactionCode() + " cần được Admin xem xét.",
                "High",
                "/admin/stock/approve/pending"
            );
        }

        return toResponse(savedTx);
    }

    // ================= MAPPING DTO =================
    private StockTransactionResponse toResponse(StockTransaction t) {
        StockTransactionResponse dto = new StockTransactionResponse();
        dto.setTransactionId(t.getTransactionId());
        dto.setTransactionCode(t.getTransactionCode());
        dto.setTransactionType(t.getTransactionType());
        dto.setStatus(t.getStatus());
        dto.setReason(t.getReason());
        dto.setCreatedAt(t.getCreatedAt());

        // Xử lý lấy thông tin món hàng đầu tiên
        if (t.getDetails() != null && !t.getDetails().isEmpty()) {
            StockTransactionDetail firstItem = t.getDetails().get(0);
            dto.setProductId(firstItem.getProduct().getProductId());
            dto.setProductCode(firstItem.getProduct().getProductCode());
            dto.setProductName(firstItem.getProduct().getProductName());
            dto.setProductUnit(firstItem.getProduct().getUnit());
            dto.setQuantity(firstItem.getQuantity());
        }

        if (t.getFromBranch() != null) {
            dto.setFromBranchId(t.getFromBranch().getBranchId());
            dto.setFromBranchName(t.getFromBranch().getBranchName());
        }

        if (t.getToBranch() != null) {
            dto.setToBranchId(t.getToBranch().getBranchId());
            dto.setToBranchName(t.getToBranch().getBranchName());
        }

        if (t.getCreatedBy() != null) {
            dto.setCreatedById(t.getCreatedBy().getUserId());
            dto.setCreatedByName(t.getCreatedBy().getFullName());
        }

        return dto;
    }
}