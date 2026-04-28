package com.branchmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AttendanceResponse {
    private Integer id;
    private Integer userId;
    private String employeeName;
    private String employeeCode;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalHours;
    
    // ✅ THÊM 2 TRƯỜNG NÀY ĐỂ APP MOBILE ĐỌC ĐƯỢC 2 ẢNH
    private String checkInPhotoUrl; 
    private String checkOutPhotoUrl; 
    
    private Double distance;
    private Double latitude;
    private Double longitude;
    private String status;
    private String rejectReason;

    public AttendanceResponse() {}

    // --- GETTERS & SETTERS ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }

    // ✅ GETTER & SETTER CHO 2 ẢNH
    public String getCheckInPhotoUrl() { return checkInPhotoUrl; }
    public void setCheckInPhotoUrl(String checkInPhotoUrl) { this.checkInPhotoUrl = checkInPhotoUrl; }

    public String getCheckOutPhotoUrl() { return checkOutPhotoUrl; }
    public void setCheckOutPhotoUrl(String checkOutPhotoUrl) { this.checkOutPhotoUrl = checkOutPhotoUrl; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}