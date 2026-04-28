package com.branchmanagement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the status of an attendance record.
 */
@Getter
@RequiredArgsConstructor
public enum AttendanceStatus {
    NORMAL("normal", "Hợp lệ"),
    ABNORMAL("abnormal", "Bất thường"),
    APPROVED("approved", "Đã duyệt"),
    REJECTED("rejected", "Từ chối");

    private final String value;
    private final String description;

    public static AttendanceStatus fromValue(String value) {
        for (AttendanceStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
