package com.branchmanagement.dto;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin SystemConfig cho frontend
 */
public class SystemConfigResponse {

	private Integer configId;
	private String configKey;
	private String configValue;
	private String description;
	private LocalDateTime updatedAt;

	// Constructor rỗng
	public SystemConfigResponse() {
	}

	// Constructor đầy đủ
	public SystemConfigResponse(Integer configId, String configKey, String configValue, String description,
			LocalDateTime updatedAt) {
		this.configId = configId;
		this.configKey = configKey;
		this.configValue = configValue;
		this.description = description;
		this.updatedAt = updatedAt;
	}

	// Getters & Setters
	public Integer getConfigId() {
		return configId;
	}

	public void setConfigId(Integer configId) {
		this.configId = configId;
	}

	public String getConfigKey() {
		return configKey;
	}

	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	// Builder pattern
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Integer configId;
		private String configKey;
		private String configValue;
		private String description;
		private LocalDateTime updatedAt;

		public Builder configId(Integer configId) {
			this.configId = configId;
			return this;
		}

		public Builder configKey(String configKey) {
			this.configKey = configKey;
			return this;
		}

		public Builder configValue(String configValue) {
			this.configValue = configValue;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder updatedAt(LocalDateTime updatedAt) {
			this.updatedAt = updatedAt;
			return this;
		}

		public SystemConfigResponse build() {
			return new SystemConfigResponse(configId, configKey, configValue, description, updatedAt);
		}
	}
}