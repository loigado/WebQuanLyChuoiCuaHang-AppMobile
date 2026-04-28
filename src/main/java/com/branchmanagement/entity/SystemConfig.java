package com.branchmanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entity mapping bảng SystemConfig trong SQL Server Lưu cấu hình hệ thống dạng
 * key-value
 */
@Entity
@Table(name = "[SystemConfig]")
public class SystemConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "config_id")
	private Integer configId;

	/**
	 * Key cấu hình - unique Ví dụ: gps_radius, checkin_start, stock_min_alert
	 */
	@Column(name = "config_key", unique = true, nullable = false, length = 50)
	private String configKey;

	/**
	 * Giá trị cấu hình Ví dụ: "100", "07:00", "10"
	 */
	@Column(name = "config_value", nullable = false, length = 200)
	private String configValue;

	/**
	 * Mô tả ý nghĩa của config
	 */
	@Column(name = "description", length = 200)
	private String description;

	/**
	 * Thời gian cập nhật gần nhất
	 */
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// Tự động set thời gian khi tạo mới
	@PrePersist
	protected void onCreate() {
		this.updatedAt = LocalDateTime.now();
	}

	// Tự động update thời gian khi sửa
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
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

	// Builder pattern (manual)
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final SystemConfig instance = new SystemConfig();

		public Builder configKey(String key) {
			instance.configKey = key;
			return this;
		}

		public Builder configValue(String value) {
			instance.configValue = value;
			return this;
		}

		public Builder description(String desc) {
			instance.description = desc;
			return this;
		}

		public SystemConfig build() {
			return instance;
		}
	}
}