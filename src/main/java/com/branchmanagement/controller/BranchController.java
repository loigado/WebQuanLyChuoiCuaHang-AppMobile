package com.branchmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.branchmanagement.annotation.AuditAction;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.BranchResponse;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.service.BranchService;
@RestController
@RequestMapping("/api/admin/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    /**
     * Creates a new branch.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponse>> create(@RequestBody Branch request) {
        Branch created = branchService.createBranch(request);
        BranchResponse response = branchService.getBranchById(created.getBranchId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo chi nhánh thành công", response));
    }

    /**
     * Updates an existing branch's information.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> update(@PathVariable Integer id, @RequestBody Branch request) {
        branchService.updateBranch(id, request);
        BranchResponse updated = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật chi nhánh thành công", updated));
    }

    /**
     * Retrieves a list of all branches for administrative overview.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches() {
        try {
            List<BranchResponse> branches = branchService.getAllBranches();
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách chi nhánh thành công", branches));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách chi nhánh: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a list of active branches, often used for dropdown selections.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getActiveBranches() {
        try {
            List<BranchResponse> branches = branchService.getActiveBranches();
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách chi nhánh hoạt động thành công", branches));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * Retrieves detailed information for a specific branch by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Integer id) {
        try {
            BranchResponse branch = branchService.getBranchById(id);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin chi nhánh thành công", branch));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Toggles the active/inactive status of a branch.
     * Actions are audited for security tracking.
     */
    @PutMapping("/{id}/toggle")
    @AuditAction(action = "TOGGLE_BRANCH_STATUS") 
    public ResponseEntity<ApiResponse<Void>> toggleBranchStatus(@PathVariable Integer id) {
        try {
            branchService.toggleStatus(id);
            return ResponseEntity.ok(ApiResponse.ok("Đổi trạng thái chi nhánh thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Retrieves detailed information for a specific branch by its unique code.
     */
    @GetMapping("/code/{branchCode}")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchByCode(@PathVariable String branchCode) {
        try {
            BranchResponse branch = branchService.getBranchByCode(branchCode);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin chi nhánh thành công", branch));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}