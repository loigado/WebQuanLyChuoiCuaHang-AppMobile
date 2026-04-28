package com.branchmanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.branchmanagement.dto.TransferRequestDto;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.entity.Product;
import com.branchmanagement.entity.Stock;
import com.branchmanagement.entity.StockTransaction;
import com.branchmanagement.entity.StockTransactionDetail; // ✅ Thêm Detail
import com.branchmanagement.entity.TransferRequest; 
import com.branchmanagement.entity.User;
import com.branchmanagement.repository.*;
import com.branchmanagement.service.NotificationService;

@Service
@Transactional
public class TransferServiceImpl {

    private final TransferRequestRepository transferReqRepo;
    private final StockTransactionRepository transactionRepo;
    private final StockRepository stockRepo;
    private final ProductRepository productRepo;
    private final BranchRepository branchRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public TransferServiceImpl(TransferRequestRepository transferReqRepo, StockTransactionRepository transactionRepo,
            StockRepository stockRepo, ProductRepository productRepo, BranchRepository branchRepo, UserRepository userRepo,
            NotificationService notificationService) {
        this.transferReqRepo = transferReqRepo;
        this.transactionRepo = transactionRepo;
        this.stockRepo = stockRepo;
        this.productRepo = productRepo;
        this.branchRepo = branchRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    public TransferRequest createRequest(TransferRequestDto dto, Integer requesterId) {
        Product product = productRepo.findById(dto.getProductId()).orElseThrow();
        Branch fromBranch = branchRepo.findById(dto.getFromBranchId()).orElseThrow();
        Branch toBranch = branchRepo.findById(dto.getToBranchId()).orElseThrow();
        User requester = userRepo.findById(requesterId).orElseThrow();

        TransferRequest req = new TransferRequest();
        req.setRequestCode("REQ-DC-" + System.currentTimeMillis());
        req.setProduct(product);
        req.setFromBranch(fromBranch);
        req.setToBranch(toBranch);
        req.setQuantity(dto.getQuantity());
        req.setReason(dto.getReason());
        req.setPriority(dto.getPriority()); 
        req.setExpectedDate(dto.getExpectedDate());
        req.setStatus("Pending"); 
        req.setCreatedBy(requester);
        req.setCreatedAt(LocalDateTime.now());

        TransferRequest savedReq = transferReqRepo.save(req);

        List<User> targetManagers = userRepo.findManagersByBranch(toBranch.getBranchId());
        if (!targetManagers.isEmpty()) {
            notificationService.sendAlert(targetManagers.get(0), "transfer_pending", "Yêu cầu điều chuyển mới", "Chi nhánh " + fromBranch.getBranchName() + " đang xin " + dto.getQuantity() + " sản phẩm.", "High", "/manager/stock/transfer/" + savedReq.getRequestId());
        }
        return savedReq;
    }

    @Transactional
    public TransferRequest approveRequest(Integer requestId, Integer approverId) {
        TransferRequest req = transferReqRepo.findById(requestId).orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
        if (!"Pending".equals(req.getStatus())) throw new RuntimeException("Chỉ duyệt yêu cầu Pending");

        Stock sourceStock = stockRepo.findByProduct_ProductIdAndBranch_BranchId(req.getProduct().getProductId(), req.getFromBranch().getBranchId()).orElseThrow(() -> new RuntimeException("Sản phẩm không có trong kho nguồn"));
        BigDecimal available = sourceStock.getQuantity().subtract(sourceStock.getReservedQuantity());
        if (req.getQuantity().compareTo(available) > 0) throw new RuntimeException("Tồn kho khả dụng không đủ");

        sourceStock.setReservedQuantity(sourceStock.getReservedQuantity().add(req.getQuantity()));
        stockRepo.save(sourceStock);

        User approver = userRepo.findById(approverId).orElseThrow();

        // ✅ ĐÃ SỬA: Tạo phiếu xuất bằng Master-Detail
        StockTransaction outTx = new StockTransaction();
        outTx.setTransactionCode("OUT-" + req.getRequestCode());
        outTx.setTransactionType("transfer_out");
        outTx.setTransferRequest(req); 
        outTx.setFromBranch(req.getFromBranch());
        outTx.setToBranch(req.getToBranch());
        outTx.setStatus("Processing"); 
        outTx.setCreatedBy(approver);
        
        StockTransactionDetail outDetail = new StockTransactionDetail();
        outDetail.setProduct(req.getProduct());
        outDetail.setQuantity(req.getQuantity());
        outTx.addDetail(outDetail);
        transactionRepo.save(outTx);

        // ✅ ĐÃ SỬA: Tạo phiếu nhập bằng Master-Detail
        StockTransaction inTx = new StockTransaction();
        inTx.setTransactionCode("IN-" + req.getRequestCode());
        inTx.setTransactionType("transfer_in");
        inTx.setTransferRequest(req); 
        inTx.setFromBranch(req.getFromBranch());
        inTx.setToBranch(req.getToBranch());
        inTx.setStatus("Processing"); 
        inTx.setCreatedBy(approver);
        
        StockTransactionDetail inDetail = new StockTransactionDetail();
        inDetail.setProduct(req.getProduct());
        inDetail.setQuantity(req.getQuantity());
        inTx.addDetail(inDetail);
        transactionRepo.save(inTx);

        req.setStatus("Approved");
        req.setUpdatedAt(LocalDateTime.now());
        TransferRequest savedReq = transferReqRepo.save(req);

        List<User> sourceManagers = userRepo.findManagersByBranch(req.getFromBranch().getBranchId());
        if (!sourceManagers.isEmpty()) {
            notificationService.sendAlert(sourceManagers.get(0), "transfer_approved", "Yêu cầu điều chuyển ĐÃ ĐƯỢC DUYỆT", "Chi nhánh đích đã đồng ý xuất hàng.", "Normal", "/manager/stock/transfer/" + savedReq.getRequestId());
        }
        return savedReq;
    }

    @Transactional
    public TransferRequest confirmShipping(Integer requestId) {
        TransferRequest req = transferReqRepo.findById(requestId).orElseThrow();
        if (!"Approved".equals(req.getStatus())) throw new RuntimeException("Chỉ xuất hàng cho yêu cầu đã duyệt");

        // ✅ ĐÃ SỬA: Chỉ cập nhật trạng thái phiếu xuất, KHÔNG trừ kho ở đây
        // Việc trừ kho thực tế sẽ do StockApprovalServiceImpl xử lý khi Admin duyệt phiếu transfer_out
        StockTransaction outTx = transactionRepo.findByTransferRequest_RequestIdAndTransactionType(requestId, "transfer_out").orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất"));
        outTx.setStatus("pending"); // Chuyển sang pending để Admin duyệt
        transactionRepo.save(outTx);

        req.setStatus("Waiting_Admin");
        return transferReqRepo.save(req);
    }

    @Transactional
    public TransferRequest rejectRequest(Integer requestId, Integer rejectorId, String reason) {
        TransferRequest req = transferReqRepo.findById(requestId).orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
        if (!"Pending".equals(req.getStatus())) throw new RuntimeException("Chỉ từ chối yêu cầu Pending");

        req.setStatus("Rejected");
        req.setReason(req.getReason() + " | LÝ DO TỪ CHỐI: " + reason); 
        req.setUpdatedAt(LocalDateTime.now());
        TransferRequest savedReq = transferReqRepo.save(req);

        List<User> sourceManagers = userRepo.findManagersByBranch(req.getFromBranch().getBranchId());
        if (!sourceManagers.isEmpty()) {
            notificationService.sendAlert(sourceManagers.get(0), "transfer_rejected", "Yêu cầu điều chuyển BỊ TỪ CHỐI", "Lý do: " + reason, "Urgent", "/manager/stock/transfer/" + savedReq.getRequestId());
        }
        return savedReq;
    }

    @Transactional
    public TransferRequest confirmReceipt(Integer requestId) {
        TransferRequest req = transferReqRepo.findById(requestId).orElseThrow();
        if (!"Shipping".equals(req.getStatus())) throw new RuntimeException("Chỉ nhận hàng khi trạng thái Shipping");

        StockTransaction inTx = transactionRepo.findByTransferRequest_RequestIdAndTransactionType(requestId, "transfer_in").orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        inTx.setStatus("Completed");
        transactionRepo.save(inTx);

        Stock destStock = stockRepo.findByProduct_ProductIdAndBranch_BranchId(req.getProduct().getProductId(), req.getToBranch().getBranchId()).orElseGet(() -> { 
            Stock s = new Stock();
            s.setProduct(req.getProduct());
            s.setBranch(req.getToBranch());
            s.setQuantity(BigDecimal.ZERO);
            s.setReservedQuantity(BigDecimal.ZERO);
            return s;
        });

        destStock.setQuantity(destStock.getQuantity().add(req.getQuantity()));
        stockRepo.save(destStock);
        req.setStatus("Completed");
        return transferReqRepo.save(req);
    }

    @Transactional
    public TransferRequest cancelRequest(Integer requestId, String reason) {
        TransferRequest req = transferReqRepo.findById(requestId).orElseThrow();
        if ("Shipping".equals(req.getStatus()) || "Completed".equals(req.getStatus())) throw new RuntimeException("Không thể hủy khi đang/đã giao");

        if ("Approved".equals(req.getStatus())) {
            Stock sourceStock = stockRepo.findByProduct_ProductIdAndBranch_BranchId(req.getProduct().getProductId(), req.getFromBranch().getBranchId()).orElseThrow();
            sourceStock.setReservedQuantity(sourceStock.getReservedQuantity().subtract(req.getQuantity()));
            stockRepo.save(sourceStock);
        }
        req.setStatus("Cancelled");
        req.setReason(req.getReason() + " | LÝ DO HỦY: " + reason);
        return transferReqRepo.save(req);
    }
    
    @Transactional
    public TransferRequest receiveTransfer(Integer requestId, Integer targetManagerId) {
        TransferRequest req = transferReqRepo.findById(requestId).orElseThrow(() -> new RuntimeException("Không thấy yêu cầu"));
        if (!"Shipping".equalsIgnoreCase(req.getStatus())) throw new RuntimeException("Chưa được giao đi");

        // ✅ ĐÃ SỬA: Không trừ kho nguồn ở đây nữa (đã được trừ khi Admin duyệt phiếu transfer_out)
        // Chỉ cộng kho đích
        Stock destStock = stockRepo.findByProduct_ProductIdAndBranch_BranchId(req.getProduct().getProductId(), req.getToBranch().getBranchId()).orElseGet(() -> {
            Stock s = new Stock();
            s.setProduct(req.getProduct());
            s.setBranch(req.getToBranch());
            s.setQuantity(BigDecimal.ZERO);
            s.setReservedQuantity(BigDecimal.ZERO);
            return s;
        });
        destStock.setQuantity(destStock.getQuantity().add(req.getQuantity()));
        stockRepo.save(destStock);

        User receiver = userRepo.findById(targetManagerId).orElse(null);

        // Cập nhật phiếu nhập đã được tạo từ trước
        StockTransaction inTx = transactionRepo.findByTransferRequest_RequestIdAndTransactionType(requestId, "transfer_in").orElseGet(() -> {
            // Nếu chưa có phiếu nhập, tạo mới
            StockTransaction newInTx = new StockTransaction();
            newInTx.setTransactionCode("DCH-IN-" + System.currentTimeMillis());
            newInTx.setTransactionType("transfer_in");
            newInTx.setFromBranch(req.getFromBranch());
            newInTx.setToBranch(req.getToBranch());
            newInTx.setCreatedBy(receiver);
            
            StockTransactionDetail inDetail = new StockTransactionDetail();
            inDetail.setProduct(req.getProduct());
            inDetail.setQuantity(req.getQuantity());
            newInTx.addDetail(inDetail);
            return newInTx;
        });
        inTx.setStatus("approved");
        inTx.setReason("Nhận hàng điều chuyển từ " + req.getFromBranch().getBranchName());
        inTx.setApprovedBy(receiver);
        inTx.setApprovedAt(LocalDateTime.now());
        transactionRepo.save(inTx);

        req.setStatus("Completed");
        List<User> sourceManagers = userRepo.findManagersByBranch(req.getFromBranch().getBranchId());
        if (!sourceManagers.isEmpty()) {
            notificationService.sendAlert(sourceManagers.get(0), "transfer_completed", "Điều chuyển thành công", "Chi nhánh " + req.getToBranch().getBranchName() + " đã nhận được hàng.", "Normal", "/manager/stock/transfer/history");
        }
        return transferReqRepo.save(req);
    }
}