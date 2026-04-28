package com.branchmanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "Attendance")
public class Attendance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "attendance_id")
	private Integer attendanceId;
	
	@Column(name = "working_hours")
	private Double workingHours;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	private LocalDateTime checkIn;
	private LocalDateTime checkOut;

	@Column(precision = 5, scale = 2)
	private BigDecimal totalHours;

	private String status;

	private LocalDateTime createdAt;

    // ✅ CÁC TRƯỜNG MỚI ĐỂ LƯU 2 ẢNH
    @Column(name = "check_in_photo_url", length = 500)
    private String checkInPhotoUrl; 

    @Column(name = "check_out_photo_url", length = 500)
    private String checkOutPhotoUrl; 

    private Double latitude;  
    private Double longitude;
    private Double distance;  

    @Column(name = "reject_reason", columnDefinition = "NVARCHAR(500)")
    private String rejectReason; 

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

	public Integer getAttendanceId() { return attendanceId; }
	public void setAttendanceId(Integer attendanceId) { this.attendanceId = attendanceId; }

	public Double getWorkingHours() { return workingHours; }
	public void setWorkingHours(Double workingHours) { this.workingHours = workingHours; }

	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }

	public Branch getBranch() { return branch; }
	public void setBranch(Branch branch) { this.branch = branch; }

	public LocalDateTime getCheckIn() { return checkIn; }
	public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }

	public LocalDateTime getCheckOut() { return checkOut; }
	public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }

	public BigDecimal getTotalHours() { return totalHours; }
	public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ✅ GETTER & SETTER CHO 2 ẢNH
    public String getCheckInPhotoUrl() { return checkInPhotoUrl; }
    public void setCheckInPhotoUrl(String checkInPhotoUrl) { this.checkInPhotoUrl = checkInPhotoUrl; }

    public String getCheckOutPhotoUrl() { return checkOutPhotoUrl; }
    public void setCheckOutPhotoUrl(String checkOutPhotoUrl) { this.checkOutPhotoUrl = checkOutPhotoUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}