package com.branchmanagement.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.branchmanagement.annotation.AuditAction;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.SystemConfigBulkUpdateRequest;
import com.branchmanagement.dto.SystemConfigResponse;
import com.branchmanagement.dto.SystemConfigUpdateRequest;
import com.branchmanagement.service.SystemConfigService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/system-config")
public class SystemConfigController {

	private final SystemConfigService configService;

	public SystemConfigController(SystemConfigService configService) {
		this.configService = configService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getAllConfigs() {
		return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách cấu hình thành công", configService.getAllConfigs()));
	}

	@GetMapping("/{key}")
	public ResponseEntity<ApiResponse<SystemConfigResponse>> getConfigByKey(@PathVariable String key) {
		try {
			SystemConfigResponse config = configService.getByKey(key);
			return ResponseEntity.ok(ApiResponse.ok("Lấy cấu hình thành công", config));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
		}
	}

	@PutMapping("/{key}")
    @AuditAction(action = "UPDATE_SYSTEM_CONFIG")
	public ResponseEntity<ApiResponse<SystemConfigResponse>> updateConfig(@PathVariable String key,
			@Valid @RequestBody SystemConfigUpdateRequest request) {
		try {
			SystemConfigResponse updated = configService.updateConfig(key, request);
			return ResponseEntity.ok(ApiResponse.ok("Cập nhật cấu hình thành công", updated));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
		}
	}

	@PutMapping("/bulk")
    @AuditAction(action = "BULK_UPDATE_SYSTEM_CONFIG")
	public ResponseEntity<ApiResponse<Void>> bulkUpdate(@Valid @RequestBody SystemConfigBulkUpdateRequest request) {
		try {
			configService.bulkUpdate(request);
			return ResponseEntity.ok(ApiResponse.ok("Cập nhật hàng loạt cấu hình thành công", null));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
		}
	}
}