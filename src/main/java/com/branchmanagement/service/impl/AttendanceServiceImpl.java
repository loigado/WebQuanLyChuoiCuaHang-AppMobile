package com.branchmanagement.service.impl;

import com.branchmanagement.dto.AttendanceResponse;
import com.branchmanagement.entity.Attendance;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.enums.AttendanceStatus;
import com.branchmanagement.exception.AppException;
import com.branchmanagement.repository.AttendanceRepository;
import com.branchmanagement.repository.BranchRepository;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.AttendanceService;
import com.branchmanagement.service.NotificationService;
import com.branchmanagement.service.SystemConfigService;
import com.branchmanagement.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of Attendance Service.
 * Handles employee attendance tracking with GPS and Photo verification.
 * 
 * Cleaned and optimized for production readiness.
 */
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);
    
    // Configuration Keys
    private static final String CONFIG_GPS_RADIUS = "gps_radius";
    private static final String UPLOAD_DIR = "uploads/attendance/";
    
    // Constraints
    private static final double DEFAULT_ALLOWED_RADIUS = 200.0;
    private static final double MAX_ABNORMAL_DISTANCE = 500.0;
    
    // Business Hours
    private static final int CHECKIN_START_HOUR = 7;
    private static final int CHECKIN_END_HOUR = 9;
    private static final int CHECKOUT_START_HOUR = 16;
    private static final int CHECKOUT_END_HOUR = 22;

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final SystemConfigService configService;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public void checkIn(Integer userId, Integer branchId, Double userLat, Double userLon, MultipartFile photo) {
        validatePhotoPresence(photo);
        
        Branch branch = findBranchOrThrow(branchId);
        double distance = calculateDistance(userLat, userLon, branch);
        
        LocalDateTime now = LocalDateTime.now();
        logger.info("Check-in attempt: UserID={}, Branch={}, Distance={}m", userId, branch.getBranchName(), (int)distance);

        String photoUrl = storeImage(photo);
        AttendanceStatus status = determineInitialStatus(distance);

        // Time window validation
        String systemNote = "";
        if (isOutsideCheckInWindow(now)) {
            status = AttendanceStatus.ABNORMAL;
            systemNote = String.format("[Hệ thống]: Chấm công vào ca ngoài khung giờ quy định (%02d:00-%02d:00).", 
                                       CHECKIN_START_HOUR, CHECKIN_END_HOUR);
        }

        Attendance attendance = createAttendanceEntity(userId, branch, userLat, userLon, distance, photoUrl, status);
        if (!systemNote.isEmpty()) {
            attendance.setRejectReason(systemNote);
        }
        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public void checkOut(Integer userId, Integer branchId, Double userLat, Double userLon, MultipartFile photo) {
        Attendance attendance = attendanceRepository.findTopByUser_UserIdAndCheckOutIsNullOrderByCheckInDesc(userId)
                .orElseThrow(() -> new AppException("Bạn không có lượt check-in nào đang mở hoặc đã kết thúc ca trước đó."));

        Branch branch = attendance.getBranch();
        double distance = GeoUtils.calculateDistance(userLat, userLon, 
                                                   branch.getLatitude().doubleValue(), 
                                                   branch.getLongitude().doubleValue());
        
        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOut(now);
        
        if (photo != null && !photo.isEmpty()) {
            attendance.setCheckOutPhotoUrl(storeImage(photo));
        }

        // Validate distance and time for Check-out
        double allowedRadius = getGpsRadiusLimit();
        if (distance > allowedRadius) {
             throw new AppException("Check-out thất bại! Khoảng cách của bạn không hợp lệ. Bạn đang ở cách chi nhánh " + (int)distance + "m");
        }

        boolean isOutsideWindow = isOutsideCheckOutWindow(now);
        if (isOutsideWindow) {
            attendance.setStatus(AttendanceStatus.ABNORMAL.getValue());
            StringBuilder notes = new StringBuilder(attendance.getRejectReason() != null ? attendance.getRejectReason() + " " : "");
            notes.append(String.format("[Hệ thống]: Chấm công kết ca ngoài khung giờ quy định (%02d:00-%02d:00).", 
                                       CHECKOUT_START_HOUR, CHECKOUT_END_HOUR));
            attendance.setRejectReason(notes.toString().trim());
        }

        updateWorkingHours(attendance);
        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendances(CustomUserDetails userDetails, String status, 
                                             LocalDateTime startDate, LocalDateTime endDate, 
                                             int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("checkIn").descending());
        Integer branchId = isAdmin(userDetails) ? null : userDetails.getBranchId();
        
        return attendanceRepository.findAllWithFilters(branchId, status, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void approveAttendance(Integer id, CustomUserDetails userDetails) {
        Attendance attendance = getAndVerifyAttendance(id, userDetails);
        attendance.setStatus(AttendanceStatus.APPROVED.getValue());
        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public void rejectAttendance(Integer id, CustomUserDetails userDetails, String reason) {
        Attendance attendance = getAndVerifyAttendance(id, userDetails);
        attendance.setStatus(AttendanceStatus.REJECTED.getValue());
        attendance.setRejectReason(reason);
        attendanceRepository.save(attendance);
        
        notificationService.sendAlert(
            attendance.getUser(),
            "attendance_rejected",
            "Chấm công bị từ chối",
            "Lượt chấm công ngày " + attendance.getCheckIn().toLocalDate() + " bị từ chối. Lý do: " + reason,
            "High",
            "/staff/attendance/history"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceDetail(Integer id, CustomUserDetails userDetails) {
        return mapToResponse(getAndVerifyAttendance(id, userDetails));
    }

    // --- Helper Methods ---

    private void validatePhotoPresence(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new AppException("Bắt buộc phải chụp ảnh khuôn mặt để xác thực chấm công.");
        }
    }

    private Branch findBranchOrThrow(Integer branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin chi nhánh.", HttpStatus.NOT_FOUND));
    }

    private double calculateDistance(Double userLat, Double userLon, Branch branch) {
        return GeoUtils.calculateDistance(userLat, userLon, 
                                        branch.getLatitude().doubleValue(), 
                                        branch.getLongitude().doubleValue());
    }

    private AttendanceStatus determineInitialStatus(double distance) {
        double allowedRadius = getGpsRadiusLimit();
        if (distance > allowedRadius) {
            if (distance > MAX_ABNORMAL_DISTANCE) {
                throw new AppException("Vị trí của bạn không hợp lệ. Bạn đang ở cách xa chi nhánh " + (int)distance + "m");
            }
            return AttendanceStatus.ABNORMAL;
        }
        return AttendanceStatus.NORMAL;
    }

    private boolean isOutsideCheckInWindow(LocalDateTime time) {
        int hour = time.getHour();
        return hour < CHECKIN_START_HOUR || hour >= CHECKIN_END_HOUR;
    }

    private boolean isOutsideCheckOutWindow(LocalDateTime time) {
        int hour = time.getHour();
        return hour < CHECKOUT_START_HOUR || hour >= CHECKOUT_END_HOUR;
    }

    private double getGpsRadiusLimit() {
        try {
            return Double.parseDouble(configService.getByKey(CONFIG_GPS_RADIUS).getConfigValue());
        } catch (Exception e) {
            return DEFAULT_ALLOWED_RADIUS;
        }
    }

    private String storeImage(MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();
            
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.write(Paths.get(UPLOAD_DIR + fileName), file.getBytes());
            return "/" + UPLOAD_DIR + fileName;
        } catch (IOException e) {
            throw new AppException("Lỗi khi lưu tệp tin ảnh: " + e.getMessage());
        }
    }

    private Attendance createAttendanceEntity(Integer userId, Branch branch, Double lat, Double lon, 
                                            double distance, String photoUrl, AttendanceStatus status) {
        Attendance attendance = new Attendance();
        attendance.setUser(userRepository.getReferenceById(userId));
        attendance.setBranch(branch);
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setLatitude(lat);
        attendance.setLongitude(lon);
        attendance.setDistance(distance);
        attendance.setCheckInPhotoUrl(photoUrl);
        attendance.setStatus(status.getValue());
        return attendance;
    }

    private void updateWorkingHours(Attendance attendance) {
        Duration duration = Duration.between(attendance.getCheckIn(), attendance.getCheckOut());
        double hours = duration.toMinutes() / 60.0;
        attendance.setTotalHours(BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP));
    }

    private Attendance getAndVerifyAttendance(Integer id, CustomUserDetails userDetails) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy bản ghi chấm công.", HttpStatus.NOT_FOUND));
        
        if (!isAdmin(userDetails) && !attendance.getBranch().getBranchId().equals(userDetails.getBranchId())) {
            throw new AppException("Bạn không có quyền truy cập dữ liệu của chi nhánh này.", HttpStatus.FORBIDDEN);
        }
        return attendance;
    }

    private boolean isAdmin(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN"));
    }

    private AttendanceResponse mapToResponse(Attendance entity) {
        AttendanceResponse res = new AttendanceResponse();
        res.setId(entity.getAttendanceId());
        if (entity.getUser() != null) {
            res.setUserId(entity.getUser().getUserId());
            res.setEmployeeName(entity.getUser().getFullName());
            res.setEmployeeCode(entity.getUser().getUsername());
        }
        res.setCheckInTime(entity.getCheckIn());
        res.setCheckOutTime(entity.getCheckOut());
        res.setTotalHours(entity.getTotalHours());
        res.setCheckInPhotoUrl(entity.getCheckInPhotoUrl());
        res.setCheckOutPhotoUrl(entity.getCheckOutPhotoUrl());
        res.setDistance(entity.getDistance());
        res.setStatus(entity.getStatus());
        res.setLatitude(entity.getLatitude());
        res.setLongitude(entity.getLongitude());
        res.setRejectReason(entity.getRejectReason());
        return res;
    }
}
