package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.repository.BranchRepository;
import com.branchmanagement.repository.InventoryRequestRepository; // ✅ Đã thêm Import
import com.branchmanagement.repository.StockTransactionRepository;
import com.branchmanagement.repository.TransferRequestRepository;
import com.branchmanagement.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final BranchRepository branchRepository;
    private final StockTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    // ✅ BƯỚC 1: Đã thêm khai báo thuộc tính bị thiếu
    private final InventoryRequestRepository inventoryRequestRepository; 
    private final TransferRequestRepository transferRequestRepository;

    // ✅ BƯỚC 2: Cập nhật Constructor đầy đủ các tham số
    public DashboardController(BranchRepository branchRepository, 
                               StockTransactionRepository transactionRepository,
                               InventoryRequestRepository inventoryRequestRepository,
                               TransferRequestRepository transferRequestRepository,
                               UserRepository userRepository) {
        this.branchRepository = branchRepository;
        this.transactionRepository = transactionRepository;
        this.inventoryRequestRepository = inventoryRequestRepository;
        this.transferRequestRepository = transferRequestRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy dữ liệu tổng hợp cho bảng điều khiển
     * Trả về: Tổng chi nhánh, Tổng nhân sự và Số phiếu đang chờ duyệt
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer branchId) {
        Map<String, Object> data = new HashMap<>();
        
        if (branchId != null) {
            // Stats cho Manager chi nhánh cụ thể
            data.put("totalBranches", 1); 
            data.put("totalUsers", userRepository.countByBranch_BranchId(branchId));
            
            // CHỈ ĐẾM NHỮNG PHIẾU THỰC SỰ CẦN QUẢN LÝ BẤM NÚT:
            // 1. Phiếu điều chuyển ĐẾN đang ở trạng thái 'Shipping' (Cần bấm Xác nhận nhận)
            long needReceive = transferRequestRepository.countByToBranch_BranchIdAndStatusIgnoreCase(branchId, "Shipping");
            
            // 2. Phiếu điều chuyển ĐI đang ở trạng thái 'Approved' (Cần bấm Xuất hàng)
            long needShip = transferRequestRepository.countByFromBranch_BranchIdAndStatusIgnoreCase(branchId, "Approved");
            
            data.put("totalInventory", needReceive + needShip);
        } else {
            // Stats tổng quan cho Admin
            data.put("totalBranches", branchRepository.count());
            data.put("totalUsers", userRepository.count());
            
            long pendingRequests = inventoryRequestRepository.countByStatusIgnoreCase("pending");
            // Admin cũng quan tâm các phiếu điều chuyển cần duyệt (transactionType = TRANSFER, status = Pending)
            // Giả sử có trạng thái Pending cho Transfer
            data.put("totalInventory", pendingRequests);
        }
        
        return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê thành công", data));
    }
}