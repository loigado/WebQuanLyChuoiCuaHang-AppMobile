package com.branchmanagement.controller;

import com.branchmanagement.entity.Announcement;
import com.branchmanagement.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/announcements")
@CrossOrigin(origins = "*")
public class AdminAnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @GetMapping
    public ResponseEntity<List<Announcement>> getAll() {
        return ResponseEntity.ok(announcementRepository.findTop5ByOrderByCreatedAtDesc());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> payload) {
        Announcement announcement = new Announcement();
        announcement.setTitle((String) payload.get("title"));
        announcement.setContent((String) payload.get("content"));
        announcement.setAuthor(payload.get("author") != null ? (String) payload.get("author") : "Admin");
        
        // Xử lý ngày hết hạn (ví dụ gửi lên số ngày: 3)
        if (payload.get("expiryDays") != null) {
            int days = Integer.parseInt(payload.get("expiryDays").toString());
            announcement.setExpiryDate(LocalDateTime.now().plusDays(days));
        }
        
        announcementRepository.save(announcement);
        return ResponseEntity.ok(Map.of("message", "Đăng thông báo thành công!"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Announcement updated) {
        return announcementRepository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setContent(updated.getContent());
            announcementRepository.save(existing);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công!"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        announcementRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa thông báo thành công!"));
    }
}
