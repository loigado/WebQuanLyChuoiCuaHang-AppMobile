package com.branchmanagement.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.CameraRequest;
import com.branchmanagement.dto.CameraResponse;
import com.branchmanagement.service.CameraService;
import jakarta.validation.Valid;

@RestController
public class CameraController {

    private final CameraService cameraService;

    public CameraController(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    // ================= VAI TRÒ: ADMIN (Quản lý thiết bị) =================
    // Admin có quyền CRUD (Thêm, sửa, xóa) để thiết lập hệ thống camera toàn chuỗi

    @GetMapping("/api/admin/cameras")
    public ResponseEntity<ApiResponse<List<CameraResponse>>> adminGetAll() {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách camera hệ thống thành open", cameraService.getAllCameras()));
    }

    @PostMapping("/api/admin/cameras")
    public ResponseEntity<ApiResponse<CameraResponse>> adminCreate(@Valid @RequestBody CameraRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cấu hình camera mới thành công", cameraService.createCamera(request)));
    }

    @DeleteMapping("/api/admin/cameras/{id}")
    public ResponseEntity<ApiResponse<Void>> adminDelete(@PathVariable Integer id) {
        cameraService.deleteCamera(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã gỡ bỏ camera khỏi hệ thống", null));
    }

    // ================= VAI TRÒ: MANAGER (Giám sát chi nhánh) =================
    // Manager chỉ xem được camera tại chi nhánh mình đang quản lý

    @GetMapping("/api/manager/cameras/my-branch/{branchId}")
    public ResponseEntity<ApiResponse<List<CameraResponse>>> managerGetByBranch(@PathVariable Integer branchId) {
        // Sau này khi có JWT, branchId sẽ lấy từ Token của Manager để đảm bảo bảo mật
        List<CameraResponse> cameras = cameraService.getCamerasByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách camera chi nhánh thành công", cameras));
    }

    /**
     * Endpoint lấy luồng Live cho Player (Dùng chung cho cả 2 vai trò)
     */
    @GetMapping({"/api/admin/cameras/{id}/live", "/api/manager/cameras/{id}/live"})
    public ResponseEntity<ApiResponse<String>> getLiveUrl(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy HLS URL thành công", cameraService.getHlsUrl(id)));
    }
}