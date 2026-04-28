package com.branchmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.branchmanagement.entity.SystemConfig;

/**
 * Repository cho entity SystemConfig
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {

	/**
	 * Tìm config theo key (config_key) Dùng để lấy giá trị cấu hình cụ thể
	 */
	Optional<SystemConfig> findByConfigKey(String configKey);

	/**
	 * Kiểm tra config key có tồn tại không
	 */
	boolean existsByConfigKey(String configKey);
}