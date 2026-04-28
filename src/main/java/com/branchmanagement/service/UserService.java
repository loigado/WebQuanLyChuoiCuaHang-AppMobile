package com.branchmanagement.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.branchmanagement.entity.User;

public interface UserService {

	User createUser(User user);

	User updateUser(Integer id, User updatedUser);

	void lockUser(Integer id);

	void unlockUser(Integer id);

	void resetPassword(Integer id, String newPassword);

	List<User> getAllUsers();

	// ✅ ĐÃ THÊM: Tìm user theo ID trực tiếp
	User getUserById(Integer id);

	List<User> getUsersByBranch(Integer branchId);

	Page<User> getAllUsers(int page, int size);
}