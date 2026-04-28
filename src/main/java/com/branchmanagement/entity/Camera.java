package com.branchmanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entity mapping bảng Camera trong SQL Server
 */
@Entity
@Table(name = "Camera")
public class Camera {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "camera_id")
	private Integer cameraId;

	@Column(name = "camera_name", nullable = false, length = 100)
	private String cameraName;

	// Quan hệ ManyToOne với Branch
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	/**
	 * URL stream RTSP của MediaMTX Ví dụ: rtsp://localhost:8554/camera1
	 */
	@Column(name = "stream_url", nullable = false, length = 500)
	private String streamUrl;

	/**
	 * Trạng thái: 'active' | 'inactive' Soft delete sử dụng status = 'inactive'
	 */
	@Column(name = "status", length = 20)
	private String status = "active";

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	// Tự động set thời gian tạo trước khi persist
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// Getters & Setters
	public Integer getCameraId() {
		return cameraId;
	}

	public void setCameraId(Integer cameraId) {
		this.cameraId = cameraId;
	}

	public String getCameraName() {
		return cameraName;
	}

	public void setCameraName(String cameraName) {
		this.cameraName = cameraName;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public String getStreamUrl() {
		return streamUrl;
	}

	public void setStreamUrl(String streamUrl) {
		this.streamUrl = streamUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	// Manual builder to keep existing service code using Camera.builder()
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final Camera instance = new Camera();

		public Builder cameraName(String name) {
			instance.cameraName = name;
			return this;
		}

		public Builder branch(Branch branch) {
			instance.branch = branch;
			return this;
		}

		public Builder streamUrl(String url) {
			instance.streamUrl = url;
			return this;
		}

		public Builder status(String status) {
			instance.status = status;
			return this;
		}

		public Camera build() {
			return instance;
		}
	}
}