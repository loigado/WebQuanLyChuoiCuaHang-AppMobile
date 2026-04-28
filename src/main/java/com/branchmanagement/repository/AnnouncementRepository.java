package com.branchmanagement.repository;

import com.branchmanagement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {
    // Lấy 5 thông báo mới nhất
    List<Announcement> findTop5ByOrderByCreatedAtDesc();
}
