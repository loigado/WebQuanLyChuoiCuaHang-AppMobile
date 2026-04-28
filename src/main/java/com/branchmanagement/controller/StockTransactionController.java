package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.entity.StockTransaction;
import com.branchmanagement.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/manager/StockRequest") // ✅ Khớp với file Vue
@RequiredArgsConstructor
public class StockTransactionController {

    private final StockTransactionRepository transactionRepository;

    // Lấy danh sách phiếu PENDING để duyệt
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingRequests() {
        List<StockTransaction> transactions = transactionRepository.findByStatusIgnoreCase("PENDING");
        List<Map<String, Object>> response = new ArrayList<>();

        for (StockTransaction st : transactions) {
            Map<String, Object> map = new HashMap<>();
            map.put("transactionId", st.getTransactionId());
            map.put("transactionCode", st.getTransactionCode());
            map.put("transactionType", st.getTransactionType());
            map.put("fromBranchName", st.getFromBranch() != null ? st.getFromBranch().getBranchName() : null);
            map.put("toBranchName", st.getToBranch() != null ? st.getToBranch().getBranchName() : null);
            map.put("reason", st.getReason());
            map.put("status", st.getStatus());
            
            // Map danh sách chi tiết hàng hóa (để dùng cho phần expand trong Vue)
            List<Map<String, Object>> details = new ArrayList<>();
            st.getDetails().forEach(d -> {
                Map<String, Object> item = new HashMap<>();
                // ✅ Đã sửa: dùng .put() thay vì .add()
                item.put("productCode", d.getProduct().getProductCode());
                item.put("productName", d.getProduct().getProductName());
                item.put("quantity", d.getQuantity());
                item.put("productUnit", d.getProduct().getUnit());
                details.add(item); // List thì vẫn dùng .add() bình thường
            });
            map.put("details", details);
            
            response.add(map);
        }
        return ResponseEntity.ok(ApiResponse.ok("Thành công", response));
    }

    // Xử lý Duyệt/Từ chối
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Integer id, 
            @RequestBody Map<String, String> request) {
        
        StockTransaction st = transactionRepository.findById(id).orElseThrow();
        st.setStatus(request.get("status").toUpperCase());
        st.setNote(request.get("note"));
        
        // Logic cộng/trừ tồn kho thật sẽ viết thêm ở đây sau khi duyệt
        
        transactionRepository.save(st);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", null));
    }
}