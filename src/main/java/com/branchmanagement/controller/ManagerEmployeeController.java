package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.CheckInRequest;
import com.branchmanagement.entity.Attendance;
import com.branchmanagement.entity.User;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.MobileEmployeeService;
import com.branchmanagement.service.UserService;
import com.branchmanagement.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager/employees")
public class ManagerEmployeeController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final MobileEmployeeService mobileEmployeeService;

    // ✅ ĐÃ SỬA LỖI: Thêm MobileEmployeeService vào Constructor để Spring Boot tự động Inject
    public ManagerEmployeeController(UserService userService, UserRepository userRepository, MobileEmployeeService mobileEmployeeService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.mobileEmployeeService = mobileEmployeeService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<?>> getBranchEmployees(
            @RequestParam(required = false) Integer branchId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("Vui lòng đăng nhập"));
            }

            Integer targetBranchId = branchId;

            if (targetBranchId == null) {
                User currentUser = userRepository.findById(userDetails.getUserId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại"));

                if (currentUser.getBranch() == null) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Tài khoản (Admin) chưa có chi nhánh. Vui lòng truyền branchId."));
                }
                targetBranchId = currentUser.getBranch().getBranchId();
            }

            List<User> employees = userService.getUsersByBranch(targetBranchId);
            
            List<User> activeEmployees = employees.stream()
                    .filter(u -> "active".equalsIgnoreCase(u.getStatus()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách thành công", activeEmployees));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi 500: " + e.getMessage()));
        }
    }
    
    // ✅ API Check-in dành cho Mobile (Tạm thời để trong Manager, sau này có thể tách riêng)
    @PostMapping("/checkin")
    public ResponseEntity<?> checkInWithGPS(@RequestBody CheckInRequest request) {
        try {
            Attendance result = mobileEmployeeService.processCheckIn(request);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "type", result.getStatus(),
                    "message", result.getStatus().equals("normal") ? "Chấm công hợp lệ!" : "Chấm công ngoại lệ, chờ duyệt!"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}