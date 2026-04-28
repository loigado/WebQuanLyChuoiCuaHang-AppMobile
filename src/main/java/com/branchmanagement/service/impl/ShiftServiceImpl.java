package com.branchmanagement.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.branchmanagement.entity.User;
import com.branchmanagement.entity.UserShift;
import com.branchmanagement.repository.ShiftRepository;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.repository.UserShiftRepository;
import com.branchmanagement.service.ShiftService;

@Service
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final UserShiftRepository userShiftRepository;

    public ShiftServiceImpl(ShiftRepository shiftRepository, 
                            UserRepository userRepository, 
                            UserShiftRepository userShiftRepository) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
        this.userShiftRepository = userShiftRepository;
    }

    @Override
    public void updateUserInShift(Integer userShiftId, Integer newUserId) {
        UserShift us = userShiftRepository.findById(userShiftId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin phân ca"));
        User user = userRepository.findById(newUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên mới"));
        
        us.setUser(user);
        userShiftRepository.save(us);
    }

    @Override
    public void deleteShift(Integer shiftId) {
        // Xóa ca làm việc, database sẽ tự xóa UserShift nếu bạn cấu hình Cascade.ALL
        shiftRepository.deleteById(shiftId);
    }
}