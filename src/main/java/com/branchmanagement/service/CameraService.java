package com.branchmanagement.service;

import java.util.List;

import com.branchmanagement.dto.CameraRequest;
import com.branchmanagement.dto.CameraResponse;

/**
 * Interface định nghĩa các nghiệp vụ quản lý Camera
 */
public interface CameraService {

	/**
	 * Lấy danh sách tất cả camera trong hệ thống
	 */
	List<CameraResponse> getAllCameras();

	/**
	 * Lấy danh sách camera theo chi nhánh
	 */
	List<CameraResponse> getCamerasByBranch(Integer branchId);

	/**
	 * Thêm camera mới
	 */
	CameraResponse createCamera(CameraRequest request);

	/**
	 * Cập nhật thông tin camera
	 */
	CameraResponse updateCamera(Integer id, CameraRequest request);

	/**
	 * Soft delete: set status = 'inactive' thay vì xóa thật
	 */
	void deleteCamera(Integer id);

	/**
	 * Lấy HLS URL từ camera ID Convert RTSP → HLS:
	 * http://localhost:8888/{cameraName}/index.m3u8
	 */
	String getHlsUrl(Integer id);
}