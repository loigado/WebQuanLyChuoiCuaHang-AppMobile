package com.branchmanagement.dto;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO nhận data khi update nhiều config cùng lúc Dùng cho tính năng "Lưu tất
 * cả" trong UI
 */
public class SystemConfigBulkUpdateRequest {

	/**
	 * Map: config_key → config_value Ví dụ: { "gps_radius": "150", "checkin_start":
	 * "08:00" }
	 */
	@NotEmpty(message = "Danh sách cấu hình không được rỗng")
	private Map<String, String> configs;

	// Constructor rỗng
	public SystemConfigBulkUpdateRequest() {
	}

	// Constructor đầy đủ
	public SystemConfigBulkUpdateRequest(Map<String, String> configs) {
		this.configs = configs;
	}

	// Getter & Setter
	public Map<String, String> getConfigs() {
		return configs;
	}

	public void setConfigs(Map<String, String> configs) {
		this.configs = configs;
	}
}