package com.branchmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.branchmanagement.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy danh sách các thông báo CHƯA ĐỌC của một User cụ thể, sắp xếp mới nhất lên đầu
    List<Notification> findByRecipient_UserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);
    
}