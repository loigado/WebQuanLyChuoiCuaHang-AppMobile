package com.branchmanagement.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.BranchResponse;
import com.branchmanagement.service.BranchService;

@RestController
@RequestMapping("/api/manager/branches") // ✅ Đường dẫn gốc chuẩn cho Manager
public class ManagerBranchController {

    private final BranchService branchService;

    public ManagerBranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getActiveBranchesForManager() {
        List<BranchResponse> branches = branchService.getActiveBranches();
        // ✅ Dùng ApiResponse.ok đúng chuẩn project của bạn
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách chi nhánh thành công", branches));
    }
}