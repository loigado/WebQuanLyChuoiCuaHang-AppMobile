package com.branchmanagement.service;

import java.time.LocalTime;
import java.util.List;

import com.branchmanagement.dto.SystemConfigBulkUpdateRequest;
import com.branchmanagement.dto.SystemConfigResponse;
import com.branchmanagement.dto.SystemConfigUpdateRequest;

/**
 * Interface định nghĩa các nghiệp vụ quản lý SystemConfig
 */
public interface SystemConfigService {

	/**
	 * Lấy danh sách tất cả cấu hình
	 */
	List<SystemConfigResponse> getAllConfigs();

	/**
	 * Lấy cấu hình theo key
	 */
	SystemConfigResponse getByKey(String key);

	/**
	 * Lấy giá trị cấu hình (chỉ trả về value string) Tiện ích để dùng trong code
	 */
	String getValue(String key);

	/**
	 * Cập nhật 1 cấu hình
	 */
	SystemConfigResponse updateConfig(String key, SystemConfigUpdateRequest request);

	/**
	 * Cập nhật nhiều cấu hình cùng lúc Dùng khi user nhấn "Lưu tất cả" trong UI
	 */
	void bulkUpdate(SystemConfigBulkUpdateRequest request);

	// ========== UTILITY METHODS - Lấy giá trị config đã parse sẵn ==========

	/**
	 * Lấy bán kính GPS (mét) Default: 100
	 */
	int getGpsRadius();

	/**
	 * Lấy giờ bắt đầu check-in Default: 07:00
	 */
	LocalTime getCheckinStart();

	/**
	 * Lấy giờ kết thúc check-in Default: 09:00
	 */
	LocalTime getCheckinEnd();

	/**
	 * Lấy giờ bắt đầu check-out Default: 16:00
	 */
	LocalTime getCheckoutStart();

	/**
	 * Lấy giờ kết thúc check-out Default: 22:00
	 */
	LocalTime getCheckoutEnd();

	/**
	 * Lấy ngưỡng tồn kho thấp Default: 10
	 */
	double getStockMinAlert();

	/**
	 * Lấy ngưỡng tồn kho tối đa Default: 1000
	 */
	double getStockMaxAlert();
}