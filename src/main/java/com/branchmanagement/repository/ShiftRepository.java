package com.branchmanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.branchmanagement.entity.Shift;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Integer> {

	List<Shift> findByBranch_BranchIdOrderByDateAscStartTimeAsc(Integer branchId);

	List<Shift> findByBranch_BranchIdAndDateBetweenOrderByDateAscStartTimeAsc(Integer branchId, LocalDate from,
			LocalDate to);

	List<Shift> findByDateBetweenOrderByDateAscStartTimeAsc(LocalDate from, LocalDate to);

	@Query("""
			SELECT s FROM Shift s
			LEFT JOIN FETCH s.userShifts us
			WHERE s.branch.branchId = :branchId
			  AND s.date BETWEEN :from AND :to
			ORDER BY s.date ASC, s.startTime ASC
			""")
	List<Shift> findWithUsersByBranchAndDateRange(@Param("branchId") Integer branchId, @Param("from") LocalDate from,
			@Param("to") LocalDate to);
}