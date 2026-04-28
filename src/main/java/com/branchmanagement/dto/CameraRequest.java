package com.branchmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO nhận data khi tạo hoặc cập nhật Camera từ frontend
 */
public class CameraRequest {

    @NotBlank(message = "Tên camera không được để trống")
    private String cameraName;

    @NotNull(message = "Chi nhánh không được để trống")
    private Integer branchId;

    /**
     * Stream URL dạng RTSP Ví dụ: rtsp://localhost:8554/camera1
     */
    @NotBlank(message = "Stream URL không được để trống")
    private String streamUrl;

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }
}