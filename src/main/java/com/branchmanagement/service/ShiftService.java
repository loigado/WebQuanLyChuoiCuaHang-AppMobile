package com.branchmanagement.service;

public interface ShiftService {
    // Chỉ bao gồm các hàm mà Controller của bạn đang gọi
    void updateUserInShift(Integer userShiftId, Integer newUserId);
    void deleteShift(Integer shiftId);
}