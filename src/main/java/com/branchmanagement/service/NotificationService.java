package com.branchmanagement.service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.branchmanagement.entity.Notification;
import com.branchmanagement.entity.User;
import com.branchmanagement.repository.NotificationRepository;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepo;

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    /**
     * Hàm dùng chung để bắn thông báo cho bất kỳ module nào (Kho, Chấm công...)
     */
    @Transactional
    public void sendAlert(User recipient, String type, String title, String content, String priority, String actionUrl) {
        if (recipient == null) {
            logger.warn("Không thể gửi thông báo vì không xác định được người nhận.");
            return;
        }

        Notification alert = new Notification();
        alert.setRecipient(recipient);
        alert.setType(type);
        alert.setTitle(title);
        alert.setContent(content);
        alert.setPriority(priority);
        alert.setActionUrl(actionUrl);
        
        // Thiết lập thời hạn hết hạn (Expires At) dựa trên độ ưu tiên
        LocalDateTime now = LocalDateTime.now();
        if ("Urgent".equalsIgnoreCase(priority)) {
            alert.setExpiresAt(now.plusHours(24)); // Urgent: Yêu cầu xử lý trong 24h
        } else if ("High".equalsIgnoreCase(priority)) {
            alert.setExpiresAt(now.plusHours(48)); // High: 48h
        } else {
            alert.setExpiresAt(now.plusDays(7));   // Bình thường: 7 ngày
        }
        
        notificationRepo.save(alert);
        logger.info("Đã gửi cảnh báo [{}] tới User ID: {}", type, recipient.getUserId());
    }
}