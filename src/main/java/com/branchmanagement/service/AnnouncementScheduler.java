package com.branchmanagement.service;

import com.branchmanagement.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
public class AnnouncementScheduler {

    @Autowired
    private AnnouncementRepository announcementRepository;

    /**
     * Tự động xóa các thông báo đã hết hạn.
     * Chạy vào phút thứ 0 của mỗi giờ.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpiredAnnouncements() {
        System.out.println("DEBUG: Đang quét và xóa thông báo hết hạn...");
        announcementRepository.findAll().stream()
            .filter(a -> a.getExpiryDate() != null && a.getExpiryDate().isBefore(LocalDateTime.now()))
            .forEach(a -> {
                System.out.println("Xóa thông báo hết hạn: " + a.getTitle());
                announcementRepository.delete(a);
            });
    }
}
