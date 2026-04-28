package com.branchmanagement.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShiftDTO {
    private Integer userShiftId;
    private String shiftName; 
    
    // ✅ THÊM DÒNG NÀY (Ngày làm việc)
    private LocalDate date; 
    
    private LocalTime startTime; 
    private LocalTime endTime;   
    
    // ✅ THÊM DÒNG NÀY (Tên chi nhánh)
    private String branchName; 
    
    private String status; 
    private String note;
}