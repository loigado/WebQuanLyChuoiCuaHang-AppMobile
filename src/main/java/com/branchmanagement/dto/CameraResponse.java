package com.branchmanagement.dto;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin Camera cho frontend Bao gồm branch_name để hiển thị trực
 * tiếp (không cần join thêm ở FE)
 */
public class CameraResponse {

    private Integer cameraId;
    private String cameraName;

    // Thông tin chi nhánh
    private Integer branchId;
    private String branchName;
    private String connectionStatus;
    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }

    /** URL RTSP gốc: rtsp://localhost:8554/{name} */
    private String streamUrl;

    /** URL HLS để play trong browser: http://localhost:8888/{name}/index.m3u8 */
    private String hlsUrl;

    private String status;
    private LocalDateTime createdAt;

    public Integer getCameraId() {
        return cameraId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public String getHlsUrl() {
        return hlsUrl;
    }

    public String getStatus() {
        return status;
    }
    

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Manual builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CameraResponse instance = new CameraResponse();

        public Builder cameraId(Integer id) {
            instance.cameraId = id;
            return this;
        }

        public Builder cameraName(String name) {
            instance.cameraName = name;
            return this;
        }

        public Builder branchId(Integer id) {
            instance.branchId = id;
            return this;
        }

        public Builder branchName(String name) {
            instance.branchName = name;
            return this;
        }

        public Builder streamUrl(String url) {
            instance.streamUrl = url;
            return this;
        }

        public Builder hlsUrl(String url) {
            instance.hlsUrl = url;
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

        public CameraResponse build() {
            return instance;
        }
    }
}