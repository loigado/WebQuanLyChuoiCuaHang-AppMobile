package com.branchmanagement.service.impl;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.branchmanagement.dto.SystemConfigBulkUpdateRequest;
import com.branchmanagement.dto.SystemConfigResponse;
import com.branchmanagement.dto.SystemConfigUpdateRequest;
import com.branchmanagement.entity.SystemConfig;
import com.branchmanagement.repository.SystemConfigRepository;
import com.branchmanagement.service.SystemConfigService;

@Service
@Transactional(readOnly = true)
public class SystemConfigServiceImpl implements SystemConfigService {

	private static final Logger logger = LoggerFactory.getLogger(SystemConfigServiceImpl.class);

	private final SystemConfigRepository configRepository;

	public SystemConfigServiceImpl(SystemConfigRepository configRepository) {
		this.configRepository = configRepository;
	}

	@Override
	public List<SystemConfigResponse> getAllConfigs() {
		return configRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
    public SystemConfigResponse getByKey(String key) {
        String cleanKey = key != null ? key.trim() : "";
        
        return configRepository.findByConfigKey(cleanKey)
                .map(this::toResponse)
                // CHÚ Ý: Dùng orElseGet, KHÔNG DÙNG orElseThrow
                .orElseGet(() -> {
                    SystemConfigResponse fallback = new SystemConfigResponse();
                    fallback.setConfigKey(cleanKey);
                    fallback.setConfigValue("100"); 
                    fallback.setDescription("Giá trị mặc định an toàn");
                    return fallback;
                });
    }

    @Override
    public String getValue(String key) {
        String cleanKey = key != null ? key.trim() : "";
        
        return configRepository.findByConfigKey(cleanKey)
                .map(SystemConfig::getConfigValue)
                // CHÚ Ý: Dùng orElseGet, KHÔNG DÙNG orElseThrow
                .orElseGet(() -> {
                    if ("gps_radius".equals(cleanKey)) return "100";
                    if ("checkin_start".equals(cleanKey)) return "07:00";
                    if ("checkout_end".equals(cleanKey)) return "22:00";
                    if ("stock_min_alert".equals(cleanKey)) return "10";
                    return "0";
                });
    }

    @Override
	@Transactional
	public SystemConfigResponse updateConfig(String key, SystemConfigUpdateRequest request) {
		String cleanKey = key != null ? key.trim() : "";

		// Tự động tạo mới nếu chưa tồn tại (Upsert)
		SystemConfig config = configRepository.findByConfigKey(cleanKey)
				.orElseGet(() -> {
					SystemConfig newConfig = new SystemConfig();
					newConfig.setConfigKey(cleanKey);
					newConfig.setDescription("Hệ thống tự động tạo mới khi cập nhật");
					return newConfig;
				});

		config.setConfigValue(request.getConfigValue());
		SystemConfig updated = configRepository.save(config);

		logger.info("Đã cập nhật/tạo mới cấu hình: {} = {}", cleanKey, request.getConfigValue());
		return toResponse(updated);
	}

    @Override
	@Transactional
	public void bulkUpdate(SystemConfigBulkUpdateRequest request) {
		Map<String, String> configs = request.getConfigs();

		for (Map.Entry<String, String> entry : configs.entrySet()) {
			String cleanKey = entry.getKey() != null ? entry.getKey().trim() : "";
			String value = entry.getValue();

			// Tự động tạo mới nếu chưa tồn tại (Upsert)
			SystemConfig config = configRepository.findByConfigKey(cleanKey)
					.orElseGet(() -> {
						SystemConfig newConfig = new SystemConfig();
						newConfig.setConfigKey(cleanKey);
						newConfig.setDescription("Hệ thống tự tạo từ Bulk Update");
						return newConfig;
					});

			config.setConfigValue(value);
			configRepository.save(config);
		}

		logger.info("Đã cập nhật/tạo mới {} cấu hình", configs.size());
	}

	// ========== UTILITY METHODS ==========

	@Override
	public int getGpsRadius() {
		String value = getValue("gps_radius");
		try {
			return value != null ? Integer.parseInt(value) : 100;
		} catch (NumberFormatException e) {
			logger.warn("Invalid gps_radius value: {}, using default 100", value);
			return 100;
		}
	}

	@Override
	public LocalTime getCheckinStart() {
		String value = getValue("checkin_start");
		try {
			return value != null ? LocalTime.parse(value) : LocalTime.of(7, 0);
		} catch (Exception e) {
			logger.warn("Invalid checkin_start value: {}, using default 07:00", value);
			return LocalTime.of(7, 0);
		}
	}

	@Override
	public LocalTime getCheckinEnd() {
		String value = getValue("checkin_end");
		try {
			return value != null ? LocalTime.parse(value) : LocalTime.of(9, 0);
		} catch (Exception e) {
			logger.warn("Invalid checkin_end value: {}, using default 09:00", value);
			return LocalTime.of(9, 0);
		}
	}

	@Override
	public LocalTime getCheckoutStart() {
		String value = getValue("checkout_start");
		try {
			return value != null ? LocalTime.parse(value) : LocalTime.of(16, 0);
		} catch (Exception e) {
			logger.warn("Invalid checkout_start value: {}, using default 16:00", value);
			return LocalTime.of(16, 0);
		}
	}

	@Override
	public LocalTime getCheckoutEnd() {
		String value = getValue("checkout_end");
		try {
			return value != null ? LocalTime.parse(value) : LocalTime.of(22, 0);
		} catch (Exception e) {
			logger.warn("Invalid checkout_end value: {}, using default 22:00", value);
			return LocalTime.of(22, 0);
		}
	}

	@Override
	public double getStockMinAlert() {
		String value = getValue("stock_min_alert");
		try {
			return value != null ? Double.parseDouble(value) : 10.0;
		} catch (NumberFormatException e) {
			logger.warn("Invalid stock_min_alert value: {}, using default 10", value);
			return 10.0;
		}
	}

	@Override
	public double getStockMaxAlert() {
		String value = getValue("stock_max_alert");
		try {
			return value != null ? Double.parseDouble(value) : 1000.0;
		} catch (NumberFormatException e) {
			logger.warn("Invalid stock_max_alert value: {}, using default 1000", value);
			return 1000.0;
		}
	}

	// ========== HELPER METHODS ==========

	/**
	 * Map SystemConfig entity → SystemConfigResponse DTO
	 */
	private SystemConfigResponse toResponse(SystemConfig config) {
		return SystemConfigResponse.builder().configId(config.getConfigId()).configKey(config.getConfigKey())
				.configValue(config.getConfigValue()).description(config.getDescription())
				.updatedAt(config.getUpdatedAt()).build();
	}
}