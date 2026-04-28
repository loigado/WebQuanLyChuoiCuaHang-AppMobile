package com.branchmanagement.controller;

import com.branchmanagement.dto.CheckInRequest;
import com.branchmanagement.dto.LoginRequest;
import com.branchmanagement.entity.Attendance;
import com.branchmanagement.entity.User;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.service.MobileEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Cấu hình CORS cực kỳ thông thoáng để trình duyệt không chặn
@CrossOrigin(
    origins = "http://localhost:8100", 
    allowedHeaders = "*", 
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
public class MobileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MobileEmployeeService mobileEmployeeService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        
        // Kiểm tra mật khẩu bằng PasswordEncoder
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.ok(Map.of(
                    "user_id", String.valueOf(user.getUserId()),
                    "full_name", user.getFullName(),
                    "role", user.getRole().name()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("message", "Sai tài khoản hoặc mật khẩu"));
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkInWithGPS(@RequestBody CheckInRequest request) {
        try {
            Attendance result = mobileEmployeeService.processCheckIn(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "type", result.getStatus(),
                    "message", result.getStatus().equals("normal") ? "Thành công!" : "Bất thường!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam Integer userId) {
        List<Attendance> history = mobileEmployeeService.getPersonalAttendanceHistory(userId);
        
        // Chuyển đổi sang Map để tránh lỗi vòng lặp tuần hoàn JSON
        List<Map<String, Object>> result = history.stream().map(att -> {
            Map<String, Object> map = new HashMap<>();
            map.put("check_in", att.getCheckIn() != null ? att.getCheckIn().toString() : null);
            map.put("check_out", att.getCheckOut() != null ? att.getCheckOut().toString() : null);
            map.put("total_hours", att.getTotalHours());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@RequestBody CheckInRequest request) {
        try {
            Attendance result = mobileEmployeeService.processCheckOut(request);
            return ResponseEntity.ok(Map.of(
                "message", "Check-out thành công!",
                "totalHours", result.getTotalHours()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}