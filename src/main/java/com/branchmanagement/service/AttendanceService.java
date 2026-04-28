package com.branchmanagement.service;

import com.branchmanagement.dto.AttendanceResponse;
import com.branchmanagement.security.CustomUserDetails;
import org.springframework.data.domain.Page; // ✅ ĐÂY LÀ DÒNG BẠN ĐANG THIẾU
import org.springframework.web.multipart.MultipartFile;
public interface AttendanceService {
    
    // Hàm lấy danh sách chấm công
    Page<AttendanceResponse> getAttendances(CustomUserDetails userDetails, String status, 
                                            java.time.LocalDateTime startDate, 
                                            java.time.LocalDateTime endDate, 
                                            int page, int size);
    
    // Hàm lấy chi tiết
    AttendanceResponse getAttendanceDetail(Integer id, CustomUserDetails userDetails);
    
    // Hàm duyệt/từ chối
    void approveAttendance(Integer id, CustomUserDetails userDetails);
    void rejectAttendance(Integer id, CustomUserDetails userDetails, String reason);
    
    // ✅ Hàm check-in đã được thêm MultipartFile
    void checkIn(Integer userId, Integer branchId, Double userLat, Double userLon, MultipartFile photo);

    void checkOut(Integer userId, Integer branchId, Double userLat, Double userLon, MultipartFile photo);
}