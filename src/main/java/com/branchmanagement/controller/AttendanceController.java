package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.AttendanceResponse;
import com.branchmanagement.dto.RejectRequest;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/manager/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // Nhân viên thực hiện check-in với ảnh Selfie và GPS
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<Void>> checkIn(
            @RequestParam("branchId") Integer branchId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("photo") MultipartFile photo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            attendanceService.checkIn(userDetails.getUserId(), branchId, latitude, longitude, photo);
            return ResponseEntity.ok(ApiResponse.ok("Chấm công thành công!", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAttendances(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        java.time.LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        java.time.LocalDateTime end = toDate != null ? toDate.atTime(java.time.LocalTime.MAX) : null;

        Page<AttendanceResponse> data = attendanceService.getAttendances(userDetails, status, start, end, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Thành công", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceDetail(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        AttendanceResponse data = attendanceService.getAttendanceDetail(id, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Thành công", data));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveAttendance(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        attendanceService.approveAttendance(id, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Duyệt chấm công thành công", null));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectAttendance(
            @PathVariable Integer id,
            @Valid @RequestBody RejectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        attendanceService.rejectAttendance(id, userDetails, request.getReason());
        return ResponseEntity.ok(ApiResponse.ok("Đã từ chối chấm công", null));
    }
    
    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<Void>> checkOut(
            @RequestParam("branchId") Integer branchId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            attendanceService.checkOut(userDetails.getUserId(), branchId, latitude, longitude, photo);
            return ResponseEntity.ok(ApiResponse.ok("Check-out thành công!", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}