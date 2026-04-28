package com.branchmanagement.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.entity.Notification;
import com.branchmanagement.repository.NotificationRepository;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // 1. Lấy danh sách cảnh báo CHƯA ĐỌC của tôi
    @GetMapping("/my-alerts/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getMyAlerts(@PathVariable Integer userId) {
        List<Notification> alerts = notificationRepository.findByRecipient_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách thông báo thành công", alerts));
    }

    // 2. Đánh dấu 1 thông báo là ĐÃ ĐỌC (Khi user click vào)
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
            
        notification.setRead(true);
        notificationRepository.save(notification);
        
        return ResponseEntity.ok(ApiResponse.ok("Đã đánh dấu đọc", null));
    }
}