package com.branchmanagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.entity.User;
import com.branchmanagement.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==========================================
    // 1. NHÓM API LẤY DỮ LIỆU (GET)
    // ==========================================

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách thành công", users));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByBranch(@PathVariable Integer branchId) {
        List<User> users = userService.getUsersByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách theo chi nhánh thành công", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Integer id) {
        // ✅ ĐÃ SỬA: Gọi findById trực tiếp thay vì load toàn bộ user rồi filter
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok("Thành công", user));
    }


    // ==========================================
    // 2. NHÓM API THÊM VÀ SỬA (POST / PUT)
    // ==========================================

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Tạo tài khoản thành công", createdUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Integer id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật tài khoản thành công", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // 3. NHÓM API KHÓA VÀ MỞ KHÓA TÀI KHOẢN (PATCH)
    // ==========================================

    @PatchMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Integer id) {
        try {
            userService.lockUser(id);
            return ResponseEntity.ok(ApiResponse.ok("Khóa tài khoản thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Integer id) {
        try {
            userService.unlockUser(id);
            return ResponseEntity.ok(ApiResponse.ok("Mở khóa tài khoản thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ✅ ĐÃ THÊM: Reset mật khẩu (UC 1.1)
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, String> payload) {
        try {
            String newPassword = payload.get("newPassword");
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu phải có ít nhất 6 ký tự"));
            }
            userService.resetPassword(id, newPassword);
            return ResponseEntity.ok(ApiResponse.ok("Reset mật khẩu thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}