package com.branchmanagement.dto;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin Branch cho frontend Dùng trong dropdown, danh sách chi
 * nhánh, map GPS...
 */
public class BranchResponse {

	private Integer branchId;

	private String branchCode;

	private String branchName;

	private String address;

	/**
	 * Tọa độ GPS - dùng cho chấm công, hiển thị map
	 */
	private Double latitude;
	private Double longitude;

	private String status;

	private LocalDateTime createdAt;

	/**
	 * Số lượng camera tại chi nhánh này (optional) Có thể dùng khi hiển thị thống
	 * kê
	 */
	private Integer cameraCount;

	public Integer getBranchId() {
		return branchId;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public String getBranchName() {
		return branchName;
	}

	public String getAddress() {
		return address;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public String getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Integer getCameraCount() {
		return cameraCount;
	}

	// Manual builder
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final BranchResponse instance = new BranchResponse();

		public Builder branchId(Integer id) {
			instance.branchId = id;
			return this;
		}

		public Builder branchCode(String code) {
			instance.branchCode = code;
			return this;
		}

		public Builder branchName(String name) {
			instance.branchName = name;
			return this;
		}

		public Builder address(String address) {
			instance.address = address;
			return this;
		}

		public Builder latitude(Double lat) {
			instance.latitude = lat;
			return this;
		}

		public Builder longitude(Double lon) {
			instance.longitude = lon;
			return this;
		}

		public Builder status(String status) {
			instance.status = status;
			return this;
		}

		public Builder createdAt(LocalDateTime createdAt) {
			instance.createdAt = createdAt;
			return this;
		}

		public Builder cameraCount(Integer count) {
			instance.cameraCount = count;
			return this;
		}

		public BranchResponse build() {
			return instance;
		}
	}
}