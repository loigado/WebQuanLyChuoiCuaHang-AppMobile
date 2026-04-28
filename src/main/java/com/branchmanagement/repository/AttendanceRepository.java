package com.branchmanagement.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.branchmanagement.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

	@Query("""
			    SELECT a.user.userId,
			           a.user.fullName,
			           SUM(a.totalHours)
			    FROM Attendance a
			    WHERE (:branchId IS NULL OR a.branch.branchId = :branchId)
			      AND (:fromDate IS NULL OR a.checkIn >= :fromDate)
			      AND (:toDate IS NULL OR a.checkOut <= :toDate)
			    GROUP BY a.user.userId, a.user.fullName
			""")
	List<Object[]> getAttendanceSummary(@Param("branchId") Integer branchId, @Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate);

	@Query("""
			    SELECT
			        a.user.userId,
			        a.user.fullName,
			        a.branch.branchName,
			        SUM(a.totalHours),
			        COUNT(a),
			        SUM(CASE WHEN a.status = 'abnormal' THEN 1 ELSE 0 END),
			        SUM(CASE WHEN FUNCTION('DATEPART', hour, a.checkIn) >= :lateHour THEN 1 ELSE 0 END),
			        SUM(CASE WHEN a.checkOut IS NOT NULL AND FUNCTION('DATEPART', hour, a.checkOut) < :earlyHour THEN 1 ELSE 0 END)
			    FROM Attendance a
			    WHERE (:branchId IS NULL OR a.branch.branchId = :branchId)
			      AND (:fromDate IS NULL OR a.checkIn >= :fromDate)
			      AND (:toDate IS NULL OR a.checkIn <= :toDate)
			    GROUP BY a.user.userId, a.user.fullName, a.branch.branchName
			    ORDER BY a.user.fullName
			""")
	List<Object[]> getAttendanceDetailSummary(@Param("branchId") Integer branchId,
			@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate,
			@Param("lateHour") int lateHour, @Param("earlyHour") int earlyHour);

	@Query("""
			    SELECT a
			    FROM Attendance a
			    WHERE (:branchId IS NULL OR a.branch.branchId = :branchId)
			""")
	List<Attendance> findAllByBranch(@Param("branchId") Integer branchId);
	
	@Query("SELECT new com.branchmanagement.dto.AttendanceReportDto(" +
	           "u.username, u.fullName, " +
	           "SUM(CASE WHEN a.status = 'PRESENT' THEN 1L ELSE 0L END), " +
	           "SUM(CASE WHEN a.status = 'LATE' THEN 1L ELSE 0L END), " +
	           "SUM(CASE WHEN a.status = 'EARLY_LEAVE' THEN 1L ELSE 0L END), " +
	           "SUM(a.totalHours)) " +
	           "FROM Attendance a JOIN a.user u " +
	           "WHERE u.branch.branchId = :branchId " +
	           "AND a.checkIn >= :startDate AND a.checkIn <= :endDate " +
	           "GROUP BY u.username, u.fullName")
	List<com.branchmanagement.dto.AttendanceReportDto> getBranchAttendanceReport(
	            @Param("branchId") Integer branchId, 
	            @Param("startDate") LocalDateTime startDate, 
	            @Param("endDate") LocalDateTime endDate);

    // ✅ THÊM MỚI: Lấy danh sách chấm công theo chi nhánh có phân trang (Phục vụ màn hình Manager)
    Page<Attendance> findByBranch_BranchIdOrderByCheckInDesc(Integer branchId, Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.branch.branchId = :branchId")
    long countByBranchId(@Param("branchId") Integer branchId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.branch.branchId = :branchId AND a.status = :status")
    long countByBranchIdAndStatus(@Param("branchId") Integer branchId, @Param("status") String status);

    long countByStatusNot(String status);
    
    Page<Attendance> findAllByOrderByCheckInDesc(Pageable pageable);
    Page<Attendance> findByStatusOrderByCheckInDesc(String status, Pageable pageable);
    Page<Attendance> findByBranch_BranchIdAndStatusOrderByCheckInDesc(Integer branchId, String status, Pageable pageable);

    @Query("SELECT a FROM Attendance a " +
           "WHERE (:branchId IS NULL OR a.branch.branchId = :branchId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:startDate IS NULL OR a.checkIn >= :startDate) " +
           "AND (:endDate IS NULL OR a.checkIn <= :endDate)")
    Page<Attendance> findAllWithFilters(
            @Param("branchId") Integer branchId, 
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.user " +
           "WHERE a.user.username = :username " +
           "AND a.checkIn >= :startDate AND a.checkIn <= :endDate " +
           "ORDER BY a.checkIn DESC")
    List<Attendance> findDetailsByUserAndPeriod(
            @Param("username") String username, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

 // Dùng chung cho cả Kế toán (branchId = null) và Quản lý (truyền branchId)
    @Query("SELECT b.branchName, u.username, u.fullName, " +
           "COUNT(a.attendanceId), SUM(a.totalHours), " +
           "SUM(CASE WHEN UPPER(a.status) = 'ABNORMAL' THEN 1 ELSE 0 END), " +
           "u.hourlyRate " +
           "FROM Attendance a " +
           "JOIN a.user u " +
           "JOIN u.branch b " +
           "WHERE (:branchId IS NULL OR b.branchId = :branchId) " +
           "AND a.checkIn BETWEEN :startDate AND :endDate " +
           "GROUP BY b.branchName, u.username, u.fullName, u.hourlyRate")
    List<Object[]> getAggregatedAttendanceData(
            @Param("branchId") Integer branchId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    List<Attendance> findByUser_UserIdOrderByCheckInDesc(Integer userId);

    List<Attendance> findByUser_UserIdAndCheckInAfter(Integer userId, LocalDateTime after);

    Optional<Attendance> findTopByUser_UserIdAndCheckOutIsNullOrderByCheckInDesc(Integer userId);

    List<Attendance> findByUser_UserIdAndCheckInBetween(Integer userId, LocalDateTime start, LocalDateTime end);
}