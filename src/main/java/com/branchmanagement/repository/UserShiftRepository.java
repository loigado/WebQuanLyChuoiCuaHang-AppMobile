package com.branchmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.branchmanagement.entity.UserShift;

@Repository
public interface UserShiftRepository extends JpaRepository<UserShift, Integer> {

	List<UserShift> findByShift_ShiftId(Integer shiftId);

	List<UserShift> findByUser_UserId(Integer userId);

	Optional<UserShift> findByShift_ShiftIdAndUser_UserId(Integer shiftId, Integer userId);

	boolean existsByShift_ShiftIdAndUser_UserId(Integer shiftId, Integer userId);
	void deleteByShift_ShiftId(Integer shiftId);
}