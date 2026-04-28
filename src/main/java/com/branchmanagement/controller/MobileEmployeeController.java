package com.branchmanagement.controller;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.UserShiftDTO;
import com.branchmanagement.dto.LeaveRequestResponse;
import com.branchmanagement.dto.AttendanceResponse;
import com.branchmanagement.service.MobileEmployeeService;
import com.branchmanagement.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Mobile-specific API controller for employee features.
 * Handles shift viewing, leave requests, and real-time attendance tracking.
 */
@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
public class MobileEmployeeController {

    private final MobileEmployeeService mobileEmployeeService;
    private final AttendanceService attendanceService;

    /**
     * Retrieve assigned shifts for the current user.
     * 
     * @param userId The ID of the employee
     * @param startDate Optional filter start date (ISO format)
     * @param endDate Optional filter end date (ISO format)
     */
    @GetMapping("/my-shifts")
    public ResponseEntity<ApiResponse<List<UserShiftDTO>>> getMyShifts(
            @RequestParam Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<UserShiftDTO> shifts = mobileEmployeeService.getMyShifts(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách ca làm việc thành công", shifts));
    }

    /**
     * List all leave requests submitted by the employee.
     */
    @GetMapping("/leave-requests")
    public ResponseEntity<ApiResponse<List<?>>> getLeaveRequests(@RequestParam Integer userId) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách đơn xin nghỉ thành công", mobileEmployeeService.getMyLeaveRequests(userId)));
    }

    /**
     * Submit a new leave request for approval.
     */
    @PostMapping("/leave-requests")
    public ResponseEntity<ApiResponse<Void>> submitLeaveRequest(
            @RequestParam Integer userId, 
            @RequestBody LeaveRequestResponse request) {
        mobileEmployeeService.submitLeaveRequest(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Gửi đơn xin nghỉ thành công, vui lòng chờ quản lý duyệt!", null));
    }

    /**
     * Record a check-in event with GPS and photo verification.
     */
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<Void>> checkIn(
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "branchId", required = false) Integer branchId,
            @RequestParam("lat") Double latitude,
            @RequestParam("lng") Double longitude,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        try {
            attendanceService.checkIn(userId, branchId, latitude, longitude, photo);
            return ResponseEntity.ok(ApiResponse.ok("Chấm công vào ca thành công!", null));
        } catch (Exception e) {
            // Trả về 200 OK nhưng success = false để hiện được message
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Record a check-out event.
     */
    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<Void>> checkOut(
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "branchId", required = false) Integer branchId,
            @RequestParam("lat") Double latitude,
            @RequestParam("lng") Double longitude,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        try {
            attendanceService.checkOut(userId, branchId, latitude, longitude, photo);
            return ResponseEntity.ok(ApiResponse.ok("Kết thúc ca làm việc thành công!", null));
        } catch (Exception e) {
            // Trả về 200 OK nhưng success = false để hiện được message
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Retrieve the attendance history for the employee.
     */
    @GetMapping("/attendance-history")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceHistory(@RequestParam Integer userId) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy lịch sử chấm công thành công", mobileEmployeeService.getMyAttendanceHistory(userId)));
    }



    /**
     * View current inventory of the branch.
     */
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<com.branchmanagement.dto.MobileStockResponse>>> getBranchInventory(@RequestParam Integer userId) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy dữ liệu tồn kho chi nhánh thành công", mobileEmployeeService.getBranchInventory(userId)));
    }

    /**
     * Get list of pending stock requests for approval.
     */
    @GetMapping(value = "/pending-requests", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ApiResponse<List<com.branchmanagement.dto.StockTransactionResponse>>> getPendingRequests(@RequestParam Integer userId) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách phiếu chờ duyệt thành công", mobileEmployeeService.getMyPendingRequests(userId)));
    }

    /**
     * Approve or Reject a stock request.
     */
    @PostMapping(value = "/approve-request", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ApiResponse<com.branchmanagement.dto.StockTransactionResponse>> processRequest(
            @RequestParam Integer userId,
            @RequestParam Integer transactionId,
            @RequestParam String decision, // "APPROVE" or "REJECT"
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(ApiResponse.ok("Xử lý phiếu thành công", mobileEmployeeService.processRequestFromMobile(userId, transactionId, decision, note)));
    }

    /**
     * Get estimated payroll for the current month.
     */
    @GetMapping(value = "/payroll", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ApiResponse<com.branchmanagement.dto.MobilePayrollResponse>> getMyPayroll(@RequestParam Integer userId) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy bảng lương dự tính thành công", mobileEmployeeService.getMyPayrollSummary(userId)));
    }

    /**
     * Get 5 latest internal announcements.
     */
    @GetMapping(value = "/announcements", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ApiResponse<List<com.branchmanagement.entity.Announcement>>> getAnnouncements() {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách thông báo thành công", mobileEmployeeService.getLatestAnnouncements()));
    }
}
