package com.branchmanagement.dto;

import lombok.Data;

@Data
public class CheckInRequest {
    private Integer userId;
    private Integer branchId;
    private Double lat; // Vĩ độ của điện thoại
    private Double lng; // Kinh độ của điện thoại
    private String photoUrl; // Link ảnh khuôn mặt
}