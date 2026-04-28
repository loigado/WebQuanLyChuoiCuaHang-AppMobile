package com.branchmanagement.service;

import com.branchmanagement.dto.CheckInRequest;
import com.branchmanagement.dto.UserShiftDTO;
import com.branchmanagement.dto.LeaveRequestResponse; // ✅ Import DTO
import com.branchmanagement.entity.Attendance;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.entity.LeaveRequest; // ✅ Import Entity
import com.branchmanagement.entity.User;
import com.branchmanagement.repository.AttendanceRepository;
import com.branchmanagement.repository.BranchRepository;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.repository.UserShiftRepository;
import com.branchmanagement.repository.LeaveRequestRepository; // ✅ Import Repo
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest; // ✅ Import PageRequest
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MobileEmployeeService {

    private final UserShiftRepository userShiftRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final com.branchmanagement.repository.SystemConfigRepository systemConfigRepository;
    private final com.branchmanagement.repository.StockRepository stockRepository;
    private final com.branchmanagement.service.StockApprovalService stockApprovalService;
    private final com.branchmanagement.repository.StockTransactionRepository transactionRepository;
    private final com.branchmanagement.repository.TransferRequestRepository transferRequestRepository;
    private final com.branchmanagement.repository.AnnouncementRepository announcementRepository;

    public MobileEmployeeService(
            UserShiftRepository userShiftRepository,
            UserRepository userRepository,
            BranchRepository branchRepository,
            AttendanceRepository attendanceRepository,
            LeaveRequestRepository leaveRequestRepository,
            com.branchmanagement.repository.SystemConfigRepository systemConfigRepository,
            com.branchmanagement.repository.StockRepository stockRepository,
            com.branchmanagement.service.StockApprovalService stockApprovalService,
            com.branchmanagement.repository.StockTransactionRepository transactionRepository,
            com.branchmanagement.repository.TransferRequestRepository transferRequestRepository,
            com.branchmanagement.repository.AnnouncementRepository announcementRepository) {
        this.userShiftRepository = userShiftRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.stockRepository = stockRepository;
        this.stockApprovalService = stockApprovalService;
        this.transactionRepository = transactionRepository;
        this.transferRequestRepository = transferRequestRepository;
        this.announcementRepository = announcementRepository;
    }

    /**
     * Retrieves assigned shifts within a specific date range.
     * Defaults to filtering out past shifts if no range is provided.
     */
    public List<UserShiftDTO> getMyShifts(Integer userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return userShiftRepository.findByUser_UserId(userId).stream()
                .filter(shift -> shift.getShift() != null && shift.getShift().getDate() != null)
                .filter(shift -> {
                    java.time.LocalDate shiftDate = shift.getShift().getDate();
                    if (startDate != null && shiftDate.isBefore(startDate)) return false;
                    if (endDate != null && shiftDate.isAfter(endDate)) return false;
                    // Default behavior: Don't show past shifts if no start date is given
                    if (startDate == null && shiftDate.isBefore(java.time.LocalDate.now())) return false;
                    return true;
                })
                .map(shift -> UserShiftDTO.builder()
                        .userShiftId(shift.getUserShiftId())
                        .status(shift.getStatus())
                        .note(shift.getNote())
                        .shiftName(shift.getShift().getShiftName()) 
                        .startTime(shift.getShift().getStartTime())
                        .endTime(shift.getShift().getEndTime())
                        .date(shift.getShift().getDate())
                        .branchName(shift.getShift().getBranch().getBranchName())
                        .build()
                ).collect(Collectors.toList());
    }

    public List<Attendance> getPersonalAttendanceHistory(Integer userId) {
        return attendanceRepository.findByUser_UserIdOrderByCheckInDesc(userId);
    }

    public List<com.branchmanagement.dto.AttendanceResponse> getMyAttendanceHistory(Integer userId) {
        return attendanceRepository.findByUser_UserIdOrderByCheckInDesc(userId).stream()
                .map(att -> {
                    com.branchmanagement.dto.AttendanceResponse res = new com.branchmanagement.dto.AttendanceResponse();
                    res.setId(att.getAttendanceId());
                    res.setCheckInTime(att.getCheckIn());
                    res.setCheckOutTime(att.getCheckOut());
                    res.setTotalHours(att.getTotalHours());
                    res.setStatus(att.getStatus());
                    res.setCheckInPhotoUrl(att.getCheckInPhotoUrl());
                    res.setCheckOutPhotoUrl(att.getCheckOutPhotoUrl());
                    res.setLatitude(att.getLatitude());
                    res.setLongitude(att.getLongitude());
                    res.setDistance(att.getDistance());
                    return res;
                }).collect(Collectors.toList());
    }

    /**
     * Processes check-in with GPS verification against system configuration.
     */
    public Attendance processCheckIn(CheckInRequest req) {
        Branch branch = branchRepository.findById(req.getBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

        double distance = calculateDistance(req.getLat(), req.getLng(), 
                                            branch.getLatitude().doubleValue(), 
                                            branch.getLongitude().doubleValue());

        double configRadius = getConfigValueAsDouble("GPS_RADIUS", 100.0);

        if (distance > configRadius) {
            String msg = String.format("Chấm công thất bại! Bạn đang ở cách chi nhánh %.0f mét (Giới hạn cho phép: %.0f mét).", distance, configRadius);
            throw new RuntimeException(msg);
        }

        Attendance att = new Attendance();
        att.setUser(user);
        att.setBranch(branch);
        att.setCheckIn(LocalDateTime.now());
        att.setLatitude(req.getLat()); 
        att.setLongitude(req.getLng());
        att.setCheckInPhotoUrl(req.getPhotoUrl());
        att.setDistance(distance);
        att.setStatus("normal");
        
        return attendanceRepository.save(att);
    }

    /**
     * Processes check-out with GPS verification and automatic hour calculation.
     */
    public Attendance processCheckOut(CheckInRequest req) {
        Attendance attendance = attendanceRepository
            .findTopByUser_UserIdAndCheckOutIsNullOrderByCheckInDesc(req.getUserId())
            .orElseThrow(() -> new RuntimeException("Bạn chưa Check-in hoặc đã hoàn thành ca làm việc!"));

        Branch branch = attendance.getBranch();
        double distance = calculateDistance(req.getLat(), req.getLng(), 
                                            branch.getLatitude().doubleValue(), 
                                            branch.getLongitude().doubleValue());
        
        double configRadius = getConfigValueAsDouble("GPS_RADIUS", 100.0);

        if (distance > configRadius) {
            String msg = String.format("Check-out thất bại! Bạn đang ở cách chi nhánh %.0f mét (Giới hạn cho phép: %.0f mét).", distance, configRadius);
            throw new RuntimeException(msg);
        }

        attendance.setCheckOut(LocalDateTime.now());
        attendance.setCheckOutPhotoUrl(req.getPhotoUrl());
        
        // If checkout is abnormal, we keep/set the status to abnormal
        if (distance > configRadius) {
            attendance.setStatus("abnormal");
        }

        Duration duration = Duration.between(attendance.getCheckIn(), attendance.getCheckOut());
        double hours = (double) duration.toMinutes() / 60.0;
        attendance.setTotalHours(BigDecimal.valueOf(Math.round(hours * 10.0) / 10.0));

        return attendanceRepository.save(attendance);
    }

    private double getConfigValueAsDouble(String key, double defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(c -> {
                    try { return Double.parseDouble(c.getConfigValue()); }
                    catch (Exception e) { return defaultValue; }
                }).orElse(defaultValue);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
    
    public List<LeaveRequestResponse> getMyLeaveRequests(Integer userId) {
        return leaveRequestRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 50))
                .stream().map(lr -> {
                    LeaveRequestResponse dto = new LeaveRequestResponse();
                    dto.setLeaveId(lr.getLeaveId());
                    dto.setLeaveType(lr.getLeaveType());
                    dto.setStartDate(lr.getStartDate());
                    dto.setEndDate(lr.getEndDate());
                    dto.setReason(lr.getReason());
                    dto.setStatus(lr.getStatus());
                    dto.setRejectReason(lr.getRejectReason());
                    dto.setCreatedAt(lr.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList());
    }

    public void submitLeaveRequest(Integer userId, LeaveRequestResponse dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));

        LeaveRequest request = new LeaveRequest();
        request.setUser(user);
        request.setBranch(user.getBranch());
        request.setLeaveType(dto.getLeaveType());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setReason(dto.getReason());
        request.setStatus("pending");
        
        leaveRequestRepository.save(request);
    }



    /**
     * Lists current stock levels for the branch the user belongs to.
     */
    public List<com.branchmanagement.dto.MobileStockResponse> getBranchInventory(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        if (user.getBranch() == null) {
            throw new RuntimeException("Người dùng chưa được gán vào chi nhánh nào!");
        }

        Integer branchId = user.getBranch().getBranchId();
        return stockRepository.findDetailedStock(branchId, null).stream()
                .map(s -> {
                    com.branchmanagement.dto.MobileStockResponse dto = new com.branchmanagement.dto.MobileStockResponse();
                    dto.setProductId(s.getProduct().getProductId());
                    dto.setProductName(s.getProduct().getProductName());
                    dto.setProductCode(s.getProduct().getProductCode());
                    dto.setUnit(s.getProduct().getUnit());
                    dto.setQuantity(s.getQuantity());
                    dto.setLowStock(s.getQuantity().compareTo(BigDecimal.valueOf(10)) < 0);
                    return dto;
                }).collect(Collectors.toList());
    }

    /**
     * Get pending stock requests for mobile managers.
     */
    public List<com.branchmanagement.dto.StockTransactionResponse> getMyPendingRequests(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String r = user.getRole() != null ? user.getRole().name() : "";
        System.out.println("DEBUG: Mobile request for pending from userId: " + userId + ", Role: " + r);
        
        List<com.branchmanagement.dto.StockTransactionResponse> results = new java.util.ArrayList<>();

        if (r.contains("ADMIN")) {
            // Admin trên mobile chỉ nên thấy những gì trang Phê duyệt trên Web thấy
            List<com.branchmanagement.entity.StockTransaction> transactions = transactionRepository.findPendingTransactions();
            results.addAll(transactions.stream().map(this::mapToTransactionResponse).collect(Collectors.toList()));
            System.out.println("DEBUG: Admin found " + results.size() + " stock transactions");
        } else if (r.contains("MANAGER")) {
            Integer myBranchId = user.getBranch() != null ? user.getBranch().getBranchId() : null;
            if (myBranchId == null) return List.of();

            // 1. Lấy phiếu kho của chi nhánh mình
            List<com.branchmanagement.entity.StockTransaction> transactions = transactionRepository.findPendingByBranch(myBranchId);
            results.addAll(transactions.stream().map(this::mapToTransactionResponse).collect(Collectors.toList()));

            // 2. Lấy yêu cầu điều chuyển liên quan đến chi nhánh mình
            List<com.branchmanagement.entity.TransferRequest> requests = transferRequestRepository.findPendingAndShipping();
            for (com.branchmanagement.entity.TransferRequest req : requests) {
                boolean isFromMe = req.getFromBranch() != null && req.getFromBranch().getBranchId().equals(myBranchId);
                boolean isToMe = req.getToBranch() != null && req.getToBranch().getBranchId().equals(myBranchId);
                
                if ("pending".equalsIgnoreCase(req.getStatus()) && isFromMe) {
                    results.add(mapRequestToResponse(req));
                }
                else if ("shipping".equalsIgnoreCase(req.getStatus()) && isToMe) {
                    results.add(mapRequestToResponse(req));
                }
            }
        }
        return results;
    }

    private com.branchmanagement.dto.StockTransactionResponse mapRequestToResponse(com.branchmanagement.entity.TransferRequest req) {
        com.branchmanagement.dto.StockTransactionResponse dto = new com.branchmanagement.dto.StockTransactionResponse();
        dto.setTransactionId(req.getRequestId()); // Dùng requestId làm ID giả
        dto.setTransactionCode(req.getRequestCode());
        dto.setTransactionType("transfer");
        dto.setStatus(req.getStatus());
        dto.setReason(req.getReason());
        dto.setCreatedAt(req.getCreatedAt());
        
        if (req.getFromBranch() != null) dto.setFromBranchName(req.getFromBranch().getBranchName());
        if (req.getToBranch() != null) dto.setToBranchName(req.getToBranch().getBranchName());
        
        if (req.getProduct() != null) {
            dto.setProductName(req.getProduct().getProductName());
            dto.setQuantity(req.getQuantity());
        }
        return dto;
    }

    /**
     * Process approval/rejection from mobile.
     */
    @org.springframework.transaction.annotation.Transactional
    public com.branchmanagement.dto.StockTransactionResponse processRequestFromMobile(Integer userId, Integer transactionId, String decision, String note) {
        if ("APPROVE".equalsIgnoreCase(decision)) {
            com.branchmanagement.dto.ApproveTransactionRequest req = new com.branchmanagement.dto.ApproveTransactionRequest();
            req.setNote(note);
            return stockApprovalService.approveTransaction(transactionId, req, userId);
        } else {
            com.branchmanagement.dto.RejectTransactionRequest req = new com.branchmanagement.dto.RejectTransactionRequest();
            req.setRejectionReason(note);
            return stockApprovalService.rejectTransaction(transactionId, req, userId);
        }
    }

    private com.branchmanagement.dto.StockTransactionResponse mapToTransactionResponse(com.branchmanagement.entity.StockTransaction t) {
        com.branchmanagement.dto.StockTransactionResponse dto = new com.branchmanagement.dto.StockTransactionResponse();
        dto.setTransactionId(t.getTransactionId());
        dto.setTransactionCode(t.getTransactionCode());
        dto.setTransactionType(t.getTransactionType());
        dto.setStatus(t.getStatus());
        dto.setReason(t.getReason());
        dto.setCreatedAt(t.getCreatedAt());
        
        if (t.getFromBranch() != null) dto.setFromBranchName(t.getFromBranch().getBranchName());
        if (t.getToBranch() != null) dto.setToBranchName(t.getToBranch().getBranchName());
        
        if (t.getDetails() != null && !t.getDetails().isEmpty()) {
            dto.setProductName(t.getDetails().get(0).getProduct().getProductName());
            dto.setQuantity(t.getDetails().get(0).getQuantity());
        }
        return dto;
    }

    /**
     * Calculates estimated payroll for the current month.
     */
    public com.branchmanagement.dto.MobilePayrollResponse getMyPayrollSummary(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        java.time.LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59);

        // Lấy tất cả lượt chấm công đã HOÀN THÀNH (có checkout) trong tháng này
        List<com.branchmanagement.entity.Attendance> attendances = attendanceRepository.findByUser_UserIdAndCheckInBetween(
                userId, startOfMonth, endOfMonth);

        double totalHours = 0;
        int totalShifts = 0;

        for (com.branchmanagement.entity.Attendance att : attendances) {
            if (att.getCheckOut() != null && att.getTotalHours() != null) {
                totalHours += att.getTotalHours().doubleValue();
                totalShifts++;
            }
        }

        java.math.BigDecimal hourlyRate = user.getHourlyRate() != null ? user.getHourlyRate() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal estimatedSalary = hourlyRate.multiply(java.math.BigDecimal.valueOf(totalHours));

        return new com.branchmanagement.dto.MobilePayrollResponse(
                now.getMonthValue() + "/" + now.getYear(),
                totalHours,
                hourlyRate,
                estimatedSalary,
                totalShifts
        );
    }

    /**
     * Get 5 latest internal announcements.
     */
    public List<com.branchmanagement.entity.Announcement> getLatestAnnouncements() {
        return announcementRepository.findTop5ByOrderByCreatedAtDesc();
    }
}