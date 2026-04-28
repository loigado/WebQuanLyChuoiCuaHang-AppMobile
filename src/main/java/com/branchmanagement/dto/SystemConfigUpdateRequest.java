package com.branchmanagement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO nhận data khi update 1 config Chỉ cho phép update configValue, không đổi
 * configKey
 */
public class SystemConfigUpdateRequest {

	@NotBlank(message = "Giá trị cấu hình không được để trống")
	private String configValue;

	// Constructor rỗng
	public SystemConfigUpdateRequest() {
	}

	// Constructor đầy đủ
	public SystemConfigUpdateRequest(String configValue) {
		this.configValue = configValue;
	}

	// Getter & Setter
	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}
}