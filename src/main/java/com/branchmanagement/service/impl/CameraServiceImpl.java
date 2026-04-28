package com.branchmanagement.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.branchmanagement.dto.CameraRequest;
import com.branchmanagement.dto.CameraResponse;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.entity.Camera;
import com.branchmanagement.repository.BranchRepository;
import com.branchmanagement.repository.CameraRepository;
import com.branchmanagement.service.CameraService;
import org.springframework.scheduling.annotation.Scheduled;
@Service
public class CameraServiceImpl implements CameraService {

    private static final Logger logger = LoggerFactory.getLogger(CameraServiceImpl.class);

    private final CameraRepository cameraRepository;
    private final BranchRepository branchRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // Dùng để gọi API MediaMTX
    
    @Value("${media.hls.url:http://localhost:8888}")
    private String hlsBaseUrl;

    public CameraServiceImpl(CameraRepository cameraRepository, BranchRepository branchRepository) {
        this.cameraRepository = cameraRepository;
        this.branchRepository = branchRepository;
    }

    private static final String MEDIAMTX_API_URL = "http://localhost:9997/v3/paths/get/";

    @Override
    public List<CameraResponse> getAllCameras() {
        return cameraRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponseWithConnectionStatus)
                .collect(Collectors.toList());
    }

    @Override
    public List<CameraResponse> getCamerasByBranch(Integer branchId) {
        return cameraRepository.findByBranch_BranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(this::toResponseWithConnectionStatus)
                .collect(Collectors.toList());
    }

    // --- LOGIC KIỂM TRA ONLINE/OFFLINE THỰC TẾ ---
    private String checkRealtimeStatus(String cameraName) {
        try {
            // MediaMTX trả về 200 nếu path tồn tại, 404 nếu không thấy luồng
            String url = MEDIAMTX_API_URL + cameraName;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return "Online";
            }
        } catch (Exception e) {
            return "Offline"; // Không kết nối được hoặc 404
        }
        return "Offline";
    }

    @Override
    @Transactional
    public CameraResponse createCamera(CameraRequest request) {
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));

        Camera camera = Camera.builder()
                .cameraName(request.getCameraName())
                .branch(branch)
                .streamUrl(request.getStreamUrl())
                .status("active")
                .build();

        return toResponse(cameraRepository.save(camera));
    }

    @Override
    @Transactional
    public CameraResponse updateCamera(Integer id, CameraRequest request) {
        Camera camera = cameraRepository.findById(id).orElseThrow();
        Branch branch = branchRepository.findById(request.getBranchId()).orElseThrow();

        camera.setCameraName(request.getCameraName());
        camera.setBranch(branch);
        camera.setStreamUrl(request.getStreamUrl());

        return toResponse(cameraRepository.save(camera));
    }

    @Override
    @Transactional
    public void deleteCamera(Integer id) {
        Camera camera = cameraRepository.findById(id).orElseThrow();
        camera.setStatus("inactive"); // Soft delete
        cameraRepository.save(camera);
    }

    @Override
    public String getHlsUrl(Integer id) {
        Camera camera = cameraRepository.findById(id).orElseThrow();
        return hlsBaseUrl + "/" + camera.getCameraName() + "/index.m3u8";
    }

    // Mapping Entity -> Response kèm Status Online/Offline
    private CameraResponse toResponseWithConnectionStatus(Camera camera) {
        CameraResponse response = toResponse(camera);
        if ("active".equals(camera.getStatus())) {
            response.setConnectionStatus(checkRealtimeStatus(camera.getCameraName()));
        } else {
            response.setConnectionStatus("Inactive");
        }
        return response;
    }

    private CameraResponse toResponse(Camera camera) {
        return CameraResponse.builder()
                .cameraId(camera.getCameraId())
                .cameraName(camera.getCameraName())
                .branchId(camera.getBranch().getBranchId())
                .branchName(camera.getBranch().getBranchName())
                .streamUrl(camera.getStreamUrl())
                .hlsUrl(hlsBaseUrl + "/" + camera.getCameraName() + "/index.m3u8")
                .status(camera.getStatus())
                .createdAt(camera.getCreatedAt())
                .build();
    }
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void updateAllCameraStatus() {
        List<Camera> activeCameras = cameraRepository.findByStatusOrderByCreatedAtDesc("active");
        
        for (Camera cam : activeCameras) {
            String currentStatus = checkRealtimeStatus(cam.getCameraName());
            // Bạn có thể log ra để theo dõi
            logger.info("Auto-check Camera {}: {}", cam.getCameraName(), currentStatus);
            
            // Cập nhật nếu cần (tùy chọn lưu vào DB hoặc chỉ trả về cho FE)
            // cam.setConnectionStatus(currentStatus); 
            // cameraRepository.save(cam);
        }
    }
}