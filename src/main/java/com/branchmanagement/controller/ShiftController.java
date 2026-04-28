package com.branchmanagement.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.ShiftRequest;
import com.branchmanagement.dto.ShiftResponse;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.entity.Shift;
import com.branchmanagement.entity.User;
import com.branchmanagement.entity.UserShift;
import com.branchmanagement.repository.BranchRepository;
import com.branchmanagement.repository.ShiftRepository;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.repository.UserShiftRepository;
import com.branchmanagement.service.ShiftService;

@RestController
@RequestMapping("/api/manager/shifts")
public class ShiftController {

    private final ShiftRepository shiftRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final UserShiftRepository userShiftRepository;
    private final ShiftService shiftService; // Đã thêm ShiftService

    public ShiftController(ShiftRepository shiftRepository, BranchRepository branchRepository,
            UserRepository userRepository, UserShiftRepository userShiftRepository,
            ShiftService shiftService) { // Tiêm phụ thuộc (Inject) ShiftService vào
        this.shiftRepository = shiftRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.userShiftRepository = userShiftRepository;
        this.shiftService = shiftService;
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getShiftsByBranch(
            @PathVariable Integer branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        try {
            List<Shift> shifts;
            if (fromDate != null && toDate != null) {
                shifts = shiftRepository.findWithUsersByBranchAndDateRange(branchId, fromDate, toDate);
            } else {
                shifts = shiftRepository.findByBranch_BranchIdOrderByDateAscStartTimeAsc(branchId);
            }

            List<ShiftResponse> responses = shifts.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách ca làm việc thành công", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShiftResponse>> createShift(
            @RequestBody ShiftRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.branchmanagement.security.CustomUserDetails currentUser) {
        try {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));

            Shift shift = new Shift();
            shift.setBranch(branch);
            shift.setShiftName(request.getShiftName());
            shift.setShiftType(request.getShiftType());
            shift.setStartTime(request.getStartTime());
            shift.setEndTime(request.getEndTime());
            shift.setDate(request.getDate());
            shift.setStatus("active");
            
            // ✅ ĐÃ SỬA: Lấy ID người tạo từ JWT thay vì hardcode = 1
            Integer creatorId = request.getCreatedBy() != null ? request.getCreatedBy() : currentUser.getUserId();
            
            // Tìm đối tượng User từ Database
            User creator = userRepository.findById(creatorId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người tạo (User ID: " + creatorId + ")"));
            
            // Gán đối tượng User vào Shift
            shift.setCreatedBy(creator);

            Shift saved = shiftRepository.save(shift);
            return ResponseEntity.ok(ApiResponse.ok("Tạo ca làm việc thành công", toResponse(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{shiftId}/assign/{userId}")
    public ResponseEntity<ApiResponse<Void>> assignUser(@PathVariable Integer shiftId, @PathVariable Integer userId) {
        try {
            if (userShiftRepository.existsByShift_ShiftIdAndUser_UserId(shiftId, userId)) {
                throw new RuntimeException("Nhân viên này đã được phân vào ca này rồi");
            }

            Shift shift = shiftRepository.findById(shiftId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            UserShift us = new UserShift();
            us.setShift(shift);
            us.setUser(user);
            us.setStatus("assigned");
            userShiftRepository.save(us);
            return ResponseEntity.ok(ApiResponse.ok("Phân ca thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{shiftId}/assign/{userShiftId}")
    public ResponseEntity<ApiResponse<Void>> removeAssign(
            @PathVariable Integer shiftId,
            @PathVariable Integer userShiftId) {
        try {
            // Kiểm tra tồn tại trước khi xóa
            if (!userShiftRepository.existsById(userShiftId)) {
                throw new RuntimeException("Không tìm thấy thông tin phân ca");
            }
            userShiftRepository.deleteById(userShiftId);
            return ResponseEntity.ok(ApiResponse.ok("Đã hủy phân ca thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // CÁC API MỚI CHO VIỆC SỬA VÀ XÓA CA LÀM VIỆC
    // ==========================================

    /**
     * SỬA CA: Thay đổi nhân viên đã được gán (Update UserShift)
     */
    @PutMapping("/user-shift/{userShiftId}")
    public ResponseEntity<ApiResponse<Void>> updateUserInShift(
            @PathVariable Integer userShiftId,
            @RequestBody Map<String, Integer> payload) {
        try {
            Integer newUserId = payload.get("userId");
            if (newUserId == null) {
                throw new RuntimeException("Thiếu ID nhân viên mới (userId)");
            }
            shiftService.updateUserInShift(userShiftId, newUserId);
            return ResponseEntity.ok(ApiResponse.ok("Thay đổi nhân viên thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * XÓA CA: Xóa toàn bộ ca làm việc
     */
    @DeleteMapping("/{shiftId}")
    public ResponseEntity<ApiResponse<Void>> deleteShift(@PathVariable Integer shiftId) {
        try {
            shiftService.deleteShift(shiftId);
            return ResponseEntity.ok(ApiResponse.ok("Đã xóa ca làm việc thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ===== HELPER =====
    private ShiftResponse toResponse(Shift s) {
        List<ShiftResponse.AssignedUserDTO> users = s.getUserShifts() == null ? java.util.Collections.emptyList()
                : s.getUserShifts().stream()
                        .map(us -> new ShiftResponse.AssignedUserDTO(
                                us.getUser().getUserId(),
                                us.getUser().getFullName(), 
                                // Sử dụng .name() để chuyển Enum sang String
                                us.getUser().getRole() != null ? us.getUser().getRole().name() : "N/A", 
                                us.getStatus(),
                                us.getUserShiftId()))
                        .collect(Collectors.toList());

        return new ShiftResponse()
                .setShiftId(s.getShiftId())
                .setBranchId(s.getBranch().getBranchId())
                .setBranchName(s.getBranch().getBranchName())
                .setShiftName(s.getShiftName())
                .setShiftType(s.getShiftType())
                .setStartTime(s.getStartTime())
                .setEndTime(s.getEndTime())
                .setDate(s.getDate())
                .setStatus(s.getStatus())
                .setAssignedCount(users.size())
                .setAssignedUsers(users);
    }
}