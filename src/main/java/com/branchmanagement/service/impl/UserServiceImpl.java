package com.branchmanagement.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.branchmanagement.entity.Branch;
import com.branchmanagement.entity.User;
import com.branchmanagement.enums.RoleType;
import com.branchmanagement.repository.BranchRepository;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.service.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final BranchRepository branchRepository;
	
	public UserServiceImpl(UserRepository userRepository, BranchRepository branchRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.branchRepository = branchRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public List<User> getUsersByBranch(Integer branchId) {
		return userRepository.findByBranch_BranchIdOrderByFullNameAsc(branchId);
	}

	@Override
	public Page<User> getAllUsers(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return userRepository.findAll(pageable);
	}

	@Override
	public User createUser(User user) {
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new RuntimeException("Username already exists");
		}
		
		validateRole(user.getRole());

		if (user.getBranch() != null && user.getBranch().getBranchId() != null) {
			Branch branch = branchRepository.findById(user.getBranch().getBranchId())
					.orElseThrow(() -> new RuntimeException("Branch not found"));
			user.setBranch(branch);
		}
        
        // Băm mật khẩu khi tạo user mới
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		if (user.getHourlyRate() == null) {
			user.setHourlyRate(java.math.BigDecimal.ZERO);
		}
		
		return userRepository.save(user);
	}

	@Override
	public User updateUser(Integer id, User updatedUser) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

		validateRole(updatedUser.getRole());
		
		user.setFullName(updatedUser.getFullName());
		user.setRole(updatedUser.getRole());
		user.setHourlyRate(updatedUser.getHourlyRate());

		if (updatedUser.getBranch() != null && updatedUser.getBranch().getBranchId() != null) {
			Branch branch = branchRepository.findById(updatedUser.getBranch().getBranchId())
					.orElseThrow(() -> new RuntimeException("Branch not found"));
			user.setBranch(branch);
		} else {
			user.setBranch(null);
		}

		return userRepository.save(user);
	}

	@Override
	public void lockUser(Integer id) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		user.setStatus("inactive"); // Đổi sang inactive để khóa
		userRepository.save(user);
	}

	@Override
	public void unlockUser(Integer id) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		user.setStatus("active"); // Đổi sang active để mở khóa
		userRepository.save(user);
	}

	@Override
	public void resetPassword(Integer id, String newPassword) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		user.setPassword(passwordEncoder.encode(newPassword)); 
		userRepository.save(user);
	}

	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	// ✅ ĐÃ THÊM: Tìm user theo ID trực tiếp (tránh load toàn bộ rồi filter)
	@Override
	public User getUserById(Integer id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));
	}

	// ======================
	// PRIVATE VALIDATION
	// ======================

	private void validateRole(RoleType role) {
		if (role == null) {
			throw new RuntimeException("Vai trò (Role) không được để trống");
		}
	}
}